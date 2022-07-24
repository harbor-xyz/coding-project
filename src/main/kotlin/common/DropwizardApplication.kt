package common

import com.codahale.metrics.health.HealthCheck
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.google.inject.*
import io.dropwizard.Application
import io.dropwizard.Bundle
import io.dropwizard.Configuration
import io.dropwizard.lifecycle.Managed
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Base class for all dropwizard applications. This sets up instrumentation and environment in a common way
 * across all services.
 *
 * Applications should explore the following resources/customisations
 *     Use {@see injector} to access the guice injector
 *     Override {@see getGuiceModules} to register custom guice modules
 *     Override {@see getJacksonModules} to register custom jackson modules (Kotlin is already configured)
 *     Override {@see getDropwizardBundles} to register custom dropwizard bundles (Telemetry is pre-configured)
 *     Override {@see getResourceClasses} to have them automatically configured in Jersey via guice
 *     Override {@see getHealthchecks} for custom health check (config db based health check is pre-configured)
 *     Override {@see initializeAdditional} for additional steps in Dropwizard's initialize() step
 *     Override {@see runAdditional} for additional steps in Dropwizard's run() step
 *     Override {@see getMetricsFilter} for customisations to the {@see MetricsFilter} instance, such as bucket sizes
 *
 * Applications would
 */
@Suppress("unused")
abstract class DropwizardApplication<T : Configuration> :  Application<T>() {
    /**
     * Override this to provide additional guice modules to register
     */
    protected open fun getGuiceModules(configuration: T, environment: Environment): List<Module> = emptyList()

    /**
     * Override this to provide additional Jackson modules to register
     */
    protected open fun getJacksonModules(): List<com.fasterxml.jackson.databind.Module> = emptyList()

    /**
     * Override this to provide additional dropwizard bundles to register
     */
    protected open fun getDropwizardBundles(): List<Bundle> = emptyList()

    /**
     * Override this to specify dropwizard resource classes. Instances will be sourced via {@see injector}
     */
    protected open fun getResourceClasses(): List<Class<*>> = emptyList()

    /**
     * Get list of additional healthchecks to register
     */
    protected open fun getHealthchecks(): List<Pair<String, HealthCheck>> = emptyList()

    /**
     * Override this method to call any additional steps during bootstrap initialize
     */
    protected open fun initializeAdditional(bootstrap: Bootstrap<T>) {}

    /**
     * Override this to call any additional steps at the end of bootstrap run() method
     */
    protected open fun runAdditional(configuration: T, environment: Environment) {}

    /**
     * Override if you need to configure [UfApplicationListener], the place where Telemetry information is captured
     */
    protected open fun createUfApplicationListener(): UfApplicationListener = UfApplicationListener()

    override fun getName(): String = common.config.Configuration.serviceName

    /**
     * Initialize bootstrap. You cannot override this. Use {@see initializeAdditional} for additional steps
     */
    final override fun initialize(bootstrap: Bootstrap<T>) {
        // Register bundles
        val bundles = getDropwizardBundles()

        //disabling fail on unknown properties
        bootstrap.objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

        // Register jackson modules
        bootstrap.objectMapper.registerKotlinModule()
        bootstrap.objectMapper.registerModules(getJacksonModules())

        // Register healthchecks

        getHealthchecks().plus("config" to object: HealthCheck() {
            override fun check(): Result {
                return try {
                    val resources = common.config.Configuration.enumerateKeys("resources/")
                    if (resources.isEmpty()) Result.unhealthy("Failed to load config") else Result.healthy()
                } catch (ex: Exception) {
                    logger.error("config healthcheck failed", ex)
                    Result.unhealthy(ex)
                }
            }
        }).forEach { bootstrap.healthCheckRegistry.register(it.first, it.second) }


        // Call any additional initialization steps
        initializeAdditional(bootstrap)
    }

    /**
     * Start server. You cannot override this. To call any additional steps, {@see runAdditional}
     */
    final override fun run(configuration: T, environment: Environment) {
        // Create Guice injector
        _injector = Guice.createInjector(LifecycleAwareModule(object: AbstractModule() {
            override fun configure() {
                getGuiceModules(configuration, environment).forEach { install(it) }
            }

            @Provides fun getYamlMapper() : YAMLMapper = yamlObjectMapper
        }))

        //val collectorRegistry = PrometheusClient.collectorRegistry


        // Register metrics filter for http requests
        environment.jersey().register(ServletRequestInjectionFilter::class.java)
        //environment.jersey().register(createUfApplicationListener())

        //register content length interceptor
        environment.jersey().register(ResponseContentLengthInterceptor::class.java)

        // Register hooks to register and unregister collectors with the registry at the right time
        environment.lifecycle().manage(object: Managed {
            // Register all the collectors when server is starting
            override fun start() {
                val server = environment.applicationContext.server

                logger.debug("Registered QueuedThreadPool statistics collector")
            }

            // Remove all the collectors when server is stopping
            override fun stop() {
                logger.info("Unregistering Prometheus Collectors")

            }
        })


        // Register resource classes
        getResourceClasses().forEach { environment.jersey().register(injector.getInstance(it)) }


        // Call any additional steps
        runAdditional(configuration, environment)
    }

    /**
     * Start the service using the provided application object
     */
    @Suppress("unused")
    fun startWithArgs(args: Array<String>) {
        if (args.isNotEmpty()) {
            run(*args)
        }
        else {
            val configFile = File.createTempFile("uf-service", ".yml")

            // Write a temp configuration file, after having eliminated the dcos section
            configFile.writeText(
                this.javaClass.getResourceAsStream("/server-config.yml").bufferedReader()
                    .readText()
                    .replace("(?s)#exclude-start.+#exclude-end".toRegex(), "")
                    .split(SERVER_CONFIG_KUBERNETES_SECTION)[0]
                    .replace(Regex(pattern = "(?:^|\n)\\w+-service:.*?\n(\\w+):",
                        options = setOf(RegexOption.MULTILINE, RegexOption.DOT_MATCHES_ALL)),
                        replacement = "$1:"
                    )
            )
            configFile.deleteOnExit()

            // Run the server on this new config
            run("server", configFile.absolutePath)
        }
    }

    private lateinit var _injector: Injector
    @Suppress("WeakerAccess")
    val injector: Injector get() = _injector



    protected val logger: Logger by lazy { LoggerFactory.getLogger(name) }
    @Suppress("WeakerAccess")
    protected val yamlObjectMapper = YAMLMapper().registerKotlinModule() as YAMLMapper
}


private val SERVER_CONFIG_KUBERNETES_SECTION = Regex("(dcos|kubernetes):")