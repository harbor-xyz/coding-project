package server.core.mappers

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import common.sql.jdbi3.KotlinMapperJdbi3

@JsonIgnoreProperties(ignoreUnknown = true)
data class CourseData(
    val id: Int,
    val name: String,
    val teacher: String
)

data class CourseDataRedis(
    val id: Int,
    val name: String,
    val teacher: String
)


class CourseMapper: KotlinMapperJdbi3<CourseData>(CourseData::class.java)