package common.resources

import com.azure.cosmos.CosmosClient
import com.azure.cosmos.CosmosContainer
import com.azure.cosmos.implementation.Document
import com.azure.cosmos.models.CosmosQueryRequestOptions
import com.azure.cosmos.models.SqlQuerySpec
import com.azure.cosmos.models.PartitionKeyBuilder
import com.azure.cosmos.models.CosmosItemRequestOptions
import com.azure.cosmos.models.CosmosItemResponse
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import okhttp3.internal.http2.ErrorCode
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


class DocumentDbDao <D : Any>    (
    private val klass: Class<D>,
    databaseName: String,
    private val collectionName: String,
    private val mapper: ObjectMapper,
    private val cosmosClient: CosmosClient
) {

    private val container: CosmosContainer = cosmosClient
        .getDatabase(databaseName).getContainer(collectionName)

    suspend fun queryAllDocuments(queryName: String, query: String): List<Document> {
        return withTelemetry("QueryAll:$queryName", { this.usage() }) {
            container.queryItems(query, CosmosQueryRequestOptions(), Object::class.java).iterableByPage().flatMap {
                it.results
            }.map {
                Document.fromObject(it, mapper)
            }
        }
    }

    suspend fun queryAllDocuments(queryName: String, querySpec: SqlQuerySpec): List<Document> {
        return withTelemetry("QueryAll:$queryName", { this.usage() }) {
            container.queryItems(querySpec, CosmosQueryRequestOptions(), Object::class.java).iterableByPage().flatMap {
                it.results
            }.map {
                Document.fromObject(it, mapper)
            }
        }
    }

    suspend fun queryDocumentByPage(queryName: String, query: String, currentPageNumber: Int, pageSize: Int, continuationToken: String?): DocumentPage {
        val continuationTokenCopy = if (continuationToken.isNullOrEmpty()) null else continuationToken

        val feedResponse = container.queryItems(query, CosmosQueryRequestOptions(), Object::class.java).iterableByPage(
            continuationTokenCopy, pageSize
        ).first()

        val pages = feedResponse.results.map {
            Document.fromObject(it, mapper)
        }

        val responseContinuationToken = feedResponse.continuationToken

        return if (pages.isNotEmpty()) {
            DocumentPage(pages, responseContinuationToken)
        } else
            DocumentPage(emptyList(), null)
    }


    suspend fun queryDocuments(queryName: String, query: String,partitionKey: String): List<Document> {
        return withTelemetry("QueryAll:$queryName", { this.usage() }) {
            container.queryItems(query, CosmosQueryRequestOptions().setPartitionKey(
                PartitionKeyBuilder().add(partitionKey).build()
            ), Object::class.java).map {
                Document.fromObject(it, mapper)
            }
        }
    }

    fun queryDocumentsBlocking(queryName: String, query: String): List<Document> {
        return withTelemetryBlocking("QueryAll:$queryName", { this.usage() }) {
            container.queryItems(query, CosmosQueryRequestOptions(), Object::class.java).iterableByPage().flatMap {
                it.results
            }.map {
                Document.fromObject(it, mapper)
            }
        }
    }

    fun queryDocuments(queryName: String, query: String): ReceiveChannel<Document> {
        return container.queryItems(query, CosmosQueryRequestOptions(), Object::class.java).iterableByPage().flatMap {
            it.results.map { document-> Document.fromObject(document, mapper) }
        }.asReceiveChannel()
    }


    suspend fun getDocument(docId: String, partitionKey: String): Document {
        return withTelemetry("Get", { this.usage() }) {
            Document.fromObject(
                container.readItem(docId, PartitionKeyBuilder().add(partitionKey).build(), Object::class.java).item,
                mapper
            )
        }
    }

    suspend fun findDocument(docId: String): Document? {
        val pages = withTelemetry("Get", { this.usage() }) {
            container.queryItems(
                "SELECT * FROM $collectionName WHERE $collectionName.id = '$docId'",
                CosmosQueryRequestOptions(), Object::class.java
            ).iterableByPage().flatMap {
                it.results
            }.map {
                Document.fromObject(it, mapper)
            }
        }

        return pages.firstOrNull()
    }

    suspend fun createDocument(obj: D): Document {
        return withTelemetry("Create", { this.usage() }) {
            container.createItem(obj).let {
                Document.fromObject(obj, mapper)
            }
        }
    }

    suspend fun upsertDocument(obj: D): Document {
        return withTelemetry("Upsert", { this.usage() }) {
            container.upsertItem(obj).let {
                Document.fromObject(obj, mapper)
            }
        }
    }

    fun upsertDocumentBlocking(obj: D): Document {
        return withTelemetryBlocking("Upsert", { this.usage() }) {
            container.upsertItem(obj).let {
                Document.fromObject(obj, mapper)
            }
        }
    }

    suspend fun createDocumentWithTtl(obj: D,ttlInSec:Int): Document {
        return withTelemetry("Create", { this.usage() }) {
            val document = newDocument(obj)
            document.setTimeToLive(ttlInSec)
            container.createItem(document).let {
                Document.fromObject(document, mapper)
            }
        }
    }

    suspend fun deleteDocument(id:String, partitionKey: String) {
        withTelemetry("Delete", { this.usage() }) {
            container.deleteItem(id, PartitionKeyBuilder().add(partitionKey).build(), CosmosItemRequestOptions())
        }
    }


    suspend fun createOrUpdateDocument(obj: D): Document {
        return withTelemetry("Create", { this.usage() }) {
            container.upsertItem(obj).let {
                Document.fromObject(obj, mapper)
            }
        }
    }

    suspend fun updateDocument(obj: D): Document {
        return withTelemetry("Update", { this.usage() }) {
            val document = newDocument(obj)
            val item = container.queryItems(
                "SELECT * FROM $collectionName WHERE $collectionName.id = '${document.id}'",
                CosmosQueryRequestOptions(), Object::class.java
            ).iterableByPage().flatMap {
                it.results
            }.map {
                Document.fromObject(it, mapper)
            }
            if (item.isEmpty()) {
                throw Exception("")
            }
            else{
                container.upsertItem(obj).let {
                    Document.fromObject(obj, mapper)
                }
            }
        }
    }

    suspend fun updateDocumentOptimistically(obj: D,eTag:String): Document {
        return withTelemetry("Update", { this.usage() }) {
            val document = newDocument(obj)
            val item = container.queryItems(
                "SELECT * FROM $collectionName WHERE $collectionName.id = '${document.id}'",
                CosmosQueryRequestOptions(), Object::class.java
            ).iterableByPage().flatMap {
                it.results
            }.map {
                Document.fromObject(it, mapper)
            }
            if (item.isEmpty()) {
                throw Exception("no items")
            }
            else{
                container.upsertItem(obj, CosmosItemRequestOptions().setIfMatchETag(eTag)).let {
                    Document.fromObject(obj, mapper)
                }
            }
        }
    }

    private suspend fun <T> withTelemetry(commandName: String,
                                          extractUsageProperties: T.() -> Map<String, String>,
                                          fn: suspend () -> T): T {

        val startTime = System.nanoTime()
        var exception: Exception? = null
        var response: T? = null

        return try {
            fn().also {
                response = it
            }
        }
        catch (e: Exception) {
            exception = e
            throw e
        }
        finally { }
    }

    private fun <T> withTelemetryBlocking(commandName: String,
                                          extractUsageProperties: T.() -> Map<String, String>,
                                          fn: () -> T): T {

        val startTime = System.nanoTime()
        var exception: Exception? = null
        var response: T? = null

        return try {
            fn().also {
                response = it
            }
        }
        catch (e: Exception) {
            exception = e
            throw e
        }
        finally {
        }
    }

    private fun List<Document>.toParamTypeList() =
        map { document -> mapper.readValue(document.toJson(), Object::class.java) }

    private fun Document.usage(): Map<String, String> {
        // todo: return usage metrics
        return emptyMap()
    }

    private fun <T> CosmosItemResponse<T>.usage(): Map<String, String> {
        // todo: return usage metrics
        return emptyMap()
    }

    private fun <E> Collection<E>.usage(): Map<String, String> {
        // todo: return usage metrics
        return emptyMap()
    }

    private fun newDocument(obj: D): DocumentV2 = DocumentV2(obj)

    inner class DocumentV2(obj: D) : Document(this@DocumentDbDao.mapper.writeValueAsString(obj))

    data class DocumentPage(val result:List<Document>,val continuationToken: String?)

}

fun <T> List<T>.asReceiveChannel(context: CoroutineContext = EmptyCoroutineContext): ReceiveChannel<T> {
    val list = this
    return GlobalScope.produce(context) {
        for (e in list) {
            send(e)
        }
    }
}
