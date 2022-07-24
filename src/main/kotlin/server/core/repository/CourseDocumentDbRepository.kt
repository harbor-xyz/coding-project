package server.core.repository

import com.fasterxml.jackson.databind.ObjectMapper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CourseDocumentDbRepository @Inject constructor(
    private val mapper: ObjectMapper,
    //@Named("common-test-cosmos") private val cosmosClient: CosmosClient
) {

    /*
    private val documentDbDao = DocumentDbDao(
        CourseData::class.java,
        "common-test-cosmos",
        COLLECTION_NAME,
        mapper,
        cosmosClient
    )

    companion object {
        private const val COLLECTION_NAME = "course"
    }

    suspend fun create(document: CourseData) {
        documentDbDao.createDocument(document)
    }

    suspend fun upsert(document: CourseData) {
        val documents = getDocumentsByCourseId(document.id)
        if (documents.isNotEmpty()) {
        } else {
            create(document)
        }
    }

    suspend fun update(document: CourseData) {
        documentDbDao.updateDocument(document)
    }

    suspend fun delete(document: CourseData) {
        //documentDbDao.deleteDocument(document.id.toString())
    }

    suspend fun getDocumentsByCourseId(id: Int): List<CourseData> {
        return documentDbDao.queryDocuments("attribute", "SELECT * FROM $COLLECTION_NAME WHERE $COLLECTION_NAME.id IN ($id)", id.toString()).addressDocs
    }

    private val Document.addressDoc
        get() = mapper.readValue<CourseData>(this.toJson())

    private val List<Document>.addressDocs
        get() = map { it.addressDoc }


     */
}
