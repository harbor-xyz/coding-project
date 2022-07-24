package server.core.service

import com.google.inject.Inject
import com.google.inject.Singleton
import org.slf4j.LoggerFactory
import server.core.dao.CourseDao
import server.core.dao.UserDao
import server.core.mappers.User

@Singleton
class UserService @Inject constructor(
    private val userDao: UserDao,
) {
    private val logger = LoggerFactory.getLogger(UserService::class.java)

    fun getUserById(id: Int): User? {
        return userDao.getUserById(id)
    }

    fun getUserByMobileNumber(mobileNumber: String): User? {
        return userDao.getUserByMobileNumber(mobileNumber)
    }

    fun createUser(userObj: User) {
        return userDao.createUser(userObj)
    }
}