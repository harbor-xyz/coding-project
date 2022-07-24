package common.client

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.asynchttpclient.AsyncHttpClientConfig
import org.asynchttpclient.DefaultAsyncHttpClientConfig
import org.asynchttpclient.Dsl.config

data class ClientConfig(val baseUrl: String, val asyncHttpClientConfig: AsyncHttpClientConfig) {

    @JsonCreator
    constructor(@JsonProperty("baseUrl") baseUrl: String, @JsonProperty("httpClientConfig") httpClientConfig: HttpClientConfig)
            : this(baseUrl, httpClientConfig.getAsyncHttpClientConfig())

    @Deprecated("See other constructors")
    @JvmOverloads
    constructor(baseUrl: String, requestTimeoutMillis: Int)
            : this(baseUrl, HttpClientConfig(requestTimeout = requestTimeoutMillis))

    fun customize(block: (DefaultAsyncHttpClientConfig.Builder) -> Unit): ClientConfig {
        return DefaultAsyncHttpClientConfig.Builder(asyncHttpClientConfig).apply {
            block(this)
        }.let {
            ClientConfig(baseUrl, it.build())
        }
    }
}

data class HttpClientConfig(
    val threadPoolName: String = DEFAULT_THREAD_POOL_NAME,
    val maxConnections: Int = -1,
    val maxConnectionsPerHost: Int = -1,
    val connectTimeout: Int = 5000,
    val pooledConnectionIdleTimeout: Int = 60000,
    //val connectionPoolCleanerPeriod: Int = 1000,
    val readTimeout: Int = 60000,
    val requestTimeout: Int = 60000,
    val connectionTtl: Int = 60000,
    val followRedirect: Boolean = false,
    val maxRedirects: Int = 5,
    val compressionEnforced: Boolean = false,
    val userAgent: String = "AHC/2.0",
    val enabledProtocols: List<String> = listOf("TLSv1.2", "TLSv1.1", "TLSv1"),
    val useProxySelector: Boolean = false,
    val useProxyProperties: Boolean = false,
    val validateResponseHeaders: Boolean = true,
    val strict302Handling: Boolean = false,
    val keepAlive: Boolean = true,
    val maxRequestRetry: Int = 5,
    val disableUrlEncodingForBoundRequests: Boolean = false,
    //val removeQueryParamOnRedirect: Boolean = true,
    val useOpenSsl: Boolean = false,
    val acceptAnyCertificate: Boolean = false,
    val sslSessionCacheSize: Int = 0,
    val sslSessionTimeout: Int = 0,
    val tcpNoDelay: Boolean = true,
    val soReuseAddress: Boolean = false,
    val soLinger: Int = -1,
    val soSndBuf: Int = -1,
    val soRcvBuf: Int = -1,
    val httpClientCodecMaxInitialLineLength: Int = 4096,
    val httpClientCodecMaxHeaderSize: Int = 8192,
    val httpClientCodecMaxChunkSize: Int = 8192,
    val disableZeroCopy: Boolean = false,
    val handshakeTimeout: Int = 10000,
    val chunkedFileChunkSize: Int = 8192,
    val webSocketMaxBufferSize: Int = 128000000,
    val webSocketMaxFrameSize: Int = 10240,
    val keepEncodingHeader: Boolean = false,
    val shutdownQuietPeriod: Int = 2000,
    val shutdownTimeout: Int = 15000,
    val useNativeTransport: Boolean = false,
    val usePooledMemory: Boolean = true) {

    companion object {
        const val DEFAULT_THREAD_POOL_NAME = "AsyncHttpClient"
    }

    internal fun getAsyncHttpClientConfig() : AsyncHttpClientConfig =
        config()
            .setThreadPoolName(threadPoolName)
            .setMaxConnections(maxConnections)
            .setMaxConnectionsPerHost(maxConnectionsPerHost)
            .setConnectTimeout(connectTimeout)
            .setPooledConnectionIdleTimeout(pooledConnectionIdleTimeout)
            //.setConnectionPoolCleanerPeriod(connectionPoolCleanerPeriod)
            .setReadTimeout(readTimeout)
            .setRequestTimeout(requestTimeout)
            .setConnectionTtl(connectionTtl)
            .setFollowRedirect(followRedirect)
            .setMaxRedirects(maxRedirects)
            .setCompressionEnforced(compressionEnforced)
            .setUserAgent(userAgent)
            .setEnabledProtocols(enabledProtocols.toTypedArray())
            .setUseProxySelector(useProxySelector)
            .setUseProxyProperties(useProxyProperties)
            .setValidateResponseHeaders(validateResponseHeaders)
            .setStrict302Handling(strict302Handling)
            .setKeepAlive(keepAlive)
            .setMaxRequestRetry(maxRequestRetry)
            .setDisableUrlEncodingForBoundRequests(disableUrlEncodingForBoundRequests)
            //.setRemoveQueryParamOnRedirect(removeQueryParamOnRedirect)
            .setUseOpenSsl(useOpenSsl)
            .setUseInsecureTrustManager(acceptAnyCertificate)
            .setSslSessionCacheSize(sslSessionCacheSize)
            .setSslSessionTimeout(sslSessionTimeout)
            .setTcpNoDelay(tcpNoDelay)
            .setSoReuseAddress(soReuseAddress)
            .setSoLinger(soLinger)
            .setSoSndBuf(soSndBuf)
            .setSoRcvBuf(soRcvBuf)
            .setHttpClientCodecMaxInitialLineLength(httpClientCodecMaxInitialLineLength)
            .setHttpClientCodecMaxHeaderSize(httpClientCodecMaxHeaderSize)
            .setHttpClientCodecMaxChunkSize(httpClientCodecMaxChunkSize)
            .setDisableZeroCopy(disableZeroCopy)
            .setHandshakeTimeout(handshakeTimeout)
            .setChunkedFileChunkSize(chunkedFileChunkSize)
            .setWebSocketMaxBufferSize(webSocketMaxBufferSize)
            .setWebSocketMaxFrameSize(webSocketMaxFrameSize)
            .setKeepEncodingHeader(keepEncodingHeader)
            .setShutdownQuietPeriod(shutdownQuietPeriod)
            .setShutdownTimeout(shutdownTimeout)
            .setUseNativeTransport(useNativeTransport).build()
//                    .setUsePooledMemory(usePooledMemory).build()
}