package common.assets.sql.jdbi3

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import common.assets.sql.jdbi.DateTimeType
import common.assets.sql.jdbi.UtcMillis
import microsoft.sql.DateTimeOffset
import org.jdbi.v3.core.argument.Argument
import org.jdbi.v3.core.argument.NamedArgumentFinder
import org.jdbi.v3.core.statement.StatementContext
import org.jdbi.v3.sqlobject.customizer.SqlStatementCustomizerFactory
import org.jdbi.v3.sqlobject.customizer.SqlStatementParameterCustomizer
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl
import java.lang.reflect.Method
import java.lang.reflect.Parameter
import java.lang.reflect.Type
import java.sql.Timestamp
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaType


class KotlinBinderFactory: SqlStatementCustomizerFactory {
    override fun createForParameter(annotation: Annotation?, sqlObjectType: Class<*>?, method: Method?, param: Parameter?, index: Int, paramType: Type?): SqlStatementParameterCustomizer {
        return SqlStatementParameterCustomizer { stmt, dataObject ->
            val klass = dataObject.javaClass.kotlin
            val properties = klass.members
                .mapNotNull { it as? KProperty<Any?> }
            stmt.bindNamedArgumentFinder(KotlinNamedArgFinderJdbi3(properties, dataObject))
        }
    }
}


internal class KotlinNamedArgFinderJdbi3(private val properties: List<KProperty<Any?>>,
                                         private val dataObj: Any): NamedArgumentFinder {
    companion object {
        private val logger = LoggerFactory.getLogger(KotlinNamedArgFinderJdbi3::class.java)
        private val mapper = ObjectMapper().registerKotlinModule()
    }

    override fun find(name: String, ctx: StatementContext): Optional<Argument> {
        val loweredCaseName = name.filter { it != '_' }.toLowerCase()
        return properties
            .find { it.name.toLowerCase() == loweredCaseName }
            ?.let { property ->
                val value = property.getter.call(dataObj)
                logger.trace("property: $property -> value: $value")
                logger.trace("property javaType: ${property.returnType.javaType}")

                when (property.returnType.javaType) {
                    Int::class.java -> {
                        ctx.findArgumentFor(Int::class.java, value)
                    }
                    Class.forName("java.lang.Long"),
                    Long::class.java -> {
                        val dataType = (property.annotations.find { it is UtcMillis } as UtcMillis?)?.dataType
                        if (dataType != null) {
                            val unixTime = if (property.returnType.isMarkedNullable) value as Long? else value as Long

                            if (dataType == DateTimeType.DATETIME2) {
                                if (unixTime != null) {
                                    val ts = Timestamp(unixTime - TimeZone.getDefault().rawOffset)
                                    ctx.findArgumentFor(Timestamp::class.java, ts)
                                } else {
                                    ctx.findArgumentFor(Timestamp::class.java, null)
                                }
                            } else {
                                if (unixTime != null) {
                                    val msDate = DateTimeOffset.valueOf(Timestamp(unixTime), TimeUnit.MILLISECONDS.toMinutes(TimeZone.getDefault().rawOffset.toLong()).toInt())
                                    ctx.findArgumentFor(DateTimeOffset::class.java, msDate)
                                } else {
                                    ctx.findArgumentFor(DateTimeOffset::class.java, null)
                                }
                            }
                        } else {
                            ctx.findArgumentFor(Long::class.java, value)
                        }
                    }
                    java.sql.Date::class.java, java.util.Date::class.java -> {
                        val unixTime = (if (property.returnType.isMarkedNullable) value as java.util.Date? else value as java.util.Date)?.time
                        if (unixTime != null) {
                            val msDate = DateTimeOffset.valueOf(Timestamp(unixTime), 0)
                            ctx.findArgumentFor(DateTimeOffset::class.java, msDate)
                                .let {
                                    if(it.isPresent.not())
                                        ctx.findArgumentFor(Timestamp::class.java, Timestamp(unixTime))
                                    else
                                        it
                                }
                        } else {
                            ctx.findArgumentFor(Timestamp::class.java, null)
                        }
                    }
                    DateTime::class.java -> {
                        val unixTime = (if (property.returnType.isMarkedNullable) value as DateTime? else value as DateTime)?.millis
                        if (unixTime != null) {
                            val msDate = DateTimeOffset.valueOf(Timestamp(unixTime), 0)
                            ctx.findArgumentFor(DateTimeOffset::class.java, msDate)
                                .let {
                                    if(it.isPresent.not())
                                        ctx.findArgumentFor(Timestamp::class.java, Timestamp(unixTime))
                                    else
                                        it
                                }
                        } else {
                            ctx.findArgumentFor(Timestamp::class.java, null)
                        }
                    }
                    Instant::class.java -> {
                        val unixTime = (if (property.returnType.isMarkedNullable) value as Instant? else value as Instant)?.toEpochMilli()
                        if (unixTime != null) {
                            val msDate = DateTimeOffset.valueOf(Timestamp(unixTime), 0)
                            ctx.findArgumentFor(DateTimeOffset::class.java, msDate)
                                .let {
                                    if(it.isPresent.not())
                                        ctx.findArgumentFor(Timestamp::class.java, Timestamp(unixTime))
                                    else
                                        it
                                }
                        } else {
                            ctx.findArgumentFor(Timestamp::class.java, null)
                        }
                    }
                    Double::class.java -> {
                        ctx.findArgumentFor(Double::class.java, value)
                    }
                    String::class.java -> {
                        ctx.findArgumentFor(String::class.java, value)
                    }
                    Float::class.java -> {
                        ctx.findArgumentFor(Float::class.java, value)
                    }
                    else -> {
                        if (value == null) {
                            ctx.findArgumentFor(String::class.java, null)
                        } else {
                            if (property.returnType.javaType is ParameterizedTypeImpl) {
                                if ((property.returnType.javaType as ParameterizedTypeImpl).rawType.isEnum) {
                                    ctx.findArgumentFor(String::class.java, (value as Enum<*>).name)
                                } else {
                                    ctx.findArgumentFor(String::class.java, mapper.writeValueAsString(value))
                                }
                            }else {
                                if ((property.returnType.javaType as Class<*>).isEnum) {
                                    ctx.findArgumentFor(String::class.java, (value as Enum<*>).name)
                                } else {
                                    ctx.findArgumentFor(String::class.java, mapper.writeValueAsString(value))
                                }
                            }
                        }
                    }
                }
            }
            ?: throw (Exception("Unable to create argument for $name"))
    }
}
