package common.client

import java.io.PrintWriter
import java.io.StringWriter
import lombok.Data

@Data
class CommonServiceError @JvmOverloads constructor(
    private val message: String,
    cause: Throwable = Exception("Service Error")
) {
    private val causeMessage: String?
    private val causeStackTrace: String

    @JvmOverloads
    constructor(cause: Throwable = Exception("Service Error")) : this("Exception in service", cause) {
    }

    init {
        causeMessage = cause.message
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        cause.printStackTrace(pw)
        causeStackTrace = sw.toString()
    }
}