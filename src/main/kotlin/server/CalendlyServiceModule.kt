package server

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.AbstractModule
import server.CalendlyCoreModule
import java.util.function.Consumer

class CalendlyServiceModule(private val closeableConsumer: Consumer<AutoCloseable>, private val objectMapper: ObjectMapper) : AbstractModule() {

    override fun configure() {
        install(CalendlyCoreModule(closeableConsumer, objectMapper))
    }
}