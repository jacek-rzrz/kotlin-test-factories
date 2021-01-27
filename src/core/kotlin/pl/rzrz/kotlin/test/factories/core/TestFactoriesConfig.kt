package pl.rzrz.kotlin.test.factories.core

import kotlin.reflect.KClass

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class TestFactoriesConfig(

        /**
         * Target classes to generate factory methods for
         */
        val value: Array<KClass<*>>,

        /**
         * Test factories package.
         * When not specified, the package of configuration class is used.
         */
        val packageName: String = "",

        /**
         * Test factories host object name.
         */
        val className: String = "TestFactories",
)