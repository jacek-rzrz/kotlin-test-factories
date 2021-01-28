# ⌨️ kotlin-test-factories

Auto-generated test factory functions for Kotlin. Works with 
[KAPT](https://kotlinlang.org/docs/reference/kapt.html)
by dynamically creating source files.

⚠️ WARNING. IntelliJ Idea
[has not yet got a smooth KAPT support](https://youtrack.jetbrains.com/issue/KT-15040). 
When target classes change, do one of the following:

- Build the project (hit `CMD+F9` or `Ctrl+F9`). 
  IntelliJ has to be configured to [build with Gradle](https://www.jetbrains.com/help/idea/gradle.html#gradle_settings_access). 
- Invoke the `kaptTest` gradle task. 

## 🤷‍♀️ Why?
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
The code below looks clean...
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
...but it doesn't compile: the `address` constructor parameter isn't provided.
Specifying a random address would make the compiler happy
at the cost of developer happiness. It would only introduce noise to
a perfectly fine test.

A good test should be readable, focused and define clear inputs. And so is this one.

As a workaround one can write a *test factory* function:
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
With a test factory `User` instances can be obtained by specifying relevant fields
and omitting irrelevant ones. Our final test looks like this:
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

Job done! Here is a bummer though: writing and maintaining test factories
is laborious and more boring than
[scraping the internet for thousands of food pictures](https://www.youtube.com/watch?t=135&v=vIci3C4JkL0).
This library generates test factories automatically.

## 🏗 Usage
Add to `build.gradle.kts`:

```kotlin
plugins {
    kotlin("jvm") version "1.4.21"
    kotlin("kapt") version "1.4.21"
}

dependencies {
    testImplementation("pl.rzrz:kotlin-test-factories-core:VERSION") // annotations + support for generated factories
    kaptTest("pl.rzrz:kotlin-test-factories-generator:VERSION")      // generator
}
```

Create a configuration in test sources: 
```kotlin
package com.me

@TestFactoriesConfig([
    User::class
])
interface TestFactoriesConfiguration
```

Write tests:
```kotlin
import com.me.TestFactories.aUser

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


## 📃 Features

 - [x] Generate test factories
 - [x] Default package same as config
 - [x] Customizable package and class name
 - [ ] Support for generic types
 - [ ] Support for recursive types