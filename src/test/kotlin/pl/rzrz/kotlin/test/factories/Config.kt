package pl.rzrz.kotlin.test.factories

import pl.rzrz.kotlin.test.factories.core.TestFactoriesConfig

@TestFactoriesConfig([
    User::class,
    Address::class,
    ClassWithPrimitiveFields::class,
])
interface Config