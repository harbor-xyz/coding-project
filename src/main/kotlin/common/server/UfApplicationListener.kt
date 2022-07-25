package common.server

import org.glassfish.jersey.server.monitoring.ApplicationEvent
import org.glassfish.jersey.server.monitoring.ApplicationEventListener
import org.glassfish.jersey.server.monitoring.RequestEvent
import org.glassfish.jersey.server.monitoring.RequestEventListener
import javax.servlet.http.HttpServletRequest
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerRequestFilter
import javax.ws.rs.container.PreMatching
import javax.ws.rs.core.Context
import javax.ws.rs.core.SecurityContext
import javax.ws.rs.ext.Provider

/**
 * An application event listener that listens for Jersey application initialization to
 * be finished, and reports to [Telemetry] and [PrometheusClient]
 *
 * It listens for method start events, and returns a [RequestEventListener]
 * that updates the relevant metric for suitably annotated methods when it gets the
 * request events indicating that the method is about to be invoked, or just got done
 * being invoked.
 *
 * The Prometheus metric is captured as the histogram http_request_duration_seconds, with the following labels applied
 *     path: {@see pathLabelDeterminant}
 *     method: the http request method
 *     code: the response code
 */
@Provider
class UfApplicationListener(
    /**
     * Specifies the histogram buckets of response times (unit is in seconds)
     */
    /**
     * Function that determines the path label. The default is to pick the first component of the path
     */
    /**
     * Function that can be used to create an application specific custom coroutine context for a request
     */
) : ApplicationEventListener {

    init {
        println("called UfApplicationListener")

    }
    override fun onEvent(event: ApplicationEvent) {
        println("Mani:  received event $event")
    }

    override fun onRequest(p0: RequestEvent?): RequestEventListener {
        TODO("Not yet implemented")
    }

}

private fun RequestEvent.securityContext(): SecurityContext? = this.containerRequest.securityContext
private fun RequestEvent.userPrincipal() = this.securityContext()?.userPrincipal


private const val NANOS_PER_SEC = 1000.0*1000.0*1000.0

private const val REQ_PROP_CLIENT_IP = "_req_client_ip"
@Provider
@PreMatching
internal class ServletRequestInjectionFilter: ContainerRequestFilter {
    @Context var servletRequest: HttpServletRequest? = null

    override fun filter(requestContext: ContainerRequestContext) {
        servletRequest?.let {
            requestContext.setProperty(REQ_PROP_CLIENT_IP, it.remoteAddr)
        }
    }
}
