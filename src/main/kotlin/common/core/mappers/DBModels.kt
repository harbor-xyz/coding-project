package common.core.mappers

import common.sql.jdbi3.KotlinMapperJdbi3
import java.time.Instant

abstract class DBBaseModel(
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    val createdBy: String = "",
    val updatedBy: String = ""
)

data class User(
    val id: Int,
    val name: String,
    val mobileNumber: String,
    val emailId: String,
): DBBaseModel()

data class UserAvailability(
    val userId: Int,
    val date: Long,
    val startTime: Long,
    val endTime: Long
): DBBaseModel()



class UserMapper: KotlinMapperJdbi3<User>(User::class.java)
class UserAvailabilityMapper: KotlinMapperJdbi3<UserAvailability>(UserAvailability::class.java)