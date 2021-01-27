package pl.rzrz.kotlin.test.factories

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import pl.rzrz.kotlin.test.factories.TestFactories.aClassWithPrimitiveFields
import pl.rzrz.kotlin.test.factories.TestFactories.aUser

data class User(
        val firstName: String?,
        val lastName: String?,
        val addresses: List<Address>,
) {

    fun name(): String = "$firstName $lastName"
}

data class Address(
        val firstLine: String,
        val secondLine: String
)

data class ClassWithPrimitiveFields(
        val intField: Int,
        val longField: Long,
        val charField: Char,
        val floatField: Float,
        val doubleField: Double,
)

class TestFactoriesIntegrationTest {

    @Test
    fun `create an instance with test factory`() {
        val user = aUser(
                firstName = "John",
                lastName = "Smith"
        )

        assertThat(user.name()).isEqualTo("John Smith")
    }

    @Test
    fun `create an instance with null field`() {
        val user = aUser(
                firstName = null
        )

        assertThat(user.firstName).isNull()
    }

    @Test
    fun `instantiate a class with primitive fields`() {
        val primitives = aClassWithPrimitiveFields(
                doubleField = 10.1,
        )

        assertThat(primitives.doubleField).isEqualTo(10.1)
    }
}