package common.core.dao

import common.core.mappers.TwoUserAvailabilityRecords
import common.core.mappers.TwoUserAvailabilityRecordsMapper
import org.jdbi.v3.sqlobject.config.RegisterRowMapper
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import common.core.mappers.UserAvailability
import common.core.mappers.UserAvailabilityMapper
import java.time.Instant

const val USER_AVAILABILITY = "user_availability"

/*
    select
    case when
        (t1.startTime > t2.startTime and t1.startTime < t2.endTime) or
        (t1.endTime > t2.startTime and t1.endTime < t2.endTime) or
        (t1.startTime < t2.startTime and t1.endTime > t2.endTime) or
        (t1.startTime > t2.startTime and t1.endTime < t2.endTime)
    then
        'yes'
    else
        'no'
       end as OverLapping
    from user_availability as t1, user_availability as t2
        where t1.userId = 1 and t2.usersId = 2
 */
interface UserAvailabilityDao {

    @RegisterRowMapper(UserAvailabilityMapper::class)
    @SqlQuery("SELECT * FROM $USER_AVAILABILITY WHERE user_id = :userId")
    fun getUserAvailabilityByUserId(@Bind("userId") userId: Int): List<UserAvailability>?

    @SqlUpdate("INSERT INTO $USER_AVAILABILITY (user_id, date, start_time, end_time) VALUES(:userId, :date, :startTime, :endTime)")
    fun createUserAvailability(@BindBean userAvailability: UserAvailability): Unit

    @RegisterRowMapper(TwoUserAvailabilityRecordsMapper::class)
    @SqlQuery("""
       select t.u1 as user_id1, t.t1d as date1, t.t1s as start_time1, t.t1e as end_time1, t.u2 as user_id2, t.t2d as date2, t.t2s as start_time2, t.t2e as end_time2 from (
select
    case when
                 (t1.start_time > t2.start_time and t1.start_time < t2.end_time) or
                 (t1.end_time > t2.start_time and t1.end_time < t2.end_time) or
                 (t1.start_time < t2.start_time and t1.end_time > t2.end_time) or
                 (t1.start_time > t2.start_time and t1.end_time < t2.end_time)
             then
             'yes'
         else
             'no'
        end as Overlapping,
t1.user_id as u1, t2.user_id as u2, t1.date as t1d, t1.start_time as t1s, t1.end_time as t1e, t2.date as t2d, t2.start_time as t2s, t2.end_time as t2e
from user_availability as t1, user_availability as t2
where t1.user_id = :userId1 and t2.user_id = :userId2 and t1.status = 'ACTIVE' and t2.status = 'ACTIVE') as t
where t.Overlapping = 'Yes'
    """)
    fun getOverlappingAvailability(@Bind("userId1") userId1: Int, @Bind("userId2") userId2: Int, @Bind("date") date: Long): List<TwoUserAvailabilityRecords>?
}