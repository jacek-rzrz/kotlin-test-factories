package pl.rzrz.kotlin.test.factories

@TestFactoriesConfig(packageName = "pl.rzrz.test.factories.generated", value = [
    User::class,
    Address::class,
    ClassWithPrimitiveFields::class,
])
interface Config