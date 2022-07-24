package common.resources

import io.lettuce.core.AbstractRedisAsyncCommands
import io.lettuce.core.AbstractRedisClient
import io.lettuce.core.api.StatefulConnection
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection
import java.io.Closeable

/**
 * Redis Lettuce 6 client.
 */
class RedisLettuce6Client(
    private val client: AbstractRedisClient,
    private val connection: StatefulConnection<String, String>,
    /**
     * Redis host name. Can be used for debugging.
     */
    val redisHostName: String = ""
) : Closeable {
    @Suppress("UNCHECKED_CAST")
            /**
             * asyncCommands of Lettuce 6
             */
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
        connection.close()
        client.shutdown()
    }
}
