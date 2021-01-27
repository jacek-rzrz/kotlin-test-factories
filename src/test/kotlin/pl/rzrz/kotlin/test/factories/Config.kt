package pl.rzrz.kotlin.test.factories

import pl.rzrz.kotlin.test.factories.core.TestFactoriesConfig

@TestFactoriesConfig(packageName = "pl.rzrz.test.factories.generated", value = [
    User::class,
    Address::class,
    ClassWithPrimitiveFields::class,
])
interface Config