package pl.rzrz.kotlin.test.factories

import pl.rzrz.kotlin.test.factories.core.TestFactoriesConfig

data class CustomisedConfigTargetClass(val value: String)

@TestFactoriesConfig(
        packageName = "pl.rzrz.kotlin.test.factories.custompackage",
        className = "CustomTestFactories",
        value = [
            CustomisedConfigTargetClass::class,
        ])
interface CustomisedConfig