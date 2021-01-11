package pl.rzrz.kotlin.test.factories.generator

import com.squareup.kotlinpoet.*
import org.jetbrains.annotations.Nullable
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
                .returns(element.typeName())
                .addParameters(parameters)
                .addStatement("return %T($constructorArgs)", element.typeName())
                .build()
    }

    private fun parameterSpec(element: VariableElement): ParameterSpec {
        return ParameterSpec.builder(element.simpleName.toString(), element.typeName())
                .defaultValue(CodeBlock.of("%T.create<%T>()", ObjectCreator::class.asClassName(), element.typeName()))
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

    private fun Element.typeName(): TypeName {
        val annotation = getAnnotation(Nullable::class.java)
        val typeName = ClassName(packageName(), simpleClassName())
        return if(annotation == null) typeName else typeName.copy(nullable = true)
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