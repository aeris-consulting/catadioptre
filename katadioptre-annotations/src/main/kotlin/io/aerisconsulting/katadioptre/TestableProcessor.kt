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
package io.aerisconsulting.katadioptre

import com.squareup.kotlinpoet.DelicateKotlinPoetApi
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.jvm.jvmName
import com.squareup.kotlinpoet.metadata.ImmutableKmClass
import com.squareup.kotlinpoet.metadata.ImmutableKmProperty
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.isNullable
import com.squareup.kotlinpoet.metadata.toImmutableKmClass
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
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements


/**
 *
 * Processor that generates source code to provide indirect access to private properties and functions.
 *
 * @author Eric Jessé
 */
@DelicateKotlinPoetApi("Awareness of delicate aspect")
@KotlinPoetMetadataPreview
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes(TestableProcessor.ANNOTATION_CLASS_NAME)
@SupportedOptions(TestableProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
internal class TestableProcessor : AbstractProcessor() {

    private lateinit var elementUtils: Elements

    private lateinit var generatedDirPath: Path

    private lateinit var specificationUtils: SpecificationUtils

    companion object {

        const val ANNOTATION_CLASS_NAME = "io.aerisconsulting.katadioptre.Testable"

        // Property pointing to the folder where Kapt generates sources.
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"

        private const val KATADIOPTRE_UTILS_PACKAGE_NAME = "io.aerisconsulting.katadioptre"
    }

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        elementUtils = processingEnv.elementUtils
        specificationUtils = SpecificationUtils(
            processingEnv.typeUtils,
            processingEnv.typeUtils.erasure(elementUtils.getTypeElement(Void::class.java.name).asType())
        )
    }

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val annotatedElements = roundEnv.getElementsAnnotatedWith(Testable::class.java)
        if (annotatedElements.isEmpty()) return false

        val kaptKotlinGeneratedDir =
            processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
                ?: return false
        generatedDirPath = Paths.get(Paths.get(kaptKotlinGeneratedDir).parent.toUri().path, "katadioptre")

        roundEnv.getElementsAnnotatedWith(Testable::class.java)
            .filter { it.kind == ElementKind.METHOD }
            .map { it as ExecutableElement }
            .groupBy { it.enclosingElement as TypeElement }
            .forEach { (enclosingElement, elements) ->
                val classContainer = enclosingElement.toImmutableKmClass()
                val packageName = elementUtils.getPackageOf(enclosingElement.enclosingElement)
                val testableClassName = "Testable" + enclosingElement.simpleName.toString()
                val testableClassFile = FileSpec.builder("${packageName}.katadioptre", testableClassName)
                    .addImport("$packageName", enclosingElement.simpleName.toString())

                elements.forEach { element ->
                    val annotation = element.getAnnotation(Testable::class.java)
                    val property = specificationUtils.findProperty(classContainer, element)
                    if (property != null) {
                        generateTestableProperty(
                            classContainer,
                            enclosingElement,
                            property,
                            testableClassFile,
                            annotation
                        )
                    } else {
                        generateTestableFunction(classContainer, enclosingElement, element, testableClassFile)
                    }
                }


                testableClassFile.build().writeTo(generatedDirPath)
            }

        return true
    }

    /**
     * Generates the functions to manipulate properties: getter, setter and clearer.
     */
    private fun generateTestableProperty(
        declaringType: ImmutableKmClass,
        enclosingElement: TypeElement,
        property: ImmutableKmProperty,
        testableClassFile: FileSpec.Builder,
        annotation: Testable
    ) {
        val propertyName = property.name
        val propertyTypeName = specificationUtils.createTypeName(declaringType, property.returnType)
        if (annotation.propertyGetter) {
            testableClassFile.addImport(KATADIOPTRE_UTILS_PACKAGE_NAME, "getProperty")
            testableClassFile.addFunction(
                FunSpec.builder(property.name)
                    .prepareFunctionForProperty(enclosingElement, declaringType, false)
                    .returns(propertyTypeName)
                    .addStatement("return this.getProperty(\"$propertyName\")")
                    .build()
            )
        }

        if (annotation.propertySetter) {
            testableClassFile.addImport(KATADIOPTRE_UTILS_PACKAGE_NAME, "setProperty")
            testableClassFile.addFunction(
                FunSpec.builder(propertyName)
                    .addParameter("value", propertyTypeName)
                    .addStatement("this.setProperty(\"$propertyName\", value)")
                    .prepareFunctionForProperty(enclosingElement, declaringType, true)
                    .build()
            )
        }

        if (annotation.propertyClearer && property.returnType.isNullable) {
            testableClassFile.addImport(KATADIOPTRE_UTILS_PACKAGE_NAME, "clearProperty")
            testableClassFile.addFunction(
                FunSpec.builder("clear" + propertyName.capitalize())
                    .addStatement("this.clearProperty(\"$propertyName\")")
                    .prepareFunctionForProperty(enclosingElement, declaringType, true)
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
        returnsDeclaring: Boolean = false
    ): FunSpec.Builder {
        receiver(enclosingElement.asType().asTypeName())
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
        testableClassFile: FileSpec.Builder
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
        if (kmFunction.valueParameters.isEmpty()) {
            testableClassFile.addImport(KATADIOPTRE_UTILS_PACKAGE_NAME, "invokeNoArgs")
            functionBuilder = functionBuilder.addStatement("""return this.invokeNoArgs("${kmFunction.name}")""")
        } else {
            testableClassFile.addImport(KATADIOPTRE_UTILS_PACKAGE_NAME, "invokeInvisible", "named")
            var statement = """return this.invokeInvisible("${kmFunction.name}", """
            statement += kmFunction.valueParameters.joinToString(", ") { arg ->
                """named("${arg.name}", ${arg.name})"""
            } + ")"
            functionBuilder = functionBuilder.addStatement(statement)
        }
        testableClassFile.addFunction(functionBuilder.build())
    }

}
