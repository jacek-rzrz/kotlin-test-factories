# kotlin-test-factories

Auto-generated test factory functions for Kotlin. Works with 
[KAPT](https://kotlinlang.org/docs/reference/kapt.html)
by dynamically creating source files.

⚠️ WARNING. IntelliJ Idea
[has not yet got support](https://youtrack.jetbrains.com/issue/KT-15040) 
for KAPT. That means you have to invoke the `kaptTest` gradle task
by hand each time you change your model. 

## Why?
How to write a test for the `name` method?
```kotlin
data class User(
        val firstName: String, 
        val lastName: String, 
        val address: Address
) { 
    fun name(): String = "$firstName $lastName"
}
```
The code below looks clean:
```kotlin
@Test
fun name() {
    val user = User(
            firstName = "John",
            lastName = "Smith"
    )

    assertThat(user.name()).isEqualTo("John Smith")
}
```
This test doesn't compile because the `address` parameter isn't specified.
It shouldn't be specified because it's irrelevant to the `name` method.
It would just add noise to the test.

A good test should be readable, focused and define clear inputs.

A common workaround is a *test factory* function:
```kotlin
fun aUser(
        firstName: String = "",
        lastName: String = "",
        address: Address = anAddress()
): User {
    return User(
            firstName = firstName,
            lastName = lastName,
            address = address
    )
}
```
With a test factory, the final test can look like this:
```kotlin
@Test
fun name() {
    val user = aUser(
            firstName = "John",
            lastName = "Smith"
    )

    assertThat(user.name()).isEqualTo("John Smith")
}
```

Writing and maintaining test factories is a laborious task so this tool
generates them automatically.

## Usage
Add to `build.gradle.kts`:

```kotlin
plugins {
    kotlin("jvm") version "1.4.21"
    kotlin("kapt") version "1.4.21"
}

dependencies {
    testImplementation("pl.rzrz:kotlin-test-factories-core:VERSION")    // supports generated factories
    kaptTest("pl.rzrz:kotlin-test-factories-generator:VERSION")         // generator
}
```

Create a configuration in test sources: 
```kotlin
@TestFactoriesConfig(packageName = "com.example", value = [
    User::class
])
interface TestFactoriesConfiguration
```

Write tests:
```kotlin
import com.example.TestFactories.aUser

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


## Features

 - [x] Generate test factories
 - [x] Customizable package and class name
 - [ ] Handling of recursive data structures
 - [ ] Single dependency?