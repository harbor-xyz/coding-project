package common.assets

import com.lambdaworks.redis.AbstractRedisAsyncCommands
import com.lambdaworks.redis.AbstractRedisClient
import com.lambdaworks.redis.api.StatefulConnection
import com.lambdaworks.redis.api.StatefulRedisConnection
import com.lambdaworks.redis.cluster.api.StatefulRedisClusterConnection
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import redis.clients.jedis.*
import redis.clients.jedis.exceptions.JedisConnectionException
import java.io.Closeable
import java.net.URI

/**
 * Simple wrapper over Jedis Client
 */
class RedisClient(shardInfo: JedisShardInfo) : Jedis(shardInfo) {

}

interface RedisClientPool {
    /**
     * This function helps you ensure that redis client connect is closed after use
     *
     * Sample use
     *
     * val pool = ... // assuming the redis client pool has been initialized
     * val user = pool.withClient({ it['user'] })
     *
     * pool.withClient({ client ->
     *      // do some processing here with it
     * }
     */
    fun<R> withClient(block: (client: JedisCommands) -> R) : R
}

/**
 * Pooled redis client. In order to ensure closure of connections, a helper withClient() function is exposed
 *
 * @param uri redis server uri
 * @param timeout timeout in millis
 * @param maxReconnectAttempts maximum number of times
 */
class RedisClientPoolImpl(uri: URI, timeout: Int, val maxReconnectAttempts: Int=4)
    : JedisPool(poolConfig, uri, timeout), RedisClientPool {
    companion object {
        private val poolConfig = GenericObjectPoolConfig();
        init {
            poolConfig.testOnBorrow = true
        }
    }

    /**
     * This function helps you ensure that redis client connect is closed after use
     *
     * Sample use
     *
     * val pool = ... // assuming the redis client pool has been initialized
     * val user = pool.withClient({ it['user'] })
     *
     * pool.withClient({ client ->
     *      // do some processing here with it
     * }
     */
    override fun<R> withClient(block: (client: JedisCommands) -> R) : R {
        val conn = this.resource
        try {
            var savedException: Exception? = null
            for (i in 0..maxReconnectAttempts) {
                try {
                    return block(conn)
                } catch (ex: JedisConnectionException) {
                    savedException = ex
                    try {conn.disconnect()} catch (ex: Exception) {}
                    try {conn.connect()} catch (ex: Exception) {}
                }
            }
            throw savedException!!
        } finally {
            conn.close()
        }
    }
}

/**
 * Cluster redis client. In order to ensure closure of connections, a helper withClient() function is exposed
 *
 * @param host redis cluster host
 * @param port redis cluster port
 * @param connTimeout connection timeout in millis
 * @param soTimeout socket timeout in millis
 * @param maxReconnectAttempts maximum number of times
 * @param password password to the redis cluster
 */
class RedisClusterPool(host: String, port: Int, connTimeout: Int, soTimeout: Int, maxReconnectAttempts: Int, password: String)
    : JedisCluster(setOf(HostAndPort(host, port)), connTimeout, soTimeout, maxReconnectAttempts, password, poolConfig)
    , RedisClientPool {

    companion object {
        private val poolConfig = GenericObjectPoolConfig()
        init {
            poolConfig.testOnBorrow = true
        }
    }
    /**
     * This function helps you ensure that redis client connect is closed after use
     *
     * Sample use
     *
     * val pool = ... // assuming the redis client pool has been initialized
     * val user = pool.withClient({ it['user'] })
     *
     * pool.withClient({ client ->
     *      // do some processing here with it
     * }
     */
    override fun<R> withClient(block: (client: JedisCommands) -> R) : R {
        return block(this)
    }
}

class RedisLettuceClient(private val client: AbstractRedisClient,
                         private val connection: StatefulConnection<String, String>,
                         val redisHostName: String = ""): Closeable {

    @Suppress("UNCHECKED_CAST")
    val asyncCommands: AbstractRedisAsyncCommands<String, String> by lazy {
        when (connection) {
            is StatefulRedisClusterConnection ->
                connection.async() as AbstractRedisAsyncCommands<String, String>
            is StatefulRedisConnection ->
                connection.async() as AbstractRedisAsyncCommands<String, String>
            else ->
                throw RuntimeException("")
        }
    }

    override fun close() {
        asyncCommands.close()
        connection.close()
        client.shutdown()
    }
}


