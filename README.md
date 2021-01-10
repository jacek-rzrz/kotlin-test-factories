# kotlin-test-factories

Auto-generated test factory functions for Kotlin.

## Usage
Add to `build.gradle.kts`:

```kotlin
plugins {
    kotlin("jvm") version "1.4.21"
    kotlin("kapt") version "1.4.21"
}

dependencies {
    kapt("pl.rzrz:kotlin-test-factories-generator:VERSION")
    implementation("pl.rzrz:kotlin-test-factories-core:VERSION")
}
```

Annotate classes with `@TestFactory` to create functions: 
```kotlin
import pl.rzrz.kotlin.test.factories.TestFactories.aUser

@TestFactory
data class User(
        val firstName: String,
        val lastName: String,
        val address: Address
) {

    fun name(): String = "$firstName $lastName"
}

data class Address(
        val firstLine: String,
        val secondLine: String
)

class UserTest {

    @Test
    fun name() {
        val user = aUser(
                firstName = "John",
                lastName = "Smith"
        )

        assertThat(user.name()).isEqualTo("John Smith")
    }
}
```