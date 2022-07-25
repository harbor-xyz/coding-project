package common.assets.sql.jdbi

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class UtcMillis(val dataType: DateTimeType)

enum class DateTimeType {
    DATETIME2,
    DATETIMEOFFSET
}