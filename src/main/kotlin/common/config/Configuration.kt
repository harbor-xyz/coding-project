package common.config

import org.skife.jdbi.v2.DBI
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Configuration manager for  services
 *
 * Usage: Configuration.getInt("mykey")
 *
 * sql password:  sqlPassword@
 */
object Configuration {
    private val logger = LoggerFactory.getLogger(Configuration::class.java)

    private val k8sPodInfoDir = File("/etc/podinfo")
    val isKubernetesEnv = k8sPodInfoDir.isDirectory

    val azureEnv: String? = System.getenv("AZURE_ENV")

    var aadSPId: String? = System.getenv("AAD_SP_ID")
    var aadSPSecret: String? = System.getenv("AAD_SP_SECRET")
    var configDbHost: String? = System.getenv("CONFIG_DB_HOST")
    var configDbName: String? = System.getenv("CONFIG_DB_NAME")
    var configDbUser: String? = System.getenv("SECRET_CONFIG_DB_USER")
    var configDbPassword: String? = System.getenv("SECRET_CONFIG_DB_PASSWORD")

    /*
     * Initialize azure key vault client.
     *
     * KeyVault client uses client ID and secret based authentication which can be setup in following ways:
     *  - By setting up following environment variables:
     *      - AZURE_KEYVAULT_CLIENT_ID
     *      - AZURE_KEYVAULT_CLIENT_SECRET
     *      - AZURE_KEYVAULT_TENANT_ID
     *      - AZURE_KEYVAULT_VAULT
     *
     *  - By setting up following java system properties (-Dazure_keyvault_client_id=)
     *      - azure_keyvault_client_id
     *      - azure_keyvault_client_secret
     *      - azure_keyvault_tenant_id
     *      - azure_keyvault_vault
     *
     *  - By adding rows in config table with following config names (and corresponding values):
     *      - azure_keyvault_client_id
     *      - azure_keyvault_client_secret
     *      - azure_keyvault_tenant_id
     *      - azure_keyvault_vault
     *
     * If either of the parameters required are missing, this client is not initialized
     * and the variable below is set to null (with a logged warning).
     */
    /**
     * Get config value for key vault from
     *  - Java system property. If it does not exist then,
     *  - Environment variable with upper case name. If it does not exist then,
     *  - Config table. If it does not exist then,
     */
    private fun getKeyVaultClientConfig(param: String): String? {
        return (System.getProperty(param) ?: System.getenv(param.toUpperCase()))
    }

    val serviceName: String by lazy {
        val defaultName = "unknown"
        val podLabelsFile = File(k8sPodInfoDir, "labels")
        if (podLabelsFile.exists()) {
            podLabelsFile.readLines()
                .find { it.startsWith("app.kubernetes.io/name=") || it.startsWith("app=") }
                ?.substringAfter('=')
                ?.trim('"')
                ?: defaultName
        } else {
            defaultName
        }
    }

    val serviceVersion: String by lazy {
        val defaultName = "unknown"
        val podLabelsFile = File(k8sPodInfoDir, "labels")
        if (podLabelsFile.exists()) {
            podLabelsFile.readLines()
                .find { it.startsWith("app.kubernetes.io/version=") || it.startsWith("version=") }
                ?.substringAfter('=')
                ?.trim('"')
                ?: defaultName
        } else {
            defaultName
        }
    }

    private val environment = getEnv()
    val applicationName = getApplicationNameInternal()

    val isLocalEnvironment: Boolean
        get() = environment == "local"

    val isProdEnvironment: Boolean
        get() = environment == "prod"

    val testRunId: String get() =
        if(isTestEnvironment) environment
        else throw IllegalStateException("Call to Configuration.testRunId in a non-test env")

    val isTestEnvironment: Boolean
        get() = environment.startsWith("test")

    private val configMap: MutableMap<String,String> by lazy {
        buildConfigMap()
    }

    private val dbi = DBI(getJdbcUrlForEnv())

    init {
        logger.info("All configurations using the environment: $environment")
    }

    fun refreshKey(key: String) {
        if (configMap.containsKey(key)) {
            dbi.open().use { handle ->
                logger.debug("Reading configuration for key $key")
                handle.select("SELECT name, value FROM config WHERE name = ?", key)
                    .let { outputList ->
                        if (outputList.size == 1 && outputList[0]["name"] == key) {
                            val localConfigMap = mutableMapOf<String, String>().apply { this.putAll(configMap) }
                            localConfigMap[key] = (outputList[0]["value"] ?: throw Exception("Unable to fetch latest value of config key $key")) as String
                            configMap.clear()
                            configMap.putAll(localConfigMap.mapValues { substituteConfigValue(it.value) })
                        }
                    }
            }
        }
    }

    fun refresh() {
        logger.info("Refresh config map called")
        this.configMap.apply { clear() }.apply { putAll(buildConfigMap()) }
        logger.info("Config map refreshed")
    }

    private fun buildConfigMap(): MutableMap<String,String>{
        logger.info("Building config map")
        dbi.open().use { handle ->
            // Read all properties
            logger.debug("Reading configuration from database")
            val map = mutableMapOf<String, String>()
            handle.createQuery("SELECT name,value from config").forEach { map[it["name"] as String] = it["value"] as String }
            return map.mapValues {
                substituteConfigValue(it.value)
            }.toMutableMap()
            //logger.info("Loaded ${map.size} entries into the configuration database")
        }
    }

    /**
     * Substitute value in
     */
    private fun substituteConfigValue(value: String): String {
        return if (value.contains(envVarRegex)) {
            substituteConfigVars(value)
        } else if (value.contains(azureKeyVaultRegex)) {
            logger.warn("Azure keyvault value - $value is setup in config table but keyvault credentials are not provided. Hence, using same value as configuration.")
            value

        } else {
            value
        }
    }

    fun initWithMap(vararg pairs: Pair<String, String>) {
        this.configMap.clear()
        this.configMap.putAll(mapOf(*pairs).mapValues { substituteConfigValue(it.value) })
    }

    /*
     * Enumerate all keys starting with the specified prefix
     */
    fun enumerateKeys(prefix: String): List<String> {
        return this.configMap.keys.filter { it.startsWith(prefix) }.sorted()
    }

    fun get(name: String, default: String? = null): String? {
        return configMap[name] ?: default
    }

    fun getInt(name: String, default: Int? = null): Int? {
        return configMap[name]?.toInt() ?: default
    }

    fun getLong(name: String, default: Long? = null): Long? {
        return configMap[name]?.toLong() ?: default
    }

    fun getDouble(name: String, default: Double? = null): Double? {
        return configMap[name]?.toDouble() ?: default
    }

    fun getBoolean(name: String, default: Boolean? = null): Boolean? {
        return configMap[name]?.toBoolean() ?: default
    }

    fun getMap(name: String): Map<String, String>? {
        val map: MutableMap<String, String> = mutableMapOf()
        configMap[name]?.split(";")?.forEach {
            val arr = it.split('=', limit = 2)
            map[arr[0]] = arr[1]
        }
        return if (map.isNotEmpty()) map else null
    }

    /**
     * Get the current environment I'm running under
     */
    private fun getEnv(): String {
        if (isKubernetesEnv) {
            // If I'm running under k8s, use podinfo to return the environment
            return File(k8sPodInfoDir, "namespace").readText().trim()
        }
        return System.getProperty("calendly.env", "local")
    }

    /**
     * Get the application name
     */
    private fun getApplicationNameInternal(): String {
        return if (isKubernetesEnv) {
            File(k8sPodInfoDir, "name").readText().trim()
        }
        else {

            System.getenv("MESOS_TASK_ID") ?: "local"
        }
    }

    private fun getJdbcUrlForEnv(): String {
        val prefix = "jdbc:sqlserver://"
        val suffix = if(isTestEnvironment) {
            "encrypt=false;loginTimeout=30;"
        } else {
            "encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;"
        }

        val url = prefix +
                "\${CONFIG_DB_HOST};" +
                "database=\${CONFIG_DB_NAME};" +
                "user=\${SECRET_CONFIG_DB_USER};" +
                "password=\${SECRET_CONFIG_DB_PASSWORD};" +
                suffix

        return substituteConfigValue(url)
    }


    /**
     * Returns the substituted string after replacing any config vars specified in the form of ${XYZ}
     */
    private fun substituteConfigVars(input: String): String {
        return input.replace(envVarRegex) { match ->
            if (isKubernetesEnv) {
                System.getenv(match.groupValues[1]) ?: match.groupValues[0]
            }
            else {
                getHardcodedEnv(match.groupValues[1]) ?: match.groupValues[0]
            }
        }
    }


    private fun getHardcodedEnv(name: String): String? {
        if(configDbHost != null && configDbName != null && configDbUser != null && configDbPassword != null) {
            // It will use the supplied config DB parameters for connection to config table.
            // TODO: Remove this once we stop supporting local application run to connect to prod db.
            return when (name) {
                "CONFIG_DB_HOST" -> configDbHost
                "CONFIG_DB_NAME" -> configDbName
                "SECRET_CONFIG_DB_USER" -> configDbUser
                "SECRET_CONFIG_DB_PASSWORD" -> configDbPassword
                else -> null
            }
        }
        if (environment == "prod" || environment == "dev") {
            return when (name) {
                "CONFIG_DB_HOST" -> "common-test-mani.database.windows.net:1433"
                "CONFIG_DB_NAME" -> "common-test"
                "SECRET_CONFIG_DB_USER" -> "common-admin"
                "SECRET_CONFIG_DB_PASSWORD" -> "sqlPassword@"
                else -> null
            }
        }
        // For local env - this should be the only one remaining after k8s migration
        return when (name) {
            "CONFIG_DB_HOST" -> "common-test-mani.database.windows.net:1433"
            "CONFIG_DB_NAME" -> "common-test"
            "SECRET_CONFIG_DB_USER" -> "common-admin"
            "SECRET_CONFIG_DB_PASSWORD" -> "sqlPassword@"
            else -> null
        }
    }
}

private val envVarRegex = Regex("\\\$\\{([a-zA-Z0-9_]+)}")

private val azureKeyVaultRegex = Regex("\\\$akv\\{([a-zA-Z0-9-]+)}")