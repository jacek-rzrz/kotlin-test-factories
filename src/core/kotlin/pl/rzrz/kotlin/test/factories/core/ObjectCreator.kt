package pl.rzrz.kotlin.test.factories.core

import java.math.BigDecimal
import java.net.URI
import java.net.URL
import java.time.*
import java.util.*
import kotlin.reflect.*

object ObjectCreator {

    private val customFactoryByClass = mutableMapOf<KClass<*>, () -> Any>()

    inline fun <reified T> create(): T {
        @OptIn(ExperimentalStdlibApi::class)
        return create(T::class, typeOf<T>()) as T
    }

    private fun create(kType: KType): Any {
        return create(kType.classifier as KClass<*>, kType)
    }

    fun create(kClass: KClass<*>, kType: KType): Any {
        val customFactoryResult = createWithCustomFactory(kClass)
        if (customFactoryResult != null) {
            return customFactoryResult
        }

        return try {
            matchAndCreate(kClass, kType)
        } catch (e: Exception) {
            throw TestFactoryException("Error creating ${kClass.simpleName}", e)
        }
    }

    private fun createWithCustomFactory(kClass: KClass<*>): Any? {
        val factory = customFactoryByClass[kClass] ?: return null
        return factory()
    }


    private fun matchAndCreate(kClass: KClass<*>, kType: KType): Any {

        val jClass = kClass.java

        if (jClass.isEnum) return getEnumValue(jClass)

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
            UUID::class -> UUID.randomUUID()
            URI::class -> URI.create("https://example.com")
            URL::class -> URL("https://example.com")
            else -> createObject(kClass)
        }
    }

    private fun getEnumValue(jClass: Class<out Any>): Any {
        return jClass.enumConstants.first()
    }

    private fun createObject(kClass: KClass<*>, constructors: List<KFunction<Any>>): Any {
        val constructor = constructors.firstOrNull()
                ?: throw TestFactoryException("No suitable constructors: ${kClass.simpleName}")

        return try {
            createObject(constructor)
        } catch(e: Exception) {
            createObject(kClass, constructors.drop(1))
        }
    }

    private fun createObject(kClass: KClass<*>): Any {
        if (kClass.isSealed) return createObject(kClass.sealedSubclasses.first())

        val constructors = kClass.constructors
                .filter { it.visibility == KVisibility.PUBLIC }
                .sortedByDescending { it.parameters.size }

        return createObject(kClass, constructors)
    }

    private fun createObject(constructor: KFunction<*>): Any {
        val arguments = constructor.parameters
                .map { parameter -> parameter.type }
                .map { parameterType -> create(parameterType) }
                .toTypedArray()
        return constructor.call(*arguments)!!
    }

    fun <T : Any> register(kClass: KClass<T>, customFactory: () -> T) {
        customFactoryByClass[kClass] = customFactory
    }

    fun unregister(kClass: KClass<*>) {
        customFactoryByClass.remove(kClass)
    }


}