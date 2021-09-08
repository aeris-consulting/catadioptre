/*
 * Copyright 2021 AERIS-Consulting e.U.
 *
 * AERIS-Consulting e.U. licenses this file to you under the Apache License, version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.aerisconsulting.catadioptre.kotlin

import com.squareup.kotlinpoet.DelicateKotlinPoetApi
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.jvm.jvmName
import com.squareup.kotlinpoet.metadata.ImmutableKmClass
import com.squareup.kotlinpoet.metadata.ImmutableKmProperty
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.isInternal
import com.squareup.kotlinpoet.metadata.isNullable
import com.squareup.kotlinpoet.metadata.toImmutableKmClass
import io.aerisconsulting.catadioptre.KTestable
import java.nio.file.Path
import java.nio.file.Paths
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.annotation.processing.SupportedOptions
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.tools.Diagnostic


/**
 *
 * Processor that generates source code to provide indirect access to private properties and functions.
 *
 * @author Eric Jessé
 */
@DelicateKotlinPoetApi("Awareness of delicate aspect")
@KotlinPoetMetadataPreview
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes(KotlinTestableProcessor.ANNOTATION_CLASS_NAME)
@SupportedOptions(KotlinTestableProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
internal class KotlinTestableProcessor : AbstractProcessor() {

    private lateinit var elementUtils: Elements

    private lateinit var generatedDirPath: Path

    private lateinit var specificationUtils: KotlinSpecificationUtils

    private lateinit var kotlinVisibilityUtils: KotlinVisibilityUtils

    companion object {

        const val ANNOTATION_CLASS_NAME = "io.aerisconsulting.catadioptre.KTestable"

        // Property pointing to the folder where Kapt generates sources.
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"

        private const val CATADIOPTRE_UTILS_PACKAGE_NAME = "io.aerisconsulting.catadioptre"
    }

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        elementUtils = processingEnv.elementUtils
        kotlinVisibilityUtils = KotlinVisibilityUtils(processingEnv.typeUtils)
        specificationUtils = KotlinSpecificationUtils(
            processingEnv.typeUtils,
            processingEnv.typeUtils.erasure(elementUtils.getTypeElement(Void::class.java.name).asType())
        )
    }

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val annotatedElements = roundEnv.getElementsAnnotatedWith(KTestable::class.java)
        if (annotatedElements.isEmpty()) return false

        val kaptKotlinGeneratedDir =
            processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
                ?: return false
        generatedDirPath = Paths.get(Paths.get(kaptKotlinGeneratedDir).parent.toUri().path, "catadioptre")

        annotatedElements
            .filter { it.kind == ElementKind.METHOD }
            .map { it as ExecutableElement }
            .groupBy { it.enclosingElement as TypeElement }
            .forEach { (enclosingElement, elements) ->
                val classContainer = enclosingElement.toImmutableKmClass()
                val packageName = elementUtils.getPackageOf(enclosingElement.enclosingElement)
                val testableClassName = "Testable" + enclosingElement.simpleName.toString()

                val testableClassFile = FileSpec.builder("${packageName}.catadioptre", testableClassName)
                    .addImport("$packageName", enclosingElement.simpleName.toString())
                generatesProxyMethods(
                    enclosingElement,
                    classContainer,
                    elements,
                    testableClassFile
                )
                testableClassFile.build().writeTo(generatedDirPath)
            }

        return true
    }

    /**
     * Returns the [KClassVisibility] of the parameter or the container class.
     */
    private fun collectDeclaredVisibilities(
        typeElement: TypeElement,
        visibilities: MutableList<KClassVisibility>
    ) {
        visibilities += when {
            Modifier.PUBLIC in typeElement.modifiers -> KClassVisibility.PUBLIC
            typeElement.toImmutableKmClass().isInternal -> KClassVisibility.INTERNAL
            else -> KClassVisibility.PRIVATE
        }

        typeElement.typeParameters.forEach {
            collectDeclaredVisibilities(elementUtils.getTypeElement(it.toString()), visibilities)
        }
    }

    /**
     * Builds the extension functions to access the annotated members in the class.
     *
     * @param enclosingElement class declaring the annotated elements
     * @param classContainer representation of [enclosingElement] as an [ImmutableKmClass]
     * @param elements annotated elements for which extension functions have to be generated
     * @param testableClassFile specification for the file that will contain the extension functions
     * @param classVisibilityModifier visibility modifier to apply on the extension functions
     */
    private fun generatesProxyMethods(
        enclosingElement: TypeElement,
        classContainer: ImmutableKmClass,
        elements: List<ExecutableElement>,
        testableClassFile: FileSpec.Builder
    ) {
        elements.forEach { element ->
            val methodVisibility = kotlinVisibilityUtils.detectLowestVisibility(element)
            val property = specificationUtils.findProperty(classContainer, element)
            if (methodVisibility != KModifier.PRIVATE) {
                val annotation = element.getAnnotation(KTestable::class.java)

                if (property != null) {
                    generateTestableProperty(
                        classContainer,
                        enclosingElement,
                        property,
                        testableClassFile,
                        annotation,
                        methodVisibility
                    )
                } else {
                    generateTestableFunction(
                        classContainer,
                        enclosingElement,
                        element,
                        testableClassFile,
                        methodVisibility
                    )
                }
            } else {
                if (property != null) {
                    processingEnv.messager.printMessage(
                        Diagnostic.Kind.WARNING,
                        "Cannot generate the Catadioptre proxy method for the property " +
                                "${enclosingElement.asClassName()}.${property.name}, " +
                                "the type of the receiver or the property has a too low visibility"
                    )
                } else {
                    val kmFunction = specificationUtils.findFunction(classContainer, element)
                    processingEnv.messager.printMessage(
                        Diagnostic.Kind.WARNING,
                        "Cannot generate the Catadioptre proxy method for the function " +
                                "${enclosingElement.asClassName()}.${kmFunction.signature}, " +
                                "one of the used type has a too low visibility"
                    )
                }
            }
        }
    }

    /**
     * Generates the functions to manipulate properties: getter, setter and clearer.
     */
    private fun generateTestableProperty(
        declaringType: ImmutableKmClass,
        enclosingElement: TypeElement,
        property: ImmutableKmProperty,
        testableClassFile: FileSpec.Builder,
        annotation: KTestable,
        visibility: KModifier
    ) {
        val propertyName = property.name
        val propertyTypeName = specificationUtils.createTypeName(declaringType, property.returnType)
        if (annotation.getter) {
            testableClassFile.addImport(CATADIOPTRE_UTILS_PACKAGE_NAME, "getProperty")
            testableClassFile.addFunction(
                FunSpec.builder(property.name)
                    .prepareFunctionForProperty(enclosingElement, declaringType, visibility, false)
                    .returns(propertyTypeName)
                    .addStatement("return this.getProperty(\"$propertyName\")")
                    .build()
            )
        }

        if (annotation.setter) {
            testableClassFile.addImport(CATADIOPTRE_UTILS_PACKAGE_NAME, "setProperty")
            testableClassFile.addFunction(
                FunSpec.builder(propertyName)
                    .addParameter("value", propertyTypeName)
                    .addStatement("this.setProperty(\"$propertyName\", value)")
                    .prepareFunctionForProperty(enclosingElement, declaringType, visibility, true)
                    .build()
            )
        }

        if (annotation.clearer && property.returnType.isNullable) {
            testableClassFile.addImport(CATADIOPTRE_UTILS_PACKAGE_NAME, "clearProperty")
            testableClassFile.addFunction(
                FunSpec.builder("clear" + propertyName.capitalize())
                    .addStatement("this.clearProperty(\"$propertyName\")")
                    .prepareFunctionForProperty(enclosingElement, declaringType, visibility, true)
                    .build()
            )
        }
    }

    /**
     * Prepares the specification for a function to manipulate a property.
     */
    private fun FunSpec.Builder.prepareFunctionForProperty(
        enclosingElement: TypeElement,
        declaringType: ImmutableKmClass,
        visibility: KModifier,
        returnsDeclaring: Boolean = false
    ): FunSpec.Builder {
        addModifiers(visibility)
            .receiver(enclosingElement.asType().asTypeName())
            .addTypeVariables(declaringType.typeParameters.map {
                specificationUtils.createTypeNameForTypeParameter(
                    declaringType,
                    it
                )
            })

        if (returnsDeclaring) {
            returns(enclosingElement.asType().asTypeName())
                .addStatement("return this")
        }
        return this
    }

    /**
     * Generates the specification for the extension function that calls the invisible one, using reflection,
     * while keeping the same signature.
     */
    private fun generateTestableFunction(
        declaringType: ImmutableKmClass,
        enclosingElement: TypeElement,
        function: ExecutableElement,
        testableClassFile: FileSpec.Builder,
        visibility: KModifier
    ) {
        val kmFunction = specificationUtils.findFunction(declaringType, function)
        var functionBuilder = FunSpec.builder(kmFunction.name)
            .receiver(enclosingElement.asType().asTypeName())
            .jvmName(function.simpleName.toString())
            .addTypeVariables((kmFunction.typeParameters + declaringType.typeParameters).map {
                specificationUtils.createTypeNameForTypeParameter(declaringType, it)
            })
            .returns(function.returnType.asTypeName())

        kmFunction.valueParameters.forEach { arg ->
            functionBuilder =
                functionBuilder.addParameter(
                    arg.name, specificationUtils.createTypeName(declaringType, arg.type!!)
                )
        }
        functionBuilder.addModifiers(visibility)

        if (kmFunction.valueParameters.isEmpty()) {
            testableClassFile.addImport(CATADIOPTRE_UTILS_PACKAGE_NAME, "invokeNoArgs")
            functionBuilder = functionBuilder.addStatement("""return this.invokeNoArgs("${kmFunction.name}")""")
        } else {
            testableClassFile.addImport(CATADIOPTRE_UTILS_PACKAGE_NAME, "invokeInvisible", "named")
            var statement = """return this.invokeInvisible("${kmFunction.name}", """
            statement += kmFunction.valueParameters.joinToString(", ") { arg ->
                """named("${arg.name}", ${arg.name})"""
            } + ")"
            functionBuilder = functionBuilder.addStatement(statement)
        }
        testableClassFile.addFunction(functionBuilder.build())
    }

}
