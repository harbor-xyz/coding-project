package common.resources

import com.lambdaworks.redis.RedisURI
import com.lambdaworks.redis.cluster.ClusterClientOptions
import com.lambdaworks.redis.cluster.ClusterTopologyRefreshOptions
import com.lambdaworks.redis.cluster.RedisClusterClient
import com.lambdaworks.redis.resource.DefaultClientResources
import io.lettuce.core.SocketOptions
import io.lettuce.core.resource.ClientResources
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import org.slf4j.LoggerFactory
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisShardInfo
import java.net.URI
import java.time.Duration
import java.util.concurrent.TimeUnit

/**
 * if serverName is provided then use it as host else use id for creating host name
 */
fun getRedisHostFromId(descriptor: Map<String, String>): String =
    descriptor.getOrDefault("serverName", "${descriptor["id"]!!}.redis.cache.windows.net")

/**
 * Resource Builder for redis clients. Use [ResourceBuilder].redisClient as the entry point
 *
 * This assumes a Map type for configuration, with the following properties:
 * 1. id - redis server id
 * 2. password
 * 3. port (optional, default = 6379)
 * 4. db index (optional, default = 0)
 */
class RedisClientBuilder(val builder: ResourceBuilder) {

    companion object {
        /**
         * Logger for RedisClientBuilder
         */
        val logger = LoggerFactory.getLogger(RedisClientBuilder::class.java)
    }
    /**
     * Returns a [RedisClient], using the configuration specified in the builder
     *
     * NOTE: This client is not thread safe. For that, you need to use buildPool(). Also, remember to close the client
     */
    @Deprecated("Use buildPool instead")
    fun build() : RedisClient {
        val password = builder.descriptor["password"]!!
        val host = getRedisHostFromId(builder.descriptor)
        val port = builder.descriptor["port"]?.toInt() ?: 6379
        val db = builder.descriptor["db"]?.toInt() ?: 0
        val shard = JedisShardInfo(URI("redis", ":$password", host, port, "/$db", null, null))
        shard.soTimeout = builder.socketTimeoutMillis
        shard.connectionTimeout = builder.connectionTimeoutMillis
        return RedisClient(shard)
    }

    /**
     * Returns a thread safe pooled clients, which can then be used to get individual clients
     */
    fun buildPool() : RedisClientPool {
        val password = builder.descriptor["password"]!!
        val host = getRedisHostFromId(builder.descriptor)
        val port = builder.descriptor["port"]?.toInt() ?: 6379
        val db = builder.descriptor["db"]?.toInt() ?: 0
        val timeout = Math.min(builder.socketTimeoutMillis, builder.connectionTimeoutMillis)
        val isCluster = builder.descriptor["isCluster"]?.toBoolean() ?: false
        return if (isCluster)
            RedisClusterPool(host, port, builder.connectionTimeoutMillis, builder.socketTimeoutMillis, 5, password)
        else RedisClientPoolImpl(URI("redis", ":$password", host, port, "/$db", null, null), timeout)
    }

    fun buildLettuceClient(): RedisLettuceClient {

        val timeout = Math.min(builder.socketTimeoutMillis, builder.connectionTimeoutMillis).toLong()
        val isCluster = builder.descriptor["isCluster"]?.toBoolean() ?: false
        val ioThreadPoolSize = builder.descriptor["ioThreads"]?.toInt()

        val redisUri =
            RedisURI.Builder
                .redis(getRedisHostFromId(builder.descriptor))
                .withPort(builder.descriptor["port"]?.toInt() ?: 6379)
                .withDatabase(builder.descriptor["db"]?.toInt() ?: 0)
                .withPassword(builder.descriptor["password"]!!)
                .withTimeout(timeout, TimeUnit.MILLISECONDS)
                .build()

        val clientResource = DefaultClientResources.builder().also { builder ->
            ioThreadPoolSize?.let {
                builder.ioThreadPoolSize(it)
            }
        }.build()

        val clientAndConnection =
            if (isCluster) {
                val topologyRefreshOptions = ClusterTopologyRefreshOptions.builder()
                    .dynamicRefreshSources(builder.descriptor["dynamicRefreshSources"]?.toBoolean() ?: true)
                    .refreshPeriod(builder.descriptor["refreshPeriodSeconds"]?.toLong()
                        ?: 600, TimeUnit.SECONDS)
                    .enablePeriodicRefresh(builder.descriptor["enablePeriodicRefresh"]?.toBoolean() ?: false)
                    .apply {
                        if (builder.descriptor["enableAdaptiveRefresh"]?.toBoolean() ?: true)
                            enableAllAdaptiveRefreshTriggers()
                    }
                    .build()

                val clientOptions = ClusterClientOptions.builder()
                    .validateClusterNodeMembership(builder.descriptor["validateClusterNodeMembership"]?.toBoolean()
                        ?: false)
                    .topologyRefreshOptions(topologyRefreshOptions)
                    .build()

                RedisClusterClient
                    .create(clientResource, redisUri).let {
                        it.setOptions(clientOptions)
                        it.setDefaultTimeout(timeout, TimeUnit.MILLISECONDS)
                        it to it.connect()
                    }
            }
            else {
                com.lambdaworks.redis.RedisClient.create(clientResource, redisUri).let {
                    it to it.connect()
                }
            }

        return RedisLettuceClient(
            clientAndConnection.first,
            clientAndConnection.second,
            builder.descriptor["id"] ?: ""
        )
    }

    /**
     * Lettuce 6 Client builder
     */
    fun buildLettuce6Client(): RedisLettuce6Client {
        val isCluster = builder.descriptor["isCluster"]?.toBoolean() ?: false
        val ioThreadPoolSize = builder.descriptor["ioThreads"]?.toInt()
        val redisUri = io.lettuce.core.RedisURI.Builder
            .redis(getRedisHostFromId(builder.descriptor))
            .withPort(builder.descriptor["port"]?.toInt() ?: 6379)
            .withDatabase(builder.descriptor["db"]?.toInt() ?: 0)
            .withPassword( builder.descriptor["password"]!!)
            .build()

        val clientAndConnection = if (isCluster) {
            val clientResource = ClientResources.builder().build()
            val dynamicRefreshSources = builder.descriptor["dynamicRefreshSources"]?.toBoolean() ?: true
            val refreshPeriodSeconds = builder.descriptor["refreshPeriodSeconds"]?.toLong() ?: 600L
            val enablePeriodicRefresh = builder.descriptor["enablePeriodicRefresh"]?.toBoolean() ?: false
            val enableAllAdaptiveRefreshTriggers = builder.descriptor["enableAdaptiveRefresh"]?.toBoolean() ?: true
            val adaptiveRefreshTriggerTimeout =
                builder.descriptor["adaptiveRefreshTriggerTimeoutSeconds"]?.toLong()
                    ?: 5L
            val validateClusterNodeMembership = builder.descriptor["validateClusterNodeMembership"]?.toBoolean()
                ?: false
            val keepAlive = builder.descriptor["keepAlive"]?.toBoolean() ?: false
            logger.info("[LETTUCE-REDIS-CONFIG] - resourceId: ${builder.resourceId}," +
                    " dynamicRefreshSources: $dynamicRefreshSources," +
                    " refreshPeriodSeconds: $refreshPeriodSeconds," +
                    " enablePeriodicRefresh: $enablePeriodicRefresh," +
                    " enableAllAdaptiveRefreshTriggers: $enableAllAdaptiveRefreshTriggers, " +
                    " adaptiveRefreshTriggerTimeout: $adaptiveRefreshTriggerTimeout" +
                    " keepAlive: $keepAlive" +
                    " ioThreadPoolSize: $ioThreadPoolSize")
            val refreshOptions = io.lettuce.core.cluster.ClusterTopologyRefreshOptions.builder()
                .dynamicRefreshSources(dynamicRefreshSources)
                .refreshPeriod(Duration.ofSeconds(refreshPeriodSeconds))
                .enablePeriodicRefresh(enablePeriodicRefresh)
                .apply {
                    if (enableAllAdaptiveRefreshTriggers) {
                        enableAllAdaptiveRefreshTriggers()
                        adaptiveRefreshTriggersTimeout(Duration.ofSeconds(adaptiveRefreshTriggerTimeout))
                    }
                }.build()
            val clientOptions = io.lettuce.core.cluster.ClusterClientOptions.builder()
                .validateClusterNodeMembership(validateClusterNodeMembership)
                .topologyRefreshOptions(refreshOptions)
                .socketOptions(SocketOptions.builder().keepAlive(keepAlive).build())
                .build()
            io.lettuce.core.cluster.RedisClusterClient
                .create(clientResource, redisUri).let {
                    it.setOptions(clientOptions)
                    it to it.connect()
                }
        } else {
            val clientResource = ClientResources.builder().build()
            io.lettuce.core.RedisClient.create(clientResource, redisUri).let {
                it to it.connect()
            }
        }
        return RedisLettuce6Client(
            clientAndConnection.first,
            clientAndConnection.second,
            builder.descriptor["id"] ?: "")
    }

    fun buildJedisPool() : JedisPool {
        val password = builder.descriptor["password"]!!
        val host = getRedisHostFromId(builder.descriptor)
        val port = builder.descriptor["port"]?.toInt() ?: 6379
        val db = builder.descriptor["db"]?.toInt() ?: 0
        val timeout = Math.min(builder.socketTimeoutMillis, builder.connectionTimeoutMillis)
        val poolConfig = GenericObjectPoolConfig();
        poolConfig.testOnBorrow = true
        return JedisPool(poolConfig, URI("redis", ":$password", host, port, "/$db", null, null), timeout)
    }
}
