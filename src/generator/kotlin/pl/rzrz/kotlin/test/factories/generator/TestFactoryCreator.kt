package pl.rzrz.kotlin.test.factories.generator

import com.squareup.kotlinpoet.*
import pl.rzrz.kotlin.test.factories.core.ObjectCreator
import javax.lang.model.element.*

object TestFactoryCreator {

    fun createFor(element: Element): FunSpec {
        val typeName = element.simpleName.toString()
        val constructor = element.constructor()
        val parameters = constructor.parameters.map { parameterSpec(it) }
        val constructorArgs = constructor.parameters.map { it.simpleName.toString() }
                .joinToString(separator = ",") { parameter ->
                    "$parameter = $parameter"
                }

        return FunSpec.builder("a$typeName")
                .returns(element.className())
                .addParameters(parameters)
                .addStatement("return %T($constructorArgs)", element.className())
                .build()
    }

    private fun parameterSpec(element: VariableElement): ParameterSpec {
        return ParameterSpec.builder(element.simpleName.toString(), element.className())
                .defaultValue(CodeBlock.of("%T.create<%T>()", ObjectCreator::class.asClassName(), element.className()))
                .build()
    }

    private fun Element.qualifiedName(): String {
        val type = asType().toString()
        if(type == "java.lang.String") {
            return "kotlin.String"
        }
        return type
    }

    private fun Element.simpleClassName(): String {
        return qualifiedName().substringAfterLast(".")
    }

    private fun Element.className(): ClassName {
        return ClassName(packageName(), simpleClassName())
    }

    private fun Element.packageName(): String {
        return qualifiedName().substringBeforeLast(".")
    }

    private fun Element.constructors(): List<ExecutableElement> {
        return enclosedElements
                .filter { it.kind == ElementKind.CONSTRUCTOR }
                .map { it as ExecutableElement }
    }

    private fun Element.constructor(): ExecutableElement {
        return constructors().first()
    }
}