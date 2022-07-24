package server.core.dao

import org.jdbi.v3.sqlobject.config.RegisterRowMapper
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.statement.SqlQuery
import server.core.mappers.CourseData
import server.core.mappers.CourseMapper


const val COURSE = "Course_data"

interface CourseDao {
    @RegisterRowMapper(CourseMapper::class)
    @SqlQuery("SELECT * FROM $COURSE WHERE id = :id")
    fun getCourseData(@Bind("id") id: Int): CourseData?
}