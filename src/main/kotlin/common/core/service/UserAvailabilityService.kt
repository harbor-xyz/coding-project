package common.core.service

import com.google.inject.Inject
import com.google.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.slf4j.LoggerFactory
import common.core.dao.UserAvailabilityDao
import common.core.mappers.UserAvailability


@Singleton
class UserAvailabilityService @Inject constructor(
    private val userAvailabilityDao: UserAvailabilityDao
) {

    private val logger = LoggerFactory.getLogger(UserAvailabilityService::class.java)

    suspend fun getUserAvailability(userId: Int): List<UserAvailability>? {
        val jobDeferred = CoroutineScope(Dispatchers.IO).async {
            userAvailabilityDao.getUserAvailabilityByUserId(1)
        }
        return jobDeferred.await()
    }


    fun createUserAvailability(userAvailability: UserAvailability) {
        CoroutineScope(Dispatchers.IO).async {
            userAvailabilityDao.createUserAvailability(userAvailability)
        }
    }
}