package server.core.dao

import org.jdbi.v3.sqlobject.config.RegisterRowMapper
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import server.core.mappers.CourseData
import server.core.mappers.CourseMapper
import server.core.mappers.User
import server.core.mappers.UserMapper


const val USER = "user"

interface UserDao {

    @RegisterRowMapper(UserMapper::class)
    @SqlQuery("SELECT * FROM $USER WHERE id = :id")
    fun getUserById(@Bind("id") id: Int): User?

    @RegisterRowMapper(UserMapper::class)
    @SqlQuery("SELECT * FROM $USER WHERE mobileNumber = :mobileNumber")
    fun getUserByMobileNumber(@Bind("mobileNumber") mobileNumber: String): User?

    @SqlUpdate("INSERT INTO $USER (name, mobileNumber, emailId) VALUES(:name, :mobileNumber, :emailId)")
    fun createUser(@BindBean user: User)
}