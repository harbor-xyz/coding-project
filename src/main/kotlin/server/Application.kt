package server

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.google.inject.Module
import server.resources.SampleResource
import common.DropwizardApplication
import common.register
import common.LifeCycleObjectRepo
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import io.dropwizard.jersey.jackson.JsonProcessingExceptionMapper
import io.dropwizard.jersey.setup.JerseyEnvironment
import io.dropwizard.lifecycle.Managed
import java.util.function.Consumer

class CalendlyApplication : DropwizardApplication<CalendlyProjectConfiguration>() {

    override fun getGuiceModules(configuration: CalendlyProjectConfiguration, environment: Environment): List<Module> {
        val closeableConsumer = Consumer<AutoCloseable> { addCloseable(environment, it) }
        return listOf(
            CalendlyServiceModule(closeableConsumer, environment.objectMapper)
        )
    }

    override fun getJacksonModules() = listOf(JavaTimeModule())

    override fun getResourceClasses() = listOf(
        SampleResource::class.java
    )

    override fun initializeAdditional(bootstrap: Bootstrap<CalendlyProjectConfiguration>) {
        bootstrap.objectMapper
            .configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
            .configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
    }

    override fun runAdditional(configuration: CalendlyProjectConfiguration, environment: Environment) {
        environment.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        environment.register(LifeCycleObjectRepo.global())

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

