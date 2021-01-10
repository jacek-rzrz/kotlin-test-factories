package pl.rzrz.kotlin.test.factories.generator

import com.squareup.kotlinpoet.*
import pl.rzrz.kotlin.test.factories.TestFactory
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("pl.rzrz.kotlin.test.factories.TestFactory")
@SupportedOptions(TestFactoryAnnotationProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
class TestFactoryAnnotationProcessor : AbstractProcessor() {

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment): Boolean {

        val annotatedElements = roundEnv.getElementsAnnotatedWith(TestFactory::class.java)
        if(annotatedElements.isEmpty()) {
            return false
        }

        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME] ?: run {
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Can't find the target directory for generated Kotlin files.")
            return false
        }

        processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "generated dir $kaptKotlinGeneratedDir")

        val testFactoriesObject = annotatedElements
                .mapNotNull(TestFactoryCreator::createFor)
                .fold(TypeSpec.objectBuilder("TestFactories"), TypeSpec.Builder::addFunction)
                .build()

        val file = FileSpec.builder("pl.rzrz.kotlin.test.factories", "TestFactories")
                .addType(testFactoriesObject)
                .build()

        file.writeTo(File(kaptKotlinGeneratedDir))

        return true
    }
}