package common.assets.sql.jdbi3

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import microsoft.sql.DateTimeOffset
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import java.sql.Timestamp
import java.util.*
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaType
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl
import java.time.Instant


open class KotlinMapperJdbi3<C : Any> constructor(private val clazz: Class<C>, private val customColumnMappers: List<CustomColumnMapper>? = null) : RowMapper<C> {
    companion object {
        val mapper = ObjectMapper().registerKotlinModule()
        private val logger = LoggerFactory.getLogger(KotlinMapperJdbi3::class.java)

        abstract class CustomColumnMapper (val paramName: String) {
            abstract fun deserialize(resultSet: ResultSet, objectMapper: ObjectMapper, param: KParameter): Any
        }
    }

    private val klass = clazz.kotlin

    constructor(clazz: Class<C>) : this(clazz, null)

    override fun map(rs: ResultSet, ctx: StatementContext): C {
        val constructor = klass.primaryConstructor!!
        constructor.isAccessible = true

        // TODO: best fit for constructors + writeable properties, pay attention to nullables/optionals with default values
        //       for now just call primary constructor using named params and hope

        val validParametersByName = constructor.parameters
            .filter { it.kind == KParameter.Kind.VALUE && it.name != null }
            .map { it.name!!.toLowerCase() to it }
            .toMap()

        val matchingParms: MutableList<Pair<KParameter, Any?>> = mutableListOf()
        for (i: Int in 1..rs.metaData.columnCount) {
            rs.metaData.getColumnLabel(i).replace("_", "").toLowerCase()
                .let { colLabel ->
                    logger.trace("colLabel: $colLabel")
                    validParametersByName[colLabel]
                        ?.let { param: KParameter ->
                            if (param.type.isMarkedNullable && rs.getObject(i) == null) {
                                matchingParms.add(Pair(param, null))
                            } else if (customColumnMappers != null &&
                                param.name != null &&
                                customColumnMappers.isNotEmpty() &&
                                customColumnMappers.any { it.paramName.toLowerCase() == param.name!!.toLowerCase() }) {

                                logger.info("CustomMapper: Found matching param {}", param.name)
                                val colMapper = customColumnMappers.first { it.paramName.toLowerCase() == param.name!!.toLowerCase() }
                                matchingParms.add(Pair(param, colMapper.deserialize(rs, mapper, param)))
                            } else {
                                logger.trace("param: $param :: javaType: ${param.type.javaType}")
                                val paramType = param.type.javaType
                                when (paramType) {
                                    Boolean::class.java -> {
                                        logger.trace("found Boolean")
                                        matchingParms.add(Pair(param, rs.getBoolean(i)))
                                    }
                                    String::class.java -> {
                                        logger.trace("found String")
                                        matchingParms.add(Pair(param, rs.getString(i)))
                                    }
                                    java.lang.Integer::class.java,
                                    Int::class.java -> {
                                        logger.trace("found Integer")
                                        matchingParms.add(Pair(param, rs.getInt(i)))
                                    }
                                    java.lang.Long::class.java,
                                    Long::class.java -> {
                                        logger.trace("found Long")
                                        val value = rs.getObject(i)
                                        if (value is Number) {
                                            matchingParms.add(Pair(param, rs.getLong(i)))
                                        } else {
                                            val long = getTimestampUTC(i, value, param)
                                            matchingParms.add(Pair(param, long))
                                        }
                                    }
                                    Instant::class.java -> {
                                        logger.trace("found java.sql.Date")
                                        val value = rs.getObject(i)
                                        val lng = getTimestampUTC(i, value, param)
                                        matchingParms.add(Pair(param, Instant.ofEpochMilli(lng)))
                                    }
                                    java.sql.Date::class.java -> {
                                        logger.trace("found java.sql.Date")
                                        val value = rs.getObject(i)
                                        //val lng = getTimestampUTC(i, value, param)
                                        matchingParms.add(Pair(param, value))
                                    }
                                    java.sql.Timestamp::class.java -> {
                                        logger.trace("found java.sql.Date")
                                        val value = rs.getObject(i)
                                        //val lng = getTimestampUTC(i, value, param)
                                        matchingParms.add(Pair(param, value))
                                    }
                                    java.util.Date::class.java -> {
                                        logger.trace("found java.sql.Date")
                                        val value = rs.getObject(i)
                                        val lng = getTimestampUTC(i, value, param)
                                        matchingParms.add(Pair(param, java.util.Date(lng)))
                                    }
                                    DateTime::class.java -> {
                                        logger.trace("found java.sql.Date")
                                        val value = rs.getObject(i)
                                        val lng = getTimestampUTC(i, value, param)
                                        matchingParms.add(Pair(param, DateTime(lng)))
                                    }
                                    java.lang.Double::class.java,
                                    Double::class.java -> {
                                        logger.trace("found Double")
                                        matchingParms.add(Pair(param, rs.getDouble(i)))
                                    }
                                    java.lang.Float::class.java,
                                    Float::class.java -> {
                                        logger.trace("found Float")
                                        matchingParms.add(Pair(param, rs.getFloat(i)))
                                    }
                                    else -> {
                                        val value = rs.getString(i)
                                        if (param.type.javaType is ParameterizedTypeImpl) {
                                            if ((param.type.javaType as ParameterizedTypeImpl).rawType.isEnum) {
                                                logger.trace("found Enum")
                                                val enumClass = (param.type.javaType as Class<*>)
                                                matchingParms.add(Pair(param, enumClass.getMethod("valueOf", String::class.java).invoke(null, value)))
                                            } else {
                                                matchingParms.add(Pair(param, mapper.readValue(value, Class.forName((paramType as ParameterizedTypeImpl).rawType.typeName))))
                                            }
                                        } else if ((param.type.javaType as Class<*>).isEnum) {
                                            logger.trace("found Enum")
                                            val enumClass = (param.type.javaType as Class<*>)
                                            matchingParms.add(Pair(param, enumClass.getMethod("valueOf", String::class.java).invoke(null, value)))
                                        } else if (paramType.typeName == "java.util.Map<java.lang.String, java.lang.String>") {
                                            val typeRef = object : TypeReference<Map<String, String>>() {}
                                            matchingParms.add(Pair(param, mapper.readValue(value, typeRef)))
                                        } else {
                                            matchingParms.add(Pair(param, mapper.readValue(value, Class.forName(paramType.typeName))))
                                        }
                                    }
                                }
                            }
                        }
                }
        }

        val parmsThatArePresent = matchingParms.map { it.first }.toHashSet()

        // things missing from the result set that are Nullable and not optional should be set to Null
        val nullablesThatAreAbsent = constructor.parameters.filter { !it.isOptional && it.type.isMarkedNullable && it !in parmsThatArePresent }.map {
            Pair(it, null)
        }

        // things that are missing from the result set but are defaultable
        val defaultableThatAreAbsent = constructor.parameters.filter { it.isOptional && !it.type.isMarkedNullable && it !in parmsThatArePresent }.toSet()

        val finalParms = (matchingParms + nullablesThatAreAbsent)
            .filterNot { it.first in defaultableThatAreAbsent }
            .toMap()
        return constructor.callBy(finalParms)
    }

    private fun getTimestampUTC(i: Int, value: Any?, param: KParameter): Long {
        return if (value is Timestamp) {
            // Timestamp is returned when SQL Server data type is DATETIME2
            // DATETIME2 does not have timezone information, so we need to add
            // the system timezone's rawOffset (which is expected to be in UTC).
            //
            // Without adding this rawOffset, DATETIME2 is copied verbatim, e.g.
            // 2017-06-21 07:00:00 without the appropriate timezone conversion,
            // which means that if TimeZone.getDefault() != "UTC", the value will be
            // screwed up
            //
            // Of course, this also assumes that the value was inserted in UTC. So
            // in general, it is much better to just use DATETIMEOFFSET
            value.time + TimeZone.getDefault().rawOffset
        } else if (value is DateTimeOffset) {
            // DateTimeOffset is returned when SQL Server data type is DATETIMEOFFSET
            // This has timezone information, and its timestamp's time is in UTC millis
            value.timestamp.time
        } else {
            throw RuntimeException("Type mismatch: Result set at column number $i cannot be converted to Long Type. param: $param")
        }
    }
}
