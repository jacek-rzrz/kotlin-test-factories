package pl.rzrz.kotlin.test.factories.core

import java.math.BigDecimal
import java.time.*
import kotlin.reflect.*

object ObjectCreator {

    inline fun <reified T> create(): T {
        @OptIn(ExperimentalStdlibApi::class)
        return create(T::class, typeOf<T>()) as T
    }

    private fun create(kType: KType): Any {
        return create(kType.classifier as KClass<*>, kType)
    }

    fun create(kClass: KClass<*>, kType: KType): Any {
        return try {
            matchAndCreate(kClass, kType)
        } catch (e: Exception) {
            throw TestFactoryException("Error creating ${kClass.simpleName}", e)
        }
    }

    private fun matchAndCreate(kClass: KClass<*>, kType: KType): Any {

        val jClass = kClass.java

        if(jClass.isEnum) return getEnumValue(jClass)

        return when (kClass) {
            BigDecimal::class -> BigDecimal.ZERO
            Char::class -> ' '
            Double::class -> 0.0
            Duration::class -> Duration.ZERO
            Float::class -> 0.0F
            Instant::class -> Instant.now()
            Int::class -> 0
            List::class -> listOf(create(kType.arguments[0].type!!))
            LocalTime::class -> LocalTime.now()
            LocalDate::class -> LocalDate.now()
            LocalDateTime::class -> LocalDateTime.now()
            Long::class -> 0L
            Map::class -> mapOf(create(kType.arguments[0].type!!) to create(kType.arguments[1].type!!))
            String::class -> ""
            ZonedDateTime::class -> ZonedDateTime.now()
            else -> createObject(kClass)
        }
    }

    private fun getEnumValue(jClass: Class<out Any>): Any {
        return jClass.enumConstants.first()
    }

    private fun createObject(kClass: KClass<*>): Any {
        val constructor = kClass.constructors
                .sortedByDescending { it.parameters.size }
                .firstOrNull { it.visibility == KVisibility.PUBLIC } ?: throw TestFactoryException("No suitable constructors: ${kClass.simpleName}")

        return createObject(constructor)
    }

    private fun createObject(constructor: KFunction<*>): Any {
        val arguments = constructor.parameters
                .map { parameter -> parameter.type }
                .map { parameterType -> create(parameterType) }
                .toTypedArray()
        return constructor.call(*arguments)!!
    }
}