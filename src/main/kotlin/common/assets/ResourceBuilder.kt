package common.assets

import com.microsoft.azure.storage.blob.CloudBlobClient
import com.microsoft.azure.storage.blob.CloudBlobContainer
import com.microsoft.azure.storage.table.CloudTable
import com.microsoft.azure.storage.table.CloudTableClient
import common.config.Configuration
import kotlin.math.max

const val CONNECTION_TIMEOUT_DEFAULT = 5000
const val SOCKET_TIMEOUT_DEFAULT = 30000

/**
 * A way of getting clients for various PaaS resources
 *
 * Sample use:
 * val redisClient = ResourceBuilder.redisClient("sessions")
 *      .withTimeout(3000)
 *      .build()
 *
 * @param resourceId id of the resource you want to access
 */
class ResourceBuilder {
    var connectionTimeoutMillis: Int
    var socketTimeoutMillis: Int
    val descriptor: Map<String,String>
    val resourceId: String
    val resourceClass: String

    private constructor(resourceId: String, resourceClass: String) {
        this.resourceId = resourceId
        this.resourceClass = resourceClass
        val key = "resources/$resourceId/$resourceClass"
        this.descriptor = Configuration.getMap(key) ?: throw IllegalArgumentException("No configuration found for $key")
        connectionTimeoutMillis = descriptor["connto"]?.toInt() ?: CONNECTION_TIMEOUT_DEFAULT
        socketTimeoutMillis = descriptor["sockto"]?.toInt() ?: SOCKET_TIMEOUT_DEFAULT

        if (!Configuration.isKubernetesEnv) {
            connectionTimeoutMillis = max(connectionTimeoutMillis, CONNECTION_TIMEOUT_DEFAULT)
            socketTimeoutMillis = max(socketTimeoutMillis, SOCKET_TIMEOUT_DEFAULT)
        }
    }

    fun withConnectionTimeout(connectionTimeoutSecs: Int) : ResourceBuilder {
        connectionTimeoutMillis = connectionTimeoutSecs
        return this
    }

    fun withSocketTimeout(socketTimeoutSecs: Int) : ResourceBuilder {
        socketTimeoutMillis = socketTimeoutSecs
        return this
    }

    fun withTimeout(timeoutMillis: Int) : ResourceBuilder {
        return withSocketTimeout(timeoutMillis).withConnectionTimeout(timeoutMillis)
    }

    companion object {


        /**
         * @return a resource builder for a JDBI client corresponding to [resourceId].
         * For configuration options, see [Jdbi3Builder]
         */
        fun jdbi3(resourceId: String) : Jdbi3Builder {
            return Jdbi3Builder(ResourceBuilder(resourceId, "jdbi3"))
        }

    }
}

data class CloudTableClientProxy(val client: CloudTableClient) {
    fun getTableReference(id: String): CloudTable {
        return if (Configuration.isTestEnvironment)
            client.getTableReference("${Configuration.testRunId}00$id")
        else
            client.getTableReference(id)
    }
}

data class CloudBlobClientProxy(val client: CloudBlobClient) {
    fun getContainerReference(id: String): CloudBlobContainer {
        return if (Configuration.isTestEnvironment)
            client.getContainerReference("${Configuration.testRunId}-$id")
        else
            client.getContainerReference(id)
    }
}
