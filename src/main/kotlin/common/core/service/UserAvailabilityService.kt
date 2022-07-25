package common.core.service

import com.google.inject.Inject
import com.google.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.slf4j.LoggerFactory
import common.core.dao.UserAvailabilityDao
import common.core.mappers.UserAvailability
import kotlinx.coroutines.runBlocking
import java.time.Instant
import javax.jws.soap.SOAPBinding.Use


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

    fun getOverlappingAvailability(userId1: Int, userId2: Int, date: Long): List<UserAvailability>? {
        val jobDeferred = CoroutineScope(Dispatchers.IO).async {
            val records = userAvailabilityDao.getOverlappingAvailability(userId1, userId2, date)
            val overlappedAvailabilityRecords = mutableListOf<UserAvailability>()
            records?.forEach {
                overlappedAvailabilityRecords.add(UserAvailability(
                    it.userId1,
                    it.date1,
                    it.startTime1,
                    it.endTime1
                ))

                overlappedAvailabilityRecords.add(UserAvailability(
                    it.userId2,
                    it.date2,
                    it.startTime2,
                    it.endTime2
                ))
            }
            overlappedAvailabilityRecords
        }
        return runBlocking {  jobDeferred.await()}
    }
}