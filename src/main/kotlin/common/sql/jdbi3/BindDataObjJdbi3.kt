package common.sql.jdbi3

import org.jdbi.v3.sqlobject.customizer.SqlStatementCustomizingAnnotation


@SqlStatementCustomizingAnnotation(KotlinBinderFactory::class)
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class BindDataObjJdbi3