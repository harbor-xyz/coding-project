package common

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.google.inject.Module
import common.server.DropwizardApplication
import common.server.LifeCycleObjectRepo
import common.server.register
import io.dropwizard.jersey.jackson.JsonProcessingExceptionMapper
import io.dropwizard.jersey.setup.JerseyEnvironment
import io.dropwizard.lifecycle.Managed
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import org.eclipse.jetty.servlets.CrossOriginFilter
import common.resources.UserAvailabilityResource
import common.resources.UserResource
import java.util.*
import java.util.function.Consumer
import javax.servlet.DispatcherType

class CalendlyApplication : DropwizardApplication<CalendlyProjectConfiguration>() {

    override fun getGuiceModules(configuration: CalendlyProjectConfiguration, environment: Environment): List<Module> {
        val closeableConsumer = Consumer<AutoCloseable> { addCloseable(environment, it) }
        return listOf(
            CalendlyServiceModule(closeableConsumer, environment.objectMapper)
        )
    }


    override fun getResourceClasses() = listOf(
        UserAvailabilityResource::class.java,
        UserResource::class.java
    )

    override fun initializeAdditional(bootstrap: Bootstrap<CalendlyProjectConfiguration>) {
        bootstrap.objectMapper
            .configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
            .configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
    }

    override fun runAdditional(configuration: CalendlyProjectConfiguration, environment: Environment) {
        environment.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        environment.register(LifeCycleObjectRepo.global())

      // MANI added this for local testing
        val cors = environment.servlets().addFilter("CORS", CrossOriginFilter::class.java)

        // Configure CORS parameters

        // Configure CORS parameters
        // added because of localhost testing!
        cors.setInitParameter("allowedOrigins", "*")
        cors.setInitParameter("allowedHeaders", "X-Requested-With,Content-Type,Accept,Origin")
        cors.setInitParameter("allowedMethods", "POST,OPTIONS,GET,PUT,DELETE,HEAD")

        // Add URL mapping
        cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType::class.java), true, "/*")
        environment.servlets().addFilter("CORS", CrossOriginFilter::class.java)

        environment.jersey().registerExceptionMappers()
    }

    private fun JerseyEnvironment.registerExceptionMappers() {
        register(JsonProcessingExceptionMapper(true))
    }
}

fun main(args: Array<String>) {
    CalendlyApplication().startWithArgs(args)
}

private fun addCloseable(environment: Environment, closeable: AutoCloseable) {
    environment.lifecycle().manage(object : Managed {
        override fun start() {
            // nothing to do.
        }

        override fun stop() {
            closeable.close()
        }
    })
}

