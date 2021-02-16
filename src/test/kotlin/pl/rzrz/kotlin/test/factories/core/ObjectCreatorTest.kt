package pl.rzrz.kotlin.test.factories.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.*

@Suppress("Unused")
class ObjectCreatorTest {

    @Test
    fun `numbers and strings`() {
        assertThat(ObjectCreator.create<Int>()).isEqualTo(0)
        assertThat(ObjectCreator.create<Long>()).isEqualTo(0L)
        assertThat(ObjectCreator.create<Double>()).isEqualTo(0.0)
        assertThat(ObjectCreator.create<Float>()).isEqualTo(0.0F)
        assertThat(ObjectCreator.create<Char>()).isEqualTo(' ')
        assertThat(ObjectCreator.create<BigDecimal>()).isEqualTo("0")
        assertThat(ObjectCreator.create<String>()).isEqualTo("")
    }

    @Test
    fun `class with a parameterless constructor`() {
        class Parameterless
        assertThat(ObjectCreator.create<Parameterless>()).isInstanceOf(Parameterless::class.java)
    }

    @Test
    fun `class without a parameterless constructor`() {
        class Parameterless
        class WithParameters(val p: Parameterless, val s: String)
        assertThat(ObjectCreator.create<WithParameters>()).isInstanceOf(WithParameters::class.java)
    }

    @Test
    fun `constructor with the most parameters is preferred`() {
        class WithManyConstructors(val constructorParamsCardinality: Int) {
            constructor(a: String, b: String) : this(2)
            constructor(a: String, b: String, c: String) : this(3)
            constructor(a: String) : this(1)
        }
        val instance = ObjectCreator.create<WithManyConstructors>()
        assertThat(instance.constructorParamsCardinality).isEqualTo(3)
    }

    @Test
    fun `class with private constructors`() {
        class WithPrivateConstructor private constructor (val arg: Int) {
            constructor() : this(10)
        }
        val instance = ObjectCreator.create<WithPrivateConstructor>()
        assertThat(instance.arg).isEqualTo(10)
    }

    @Test
    fun list() {
        data class WithListField(val list: List<String>)
        val instance = ObjectCreator.create<WithListField>()
        assertThat(instance).isEqualTo(WithListField(list = listOf("")))
    }

    @Test
    fun map() {
        data class WithMapField(val map: Map<String, Int>)
        val instance = ObjectCreator.create<WithMapField>()
        assertThat(instance).isEqualTo(WithMapField(map = mapOf("" to 0)))
    }

    enum class MyEnum { ONE, ANOTHER }

    @Test
    fun enums() {
        assertThat(ObjectCreator.create<MyEnum>()).isIn(MyEnum.ONE, MyEnum.ANOTHER)
    }

    @Test
    fun `java time classes`() {
        assertThat(ObjectCreator.create<Instant>()).isNotNull
        assertThat(ObjectCreator.create<Duration>()).isNotNull
        assertThat(ObjectCreator.create<LocalTime>()).isNotNull
        assertThat(ObjectCreator.create<LocalDateTime>()).isNotNull
        assertThat(ObjectCreator.create<LocalDate>()).isNotNull
        assertThat(ObjectCreator.create<ZonedDateTime>()).isNotNull
    }

    @Test
    fun `sealed classes`() {
        val instance = ObjectCreator.create<ASealedClass>()
        assertThat(instance).isInstanceOf(ASealedClassChild::class.java)
    }

    @Test
    fun `custom type factories`() {
        ObjectCreator.register(String::class) { "custom string" }
        val customInstance = ObjectCreator.create<String>()
        ObjectCreator.unregister(String::class)
        val normalInstance = ObjectCreator.create<String>()

        assertThat(customInstance).isEqualTo("custom string")
        assertThat(normalInstance).isEqualTo("")
    }
}

sealed class ASealedClass

class ASealedClassChild() : ASealedClass()
