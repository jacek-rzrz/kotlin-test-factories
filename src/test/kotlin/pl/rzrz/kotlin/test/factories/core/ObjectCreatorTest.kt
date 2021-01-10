package pl.rzrz.kotlin.test.factories.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@Suppress("Unused")
class ObjectCreatorTest {

    @Test
    fun `primitives and strings`() {
        assertThat(ObjectCreator.create<Int>()).isEqualTo(0)
        assertThat(ObjectCreator.create<Long>()).isEqualTo(0L)
        assertThat(ObjectCreator.create<Double>()).isEqualTo(0.0)
        assertThat(ObjectCreator.create<Float>()).isEqualTo(0.0F)
        assertThat(ObjectCreator.create<Char>()).isEqualTo(' ')
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
    fun `create a list`() {
        data class WithListField(val list: List<String>)
        val instance = ObjectCreator.create<WithListField>()
        assertThat(instance).isEqualTo(WithListField(list = listOf("")))
    }

    @Test
    fun `create a map`() {
        data class WithMapField(val map: Map<String, Int>)
        val instance = ObjectCreator.create<WithMapField>()
        assertThat(instance).isEqualTo(WithMapField(map = mapOf("" to 0)))
    }
}