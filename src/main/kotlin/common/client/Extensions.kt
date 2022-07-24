package common.client

import kotlinx.coroutines.future.await
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException
import java.util.concurrent.ExecutionException

/**
 * Substitute for CompletableFuture.get() to be used with ServiceClient -- returns null if service throws a 404.
 */
fun <T> CompletableFuture<T>.getOrNull(): T? {
    return try {
        try {
            this.get()
        }
        catch (e: CompletionException) {
            throw e.cause ?: e
        }
        catch (e: ExecutionException) {
            throw e.cause ?: e
        }
    }
    catch (e: CommonServiceException) {
        if (e.httpStatusCode == 404) {
            null
        }
        else {
            throw e
        }
    }
}


/**
 * Substitute for CompletableFuture.await() to be used with ServiceClient -- returns null if service throws a 404.
 */
suspend fun <T> CompletableFuture<T>.awaitOrNull(): T? {
    return try {
        try {
            this.await()
        }
        catch (e: CompletionException) {
            throw e.cause ?: e
        }
        catch (e: ExecutionException) {
            throw e.cause ?: e
        }
    }
    catch (e: CommonServiceException) {
        if (e.httpStatusCode == 404) {
            null
        }
        else {
            throw e
        }
    }
}