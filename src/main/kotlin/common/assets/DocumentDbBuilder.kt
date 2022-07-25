package common.assets

import com.microsoft.azure.cosmosdb.ConsistencyLevel
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient
import com.microsoft.azure.documentdb.ConnectionMode
import com.microsoft.azure.documentdb.ConnectionPolicy
import com.microsoft.azure.documentdb.ConsistencyLevel as docConsistencyLevel
import com.microsoft.azure.documentdb.DocumentClient
import common.config.Configuration
import common.server.LifeCycleObjectRepo
import java.util.concurrent.CompletableFuture
//import com.microsoft.azure.documentdb.rx.AsyncDocumentClient
import java.util.concurrent.ConcurrentHashMap

@Suppress("MemberVisibilityCanBePrivate")
class DocumentDBClientBuilder(resourceBuilder: ResourceBuilder) {
    val documentDBHost = resourceBuilder.descriptor["uri"]!!
    val accessKey = resourceBuilder.descriptor["key"]!!
    val maxPoolSize = resourceBuilder.descriptor["maxPoolSize"]?.toInt()
    val enableEndpointDiscovery = resourceBuilder.descriptor["enableEndpointDiscovery"]?.toBoolean() ?: false
    val useMultipleWriteLocations = resourceBuilder.descriptor["useMultipleWriteLocations"]?.toBoolean() ?: false

    fun <R> with(block: (DocumentClient) -> R): R {
        return block(buildDocumentClient())
    }


    fun <R> async(block: (AsyncDocumentClient) -> CompletableFuture<R>): CompletableFuture<R> {
        return block(buildAsyncClient())
    }


    fun buildPreferredDocumentConnectionPolicy(): ConnectionPolicy {
        val connectionPolicy = ConnectionPolicy()
        // enforce connectionMode to Direct
        connectionPolicy.connectionMode = ConnectionMode.DirectHttps
        maxPoolSize?.let {
            connectionPolicy.maxPoolSize = it
        }
        if (enableEndpointDiscovery) {
            connectionPolicy.enableEndpointDiscovery = true
        }
        if (useMultipleWriteLocations) {
            // enforce enableEndpointDiscovery for availability
            connectionPolicy.enableEndpointDiscovery = true
            getEnvPreferredLocations()?.let {
                connectionPolicy.preferredLocations = it
            }
        }
        return connectionPolicy
    }

    fun buildPreferredCosmosConnectionPolicy(): ConnectionPolicy {
        val connectionPolicy = ConnectionPolicy()
        // always enforce connectionMode to Direct
        connectionPolicy.connectionMode = ConnectionMode.DirectHttps
        maxPoolSize?.let {
            connectionPolicy.maxPoolSize = it
        }
        if (enableEndpointDiscovery) {
            connectionPolicy.enableEndpointDiscovery = true
        }
        if (useMultipleWriteLocations) {
            // enforce enableEndpointDiscovery for availability
            connectionPolicy.enableEndpointDiscovery = true
            getEnvPreferredLocations()?.let {
                connectionPolicy.preferredLocations = it
            }
        }
        return connectionPolicy
    }


    fun buildDocumentClient(): DocumentClient {
        return buildDocumentClient(documentDBHost, accessKey)
    }


    fun buildAsyncClient(): AsyncDocumentClient {
        return buildAsyncClient(documentDBHost, accessKey)
    }

    fun buildAsyncClient(consistencyLevel: ConsistencyLevel): AsyncDocumentClient {
        return buildAsyncClient(documentDBHost, accessKey, consistencyLevel)
    }

    fun buildAsyncClientV2(consistencyLevel: ConsistencyLevel): AsyncDocumentClient {
        val connectionPolicy = ConnectionPolicy()
        connectionPolicy.enableEndpointDiscovery = enableEndpointDiscovery
        maxPoolSize?.let {
            connectionPolicy.maxPoolSize = it
        }
        return buildAsyncClient(documentDBHost, accessKey, consistencyLevel, )
    }


    companion object {
        val clientMap = ConcurrentHashMap<String, DocumentClient>()
        val asyncClientMap = ConcurrentHashMap<String, AsyncDocumentClient>()

        // NB: if using, also force enableEndpointDiscovery for availability
        fun getEnvPreferredLocations(): List<String>? {
            return when (Configuration.azureEnv) {
                "sin0" -> listOf("South India", "Central India")
                "cen0" -> listOf("Central India", "South India")
                else -> null
            }
        }

        private fun key(uri: String, accessKey: String) = "$uri::$accessKey"

        private fun buildDocumentClient(uri: String, accessKey: String): DocumentClient {
            val key = key(uri, accessKey)
            return clientMap.computeIfAbsent(key) {
                DocumentClient(uri, accessKey, ConnectionPolicy.GetDefault(), docConsistencyLevel.Session).apply {
                    LifeCycleObjectRepo.global().register(this)
                }
            }
        }



        private fun buildAsyncClient(uri: String, accessKey: String, consistencyLevel: ConsistencyLevel = ConsistencyLevel.Session): AsyncDocumentClient {
            val key = key(uri, accessKey)
            return asyncClientMap.computeIfAbsent(key) {
                AsyncDocumentClient.Builder()
                    .withServiceEndpoint(uri)
                    .withMasterKey(accessKey)
                    .withConsistencyLevel(consistencyLevel)
                    .build()
                    .apply {
                        LifeCycleObjectRepo.global().register(AutoCloseable { this.close() })
                    }
            }
        }
        private fun buildAsyncClient(uri: String, accessKey: String, consistencyLevel: ConsistencyLevel = ConsistencyLevel.Session, connectionPolicy: com.microsoft.azure.cosmosdb.ConnectionPolicy): AsyncDocumentClient {
            val key = key(uri, accessKey)
            return asyncClientMap.computeIfAbsent(key) {
                AsyncDocumentClient.Builder()
                    .withServiceEndpoint(uri)
                    .withMasterKey(accessKey)
                    .withConsistencyLevel(consistencyLevel)
                    .withConnectionPolicy(connectionPolicy)
                    .build()
                    .apply {
                        LifeCycleObjectRepo.global().register(AutoCloseable { this.close() })
                    }
            }
        }

    }
}
