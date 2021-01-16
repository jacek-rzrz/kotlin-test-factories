package pl.rzrz.kotlin.test.factories

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import pl.rzrz.test.factories.generated.TestFactories.aUser

@TestFactoriesConfig(packageName = "pl.rzrz.test.factories.generated", value = [
    User::class,
    Address::class
])
interface Config

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
    fun `support nullable fields`() {
        val user = aUser(
                firstName = null
        )

        assertThat(user.firstName).isNull()
    }
}