package common.core.dao

import org.jdbi.v3.sqlobject.config.RegisterRowMapper
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import common.core.mappers.UserAvailability
import common.core.mappers.UserAvailabilityMapper

const val USER_AVAILABILITY = "user_availability"

interface UserAvailabilityDao {

    @RegisterRowMapper(UserAvailabilityMapper::class)
    @SqlQuery("SELECT * FROM $USER_AVAILABILITY WHERE user_id = :userId")
    fun getUserAvailabilityByUserId(@Bind("userId") userId: Int): List<UserAvailability>?

    @SqlUpdate("INSERT INTO $USER_AVAILABILITY (user_id, date, start_time, end_time) VALUES(:userId, :date, :startTime, :endTime)")
    fun createUserAvailability(@BindBean userAvailability: UserAvailability): Unit
}