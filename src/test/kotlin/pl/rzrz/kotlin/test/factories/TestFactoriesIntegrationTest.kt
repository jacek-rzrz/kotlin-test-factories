package pl.rzrz.kotlin.test.factories

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import pl.rzrz.kotlin.test.factories.TestFactories.aUser

data class Address(
        val firstLine: String,
        val secondLine: String
)

@TestFactory
data class User(
        val firstName: String,
        val lastName: String,
        val address: Address
)

class TestFactoriesIntegrationTest {

    @Test
    fun `create an instance with test factory`() {
        val user = aUser(
                firstName = "John"
        )

        assertThat(user.firstName).isEqualTo("John")
    }
}