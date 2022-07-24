package common.client

class CommonServiceException(val requestHttpMethod: String, val requestUrl: String,
                            val httpStatusCode: Int, val error: CommonServiceError?,
                            val responseBody: ByteArray?) :
    RuntimeException(DEFAULT_MESSAGE) {

    override fun toString(): String{
        return "ServiceException(requestHttpMethod='$requestHttpMethod', requestUrl='$requestUrl', httpStatusCode=$httpStatusCode, error=$error)"
    }

    companion object {
        const val DEFAULT_MESSAGE = "Server Error"
    }
}