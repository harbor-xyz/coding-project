package common.core.service

import common.core.dao.CourseDao
import common.core.mappers.CourseData
import com.google.inject.Inject
import com.google.inject.Singleton
import org.slf4j.LoggerFactory

@Singleton
class CourseService @Inject constructor(
    private val courseDao: CourseDao,
    //@Named("common-test-cosmos") private val cosmosClient: CosmosClient,
    //@Named("common-test-redis-prod-cache") private val commonTestCache: RedisCache2<CourseDataRedis>
) {
    private val logger = LoggerFactory.getLogger(CourseService::class.java)
    fun getCourseDataById(id: Int): CourseData? =
        courseDao.getCourseData(id)

    fun getCourseDataByIdSpeed(id: Int): CourseData?{
        return CourseData(1, name = "x", "amod")
    }

}