package common.assets

import com.codahale.metrics.MetricRegistry
import com.microsoft.sqlserver.jdbc.SQLServerXADataSource
import common.config.Configuration
import io.dropwizard.db.DataSourceFactory
import io.dropwizard.db.ManagedDataSource
import io.dropwizard.db.ManagedPooledDataSource
import io.dropwizard.jdbi3.JdbiFactory
import io.dropwizard.setup.Environment
import io.dropwizard.util.Duration
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.statement.DefaultStatementBuilder
import org.jdbi.v3.core.statement.SqlLogger
import org.jdbi.v3.core.statement.StatementBuilderFactory
import org.jdbi.v3.core.statement.StatementContext
import java.lang.management.ManagementFactory
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import javax.management.ObjectName
import java.time.Duration.between as javaTimeDurationBetween


/**
 * Resource Builder for JDBI 3. Use [ResourceBuilder].jdbi3 as the entry point
 *
 * This assumes a Map type for configuration, with the following properties:
 * 1. id - sql server id
 * 2. user
 * 3. password
 * 4. db
 * 5. timeout (optional, default = 30s)
 * 6. serverName (optional)
 */
class Jdbi3Builder(builder: ResourceBuilder) {
    private val clientName = "${Configuration.applicationName}/${builder.resourceId}"
    private val id = builder.getTestEnvironmentAwareConfig("id")!!
    private val serverName = builder.getTestEnvironmentAwareConfig("serverName") ?: "$id.database.windows.net"
    private val user = builder.getTestEnvironmentAwareConfig("user") ?: ""
    private val password = builder.getTestEnvironmentAwareConfig("password") ?: ""
    private val db = builder.getTestEnvironmentAwareConfig("db")!!
    private val loginTimeout = builder.descriptor["timeout"]?.toInt() ?: 30
    private val minPoolSize = builder.descriptor["minPoolSize"]?.toInt() ?: 1
    private val maxPoolSize = builder.descriptor["maxPoolSize"]?.toInt() ?: 64
    private val connectionWaitTimoutMs = builder.descriptor["connWaitMs"]?.toLong() ?: TimeUnit.SECONDS.toMillis(1)
    private val checkOnBorrow = builder.descriptor["checkOnBorrow"]?.toBoolean() ?: false
    private val checkOnReturn = builder.descriptor["checkOnReturn"]?.toBoolean() ?: false
    private val checkConnectionWhileIdle = builder.descriptor["checkInIdle"]?.toBoolean() ?: true
    private val evictionIntervalMs = builder.descriptor["evictionMs"]?.toLong() ?: TimeUnit.SECONDS.toMillis(5)
    private val idleTimeMs = builder.descriptor["idleTimeMs"]?.toLong() ?: TimeUnit.MINUTES.toMillis(1)
    private val instrumentationKey = builder.descriptor["instrumentationKey"]
    private val logValidationErrors = builder.descriptor["logValidationErrors"]?.toBoolean() ?: false
    private val logAbandonedConnections = builder.descriptor["logAbandonedConnections"]?.toBoolean() ?: false
    private val maxSocketTimeoutInMillis = builder.descriptor["maxSocketTimeoutInMillis"]?.toInt() ?: 0
    private val maxQueryTimeoutInSeconds = builder.descriptor["maxQueryTimeoutInSeconds"]?.toInt() ?: -1
    private val applicationIntent = builder.descriptor["applicationIntent"]?.toString() ?: "ReadWrite"

    private val alwaysEncrypted = builder.descriptor["alwaysEncrypted"]?.toBoolean() ?: false
    private val aadSecurePrincipalId = Configuration.aadSPId
    private val aadSecurePrincipalSecret = Configuration.aadSPSecret

    private val telemetrySqlLogger = TelemetrySqlLogger(id, db, instrumentationKey)

    private val key = "$id/$user/$password/$db/$applicationIntent"

    /**
     * Executes a lambda, in the context of an open JDBI handle, and then closes that handle
     */
    fun <R, X : Exception> withHandle(block: (handle: Handle) -> R): R {
        return build().withHandle<R, X>(block)
    }

    /**
     * Get a new JDBI instance for the specified configuration
     */
    @JvmOverloads
    fun build(useXADatasource: Boolean = false, withoutInstrumentation: Boolean = false): Jdbi {
        return getJdbi3(this, useXADatasource, withoutInstrumentation).apply {
            if (!withoutInstrumentation) {
                setSqlLogger(telemetrySqlLogger)
            }
        }
    }

    /**
     * Get a new Data Source for the specified configuration
     */
    fun build(environment: Environment): Jdbi {
        return dbiMap.getOrPut(key) {
            dataSourceFactory(this).let {
                val dataSource = it.build(environment.metrics(), "$id/$applicationIntent/$user")
                initDBIPerformanceCounters(id, dataSource, user)

                JdbiFactory().build(environment, it, dataSource, "$id/$applicationIntent").apply {
                    //                    val oldLog = sqlLog ?: NoOpLog()
//                    sqlLog = object : SQLLog by sqlLog {
//                        override fun logSQL(time: Long, sql: String) {
//                            oldLog.logSQL(time, sql)
//                            instrument(time, sql)
//                        }
//                    }
                    if (Configuration.getBoolean("customJdbiStatementBuilderEnabled",
                            false) == true) {
                        this.statementBuilderFactory = getCustomStmtBuilderFactory()
                    }

                    setSqlLogger(telemetrySqlLogger)
                }
            }
        }
    }

    companion object {
        private const val objectNamePrefix = "com.calendly:type=jdbi3pool,id="

        private val dbiMap = ConcurrentHashMap<String, Jdbi>()
        private val metricRegistry = MetricRegistry()

        private fun initDBIPerformanceCounters(id: String, managedDataSource: ManagedDataSource, userName: String = "", applicationIntent: String ="") {
            val jmxPool = (managedDataSource as? ManagedPooledDataSource)?.pool?.jmxPool
            jmxPool?.let {
                val mbs = ManagementFactory.getPlatformMBeanServer()
                val objectName = objectNamePrefix + id + userName + applicationIntent
                val name = ObjectName(objectName)
                mbs.registerMBean(it, name)

            }
        }

        private val testEnvironmentConfigOverrides = mapOf(
            "id" to "udtestsql",
            "user" to "SA",
            "password" to "Foobar1234",
            "db" to "TestDB",
            "serverName"  to "udtestsql",
            "encrypt" to "false"
        )

        private fun ResourceBuilder.getTestEnvironmentAwareConfig(key: String): String? {
            return if(Configuration.isTestEnvironment) {
                testEnvironmentConfigOverrides[key]!!
            } else {
                this.descriptor[key]
            }
        }

        @Synchronized
        private fun getJdbi3(
            builder: Jdbi3Builder,
            useXADatasource: Boolean = false,
            withoutInstrumentation: Boolean
        ): Jdbi =
            dbiMap.getOrPut(builder.key) {
                val id = builder.id
                val dataSource = if (useXADatasource) {
                    SQLServerXADataSource().apply {
                        this.applicationName = builder.clientName
                        this.databaseName = builder.db
                        this.hostNameInCertificate = "*.database.windows.net"
                        this.encrypt = true
                        this.trustServerCertificate = false
                        this.serverName = this.serverName ?: "$id.database.windows.net"
                        this.loginTimeout = builder.loginTimeout
                        if (builder.aadSecurePrincipalId.isNullOrBlank().not()
                            && builder.aadSecurePrincipalSecret.isNullOrBlank().not()
                        ) {
                            this.authentication = SqlServerAuthenticationType.ActiveDirectoryServicePrincipal.name
                            //this.aadSecurePrincipalId = builder.aadSecurePrincipalId
                            //this.aadSecurePrincipalSecret = builder.aadSecurePrincipalSecret
                        } else {
                            this.user = builder.user
                            this.setPassword(builder.password)
                        }
                    }
                } else {
                    dataSourceFactory(builder)
                        .build(metricRegistry, "$id/${builder.db}/${builder.user}").apply {
                            if (!withoutInstrumentation) {
                                initDBIPerformanceCounters(id, this, builder.user, builder.applicationIntent)
                            }
                        }
                }
                Jdbi.create(dataSource).also {
                    if (Configuration.getBoolean(
                            "customJdbiStatementBuilderEnabled",
                            false
                        ) == true
                    ) {
                        it.statementBuilderFactory = getCustomStmtBuilderFactory()
                    }
                }
            }

        private fun dataSourceFactory(builder: Jdbi3Builder): DataSourceFactory {
            return DataSourceFactory().apply {
                this.driverClass = "com.microsoft.sqlserver.jdbc.SQLServerDriver"
                this.user = builder.user
                this.password = builder.password
                this.minSize = builder.minPoolSize
                this.maxSize = builder.maxPoolSize
                this.maxWaitForConnection = Duration.milliseconds(builder.connectionWaitTimoutMs)
                this.validationQuery = "/* MSSQL Health Check */ SELECT 1"
                this.checkConnectionWhileIdle = builder.checkConnectionWhileIdle
                this.checkConnectionOnBorrow = builder.checkOnBorrow
                this.checkConnectionOnReturn = builder.checkOnReturn
                this.evictionInterval = Duration.milliseconds(builder.evictionIntervalMs)
                this.minIdleTime = Duration.milliseconds(builder.idleTimeMs)
                this.logValidationErrors = builder.logValidationErrors
                this.logAbandonedConnections = builder.logAbandonedConnections

                this.url = "jdbc:sqlserver://${builder.serverName}:1433;database=${builder.db}" +
                        ";databaseName=${builder.db};encrypt=true;trustServerCertificate=false" +
                        ";hostNameInCertificate=*.database.windows.net;loginTimeout=${builder.loginTimeout};" +
                        "applicationName=${builder.clientName};socketTimeout=${builder.maxSocketTimeoutInMillis};" +
                        "applicationIntent=${builder.applicationIntent};queryTimeout=${builder.maxQueryTimeoutInSeconds};"

                if (builder.aadSecurePrincipalId.isNullOrBlank().not()
                    && builder.aadSecurePrincipalSecret.isNullOrBlank().not()) {
                    this.url += "authentication=${SqlServerAuthenticationType.ActiveDirectoryServicePrincipal.name};aadSecurePrincipalId=${builder.aadSecurePrincipalId};aadSecurePrincipalSecret=${builder.aadSecurePrincipalSecret};"
                }
                if (builder.alwaysEncrypted) {
                    this.url += "columnEncryptionSetting=Enabled;"
                }
            }
        }

        private fun getCustomStmtBuilderFactory(): StatementBuilderFactory =
            StatementBuilderFactory {
                object : DefaultStatementBuilder() {
                    override fun create(conn: Connection, sql: String?, ctx: StatementContext): PreparedStatement {

                        val modifiedSql = """
                            
                            $sql""".trimMargin()
                        return if (ctx.isReturningGeneratedKeys) {
                            val columnNames = ctx.generatedKeysColumnNames
                            if (columnNames != null && columnNames.isNotEmpty()) conn.prepareStatement(
                                modifiedSql,
                                columnNames) else conn.prepareStatement(sql, 1)
                        } else {
                            if (ctx.isConcurrentUpdatable) conn.prepareStatement(modifiedSql,
                                ResultSet.TYPE_FORWARD_ONLY,
                                ResultSet.CONCUR_UPDATABLE) else {
                                conn.prepareStatement(modifiedSql,
                                    ResultSet.TYPE_FORWARD_ONLY,
                                    ResultSet.CONCUR_READ_ONLY)
                            }
                        }
                    }
                }
            }
    }
}

enum class SqlServerAuthenticationType {
    SqlPassword, ActiveDirectoryPassword, ActiveDirectoryIntegrated, ActiveDirectoryInteractive, ActiveDirectoryMSI,
    ActiveDirectoryServicePrincipal
}

/**
 * An SqlLogger that tracks execution time and reports it to telemetry infrastructure
 * at. Because no instance variables exist, is threadSafe.
 *
 * Logs executions as successful and exceptions as failures. Measures and reports time
 * for both.
 */
class TelemetrySqlLogger(
    private val id: String,
    private val db: String,
    private val instrumentationKey: String?
): SqlLogger {
    override fun logAfterExecution(statementCtx: StatementContext) {
        try {
            val timeDuration = javaTimeDurationBetween(statementCtx.executionMoment, statementCtx.completionMoment)
            track(statementCtx, timeDuration, true)
        } catch (ex: RuntimeException) {
            // Do nothing, let it pass.
        }
    }

    override fun logException(statementCtx: StatementContext, ex: SQLException?) {
        try {
            val timeDuration = javaTimeDurationBetween(statementCtx.executionMoment, statementCtx.exceptionMoment)
            track(statementCtx, timeDuration, false)
        } catch (ex: RuntimeException) {
            // Do nothing, let it pass
        }
    }

    private fun track(statementCtx: StatementContext, duration: java.time.Duration, success: Boolean) {
        val daoClassName = statementCtx.extensionMethod?.type?.let { nameFromClass(it) }
        val daoClassMethod = statementCtx.extensionMethod?.method?.name
        val name = if (daoClassName.isNullOrBlank()) {
            "$id.$db"
        } else {
            "$daoClassName.$daoClassMethod"
        }

    }

    private fun nameFromClass(klass: Class<*>): String? {
        if (klass.enclosingClass == null) {
            return klass.simpleName
        } else {
            return "${nameFromClass(klass.enclosingClass)}.${klass.simpleName}"
        }
    }
}
