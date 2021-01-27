package pl.rzrz.kotlin.test.factories

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import pl.rzrz.kotlin.test.factories.custompackage.CustomTestFactories.aCustomisedConfigTargetClass

class CustomisedConfigIntegrationTest {

    @Test
    fun `package and class name can be customised`() {
        assertThat(aCustomisedConfigTargetClass()).isInstanceOf(CustomisedConfigTargetClass::class.java)
    }
}