package pl.rzrz.kotlin.test.factories.generator

import com.squareup.kotlinpoet.*
import pl.rzrz.kotlin.test.factories.core.ObjectCreator
import pl.rzrz.kotlin.test.factories.core.RuntimeCustomisations
import pl.rzrz.kotlin.test.factories.core.TestFactoriesConfig
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.MirroredTypesException
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic
import kotlin.reflect.jvm.javaMethod

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("pl.rzrz.kotlin.test.factories.core.TestFactoriesConfig")
@SupportedOptions(TestFactoryAnnotationProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
class TestFactoryAnnotationProcessor : AbstractProcessor() {

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment): Boolean {
        val output = outputDir() ?: return false
        val annotatedElements = roundEnv.getElementsAnnotatedWith(TestFactoriesConfig::class.java)
        annotatedElements.forEach { processAnnotatedElement(it, output) }
        return annotatedElements.isNotEmpty()
    }

    private val info = log(Diagnostic.Kind.NOTE)

    private val error = log(Diagnostic.Kind.ERROR)

    private fun log(level: Diagnostic.Kind) = { message: String ->
        processingEnv.messager.printMessage(level, message + "\r\n")
    }

    private fun processAnnotatedElement(annotatedElement: Element, outputDir: File) {
        info("Processing " + annotatedElement.simpleName)
        val annotation = annotatedElement.getAnnotation(TestFactoriesConfig::class.java)

        val customised = processingEnv.typeUtils.directSupertypes(annotatedElement.asType())
                .map { it as DeclaredType }
                .map { it.asElement().simpleName.toString() }
                .contains(RuntimeCustomisations::class.simpleName)

        val targetTypes = annotation.targetTypes()
        val configurationPackage = processingEnv.elementUtils.getPackageOf(annotatedElement).toString()
        val packageName = if(annotation.packageName.isNotBlank())
            annotation.packageName
        else
            configurationPackage

        val testFactoriesObject = targetTypes
                .map {
                    info("Generating test factory for $it")
                    TestFactoryCreator.createFor(it)
                }
                .fold(TypeSpec.objectBuilder(annotation.className), TypeSpec.Builder::addFunction)
                .apply {
                    if(customised) {
                        addInitializerBlock(CodeBlock.of("%T.${RuntimeCustomisations::applyRuntimeCustomisations.javaMethod!!.name}(%T)\n",
                                annotatedElement.asType(), ObjectCreator::class.asClassName())
                        )
                    }
                }
                .build()

        val file = FileSpec.builder(packageName, annotation.className)
                .addType(testFactoriesObject)
                .build()

        file.writeTo(outputDir)
    }

    private fun outputDir(): File? {
        val fileName = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME] ?: run {
            error("Can't find the target directory for generated Kotlin files.")
            return null
        }
        info("Generated dir $fileName")
        return File(fileName)
    }

    private fun TestFactoriesConfig.targetTypes(): List<TypeMirror> = try {
        value
        throw Exception("Expected to get a MirroredTypeException")
    } catch (e: MirroredTypesException) {
        e.typeMirrors
    }
}