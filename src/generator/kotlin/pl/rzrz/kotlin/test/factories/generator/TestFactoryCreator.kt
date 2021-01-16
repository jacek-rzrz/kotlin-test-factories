package pl.rzrz.kotlin.test.factories.generator

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.jetbrains.annotations.Nullable
import pl.rzrz.kotlin.test.factories.core.ObjectCreator
import pl.rzrz.kotlin.test.factories.generator.TestFactoryCreator.genericTypes
import javax.lang.model.element.*
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror

object TestFactoryCreator {

    fun createFor(typeMirror: TypeMirror): FunSpec {
        val typeName = typeMirror.simpleClassName()
        val constructor = typeMirror.constructor()
        val parameters = constructor.parameters.map { parameterSpec(it) }
        val constructorArgs = constructor.parameters.map { it.simpleName.toString() }
                .joinToString(separator = ",") { parameter ->
                    "$parameter = $parameter"
                }

        return FunSpec.builder("a$typeName")
                .returns(typeMirror.typeName())
                .addParameters(parameters)
                .addStatement("return %T($constructorArgs)", typeMirror.typeName())
                .build()
    }

    private fun parameterSpec(element: VariableElement): ParameterSpec {
        val typeName = element.typeName()
        return ParameterSpec.builder(element.simpleName.toString(), typeName)
                .defaultValue(CodeBlock.of("%T.create<%T>()", ObjectCreator::class.asClassName(), typeName))
                .build()
    }

    private fun TypeMirror.qualifiedNameWithoutGenericParameters(): String {
        return qualifiedName().split('<').first()
    }

    private fun TypeMirror.qualifiedName(): String {
        val type = toString()
        return type
                .replace("java.lang.String", "kotlin.String")
                .replace("java.util.List", "kotlin.collections.List")
    }

    private fun TypeMirror.simpleClassName(): String {
        return qualifiedName().split("<").first().substringAfterLast(".")
    }

    private fun Element.typeName(): TypeName {
        val annotation = getAnnotation(Nullable::class.java)
        val typeName = asType().typeName()
        return if(annotation == null) typeName else typeName.copy(nullable = true)
    }

    private fun TypeMirror.typeName(): TypeName {
        val genericTypes = genericTypes()
        val className = ClassName(packageName(), simpleClassName())
        return if(genericTypes.isEmpty()) className else className.parameterizedBy(genericTypes)
    }

    private fun TypeMirror.packageName(): String {
        return qualifiedNameWithoutGenericParameters().substringBeforeLast(".")
    }

    private fun TypeMirror.genericTypes(): List<TypeName> {
        return when(kind) {
            TypeKind.DECLARED -> (this as DeclaredType).typeArguments.map { it.typeName() }
            else -> emptyList()
        }
    }

    private fun Element.constructors(): List<ExecutableElement> {
        return enclosedElements
                .filter { it.kind == ElementKind.CONSTRUCTOR }
                .map { it as ExecutableElement }
    }

    private fun Element.constructor(): ExecutableElement {
        return constructors().first()
    }

    private fun TypeMirror.constructor(): ExecutableElement {
        return when(kind) {
            TypeKind.DECLARED -> (this as DeclaredType).asElement().constructor()
            else -> throw Exception("$kind")
        }
    }
}