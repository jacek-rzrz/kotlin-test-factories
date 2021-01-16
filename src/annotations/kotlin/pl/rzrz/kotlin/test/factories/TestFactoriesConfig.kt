package pl.rzrz.kotlin.test.factories

import kotlin.reflect.KClass

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class TestFactoriesConfig(
        val packageName: String,
        val className: String = "TestFactories",
        val value: Array<KClass<*>>
)