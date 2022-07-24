package common

import org.apache.commons.io.output.CountingOutputStream
import java.io.IOException
import javax.ws.rs.WebApplicationException
import javax.ws.rs.ext.Provider
import javax.ws.rs.ext.WriterInterceptor
import javax.ws.rs.ext.WriterInterceptorContext


/**
 * This global interceptor enables us to get the content length of the response
 *  before returning tp consumer.
 *
 *  Note :  this is work around due to jersey's limitations to get length of response
 */

@Provider
class ResponseContentLengthInterceptor : WriterInterceptor {

    companion object {
        private const val RESPONSE_LENGTH_PROPERTY = "x-response-size"
    }

    @Throws(IOException::class, WebApplicationException::class)
    override fun aroundWriteTo(writerContext: WriterInterceptorContext) {
        val outputStream = CountingOutputStream(writerContext.outputStream)
        writerContext.outputStream = outputStream
        writerContext.proceed()
        writerContext.headers.add(RESPONSE_LENGTH_PROPERTY, outputStream.getCount())
    }
}