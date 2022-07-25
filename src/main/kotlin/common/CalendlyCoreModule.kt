package common

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.AbstractModule
import com.google.inject.Singleton
import common.assets.ResourceBuilder
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.sqlobject.SqlObjectPlugin
import org.jdbi.v3.sqlobject.kotlin.KotlinSqlObjectPlugin
import common.core.dao.CourseDao
import common.core.dao.UserAvailabilityDao
import common.core.dao.UserDao
import java.util.function.Consumer

class CalendlyCoreModule(
    private val closeableConsumer: Consumer<AutoCloseable>, private val objectMapper: ObjectMapper
    ) : AbstractModule() {

    override fun configure() {
        val jdbi = getJdbi()
        bind(Jdbi::class.java).toInstance(jdbi)
        bind(ObjectMapper::class.java).toInstance(objectMapper)
        bind(CourseDao::class.java).toInstance(jdbi.onDemand(CourseDao::class.java))
        bind(UserAvailabilityDao::class.java).toInstance(jdbi.onDemand(UserAvailabilityDao::class.java))
        bind(UserDao::class.java).toInstance(jdbi.onDemand(UserDao::class.java))
    }

    @Singleton
    private fun getJdbi(): Jdbi =
        ResourceBuilder.jdbi3("common-test")
            .build()
            .also { jdbi ->
                jdbi.installPlugin(SqlObjectPlugin())
                jdbi.installPlugin(KotlinPlugin())
                jdbi.installPlugin(KotlinSqlObjectPlugin())
            }

}