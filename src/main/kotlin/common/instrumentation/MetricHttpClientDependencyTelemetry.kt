package common.instrumentation


import io.netty.handler.codec.http.HttpHeaders
import io.prometheus.client.Histogram
import org.asynchttpclient.*
import org.asynchttpclient.filter.FilterContext
import org.asynchttpclient.filter.RequestFilter


class MetricHttpClientDependencyTelemetry private constructor(val name: String) : RequestFilter {
    override fun <T : Any?> filter(ctx: FilterContext<T>): FilterContext<T> {

        return FilterContext.FilterContextBuilder(ctx)
                .asyncHandler(AsyncHandlerWrapper(ctx.request, ctx.asyncHandler))
                .build()
    }

    inner class AsyncHandlerWrapper<T>(@Suppress("unused") private val request: Request,
                                       private val asyncHandler: AsyncHandler<T>) : AsyncHandler<T> {

        private var statusCode = 0


        private fun complete() {
            //
        }

        override fun onThrowable(t: Throwable) {
            try {
                asyncHandler.onThrowable(t)
            } finally {
                complete()
            }
        }

        override fun onBodyPartReceived(bodyPart: HttpResponseBodyPart): AsyncHandler.State {
            return asyncHandler.onBodyPartReceived(bodyPart)
        }

        override fun onStatusReceived(responseStatus: HttpResponseStatus): AsyncHandler.State {
            statusCode = responseStatus.statusCode

            return asyncHandler.onStatusReceived(responseStatus)
        }

        override fun onHeadersReceived(headers: HttpHeaders): AsyncHandler.State {
            return asyncHandler.onHeadersReceived(headers)
        }

        override fun onCompleted(): T {
            try {
                return asyncHandler.onCompleted()
            } finally {
                complete()
            }
        }
    }

    companion object {
        fun addFilter(name: String, configBuilder: DefaultAsyncHttpClientConfig.Builder) {
            val filter = MetricHttpClientDependencyTelemetry(name)
            configBuilder.addRequestFilter(filter)
        }
    }
}

private val dependencyHistogram by lazy {
    val histo = Histogram.build("http_dependency_call_latency", "HTTP dependency latencies")
        .labelNames("target")
}

