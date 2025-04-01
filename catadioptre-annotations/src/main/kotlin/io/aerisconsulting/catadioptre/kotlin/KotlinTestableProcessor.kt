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
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.jvm.jvmName
import com.squareup.kotlinpoet.metadata.classinspectors.ElementsClassInspector
import com.squareup.kotlinpoet.metadata.specs.ClassInspector
import com.squareup.kotlinpoet.metadata.specs.toTypeSpec
import io.aerisconsulting.catadioptre.KTestable
import java.io.File
import java.util.Locale
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.annotation.processing.SupportedOptions
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic

/**
 *
 * Processor that generates source code to provide indirect access to private properties and functions.
 *
 * @author Eric Jessé
 */
@DelicateKotlinPoetApi("Awareness of delicate aspect")
@SupportedAnnotationTypes(KotlinTestableProcessor.ANNOTATION_CLASS_NAME)
@SupportedOptions(KotlinTestableProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
internal class KotlinTestableProcessor : AbstractProcessor() {

    private lateinit var typeUtils: Types

    private lateinit var elementUtils: Elements

    private lateinit var generatedDir: File

    private lateinit var specificationUtils: KotlinSpecificationUtils

    private lateinit var kotlinVisibilityUtils: KotlinVisibilityUtils

    private lateinit var classInspector: ClassInspector

    companion object {

        const val ANNOTATION_CLASS_NAME = "io.aerisconsulting.catadioptre.KTestable"

        // Property pointing to the folder where Kapt generates sources.
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"

        private const val CATADIOPTRE_UTILS_PACKAGE_NAME = "io.aerisconsulting.catadioptre"
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        // The maximal supported is the 21. But when running with a lower JDK, the enum SourceVersion.RELEASE_21
        // does not exist. So a fallback is done onto the latest source version of the current JDK.
        return SourceVersion.entries.firstOrNull { it.name == "RELEASE_21" } ?: SourceVersion.latestSupported()
    }

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        elementUtils = processingEnv.elementUtils
        typeUtils = processingEnv.typeUtils
        specificationUtils = KotlinSpecificationUtils(
            typeUtils.erasure(elementUtils.getTypeElement(Void::class.java.name).asType())
        )
        runCatching {
            // When the processing is initialized in a pure Java environment, the processor is created, but the
            // inspector cannot be created. Which does not matter, since there is no
            // KTestable to run.
            classInspector = ElementsClassInspector.create(true, elementUtils, typeUtils)
            kotlinVisibilityUtils = KotlinVisibilityUtils(classInspector, elementUtils)
        }
    }

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val annotatedElements = roundEnv.getElementsAnnotatedWith(KTestable::class.java)
        if (annotatedElements.isEmpty()) return false

        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME] ?: return false
        generatedDir = File(File(kaptKotlinGeneratedDir).parentFile, "catadioptre")
        annotatedElements
            .filter { it.kind == ElementKind.METHOD }
            .map { it as ExecutableElement }
            .groupBy { it.enclosingElement }
            .forEach { (enclosingElement, elements) ->
                val typeSpec = (enclosingElement as TypeElement).toTypeSpec(
                    lenient = true,
                    classInspector = classInspector
                )
                if (typeSpec.isCompanion || typeSpec.kind == TypeSpec.Kind.OBJECT) {
                    processingEnv.messager.printMessage(
                        Diagnostic.Kind.WARNING,
                        "No Catadioptre proxy could be generated for the members of ${enclosingElement.asClassName()}, because object types are not supported yet."
                    )
                } else {
                    val packageName = elementUtils.getPackageOf(enclosingElement.enclosingElement)
                    val testableClassName = "Testable" + enclosingElement.simpleName.toString()
                    val testableClassFile = FileSpec.builder("${packageName}.catadioptre", testableClassName)
                        .addImport("$packageName", enclosingElement.simpleName.toString())
                    generatesProxyMethods(
                        enclosingElement,
                        typeSpec,
                        elements,
                        testableClassFile
                    )
                    testableClassFile.build().writeTo(generatedDir)
                }
            }

        return true
    }

    /**
     * Builds the extension functions to access the annotated members in the class.
     *
     * @param enclosingElement class declaring the annotated elements
     * @param typeSpec the KotlinPoet [TypeSpec] corresponding to the [enclosingElement]
     * @param elements annotated elements for which extension functions have to be generated
     * @param testableClassFile specification for the file that will contain the extension functions
     */
    private fun generatesProxyMethods(
        enclosingElement: TypeElement,
        typeSpec: TypeSpec,
        elements: List<ExecutableElement>,
        testableClassFile: FileSpec.Builder
    ) {
        val remainingElements = elements.toMutableList()
        val (receiverTypeElement, receiverSpec) = if (typeSpec.isCompanion) {
            val typeElement = enclosingElement.enclosingElement as TypeElement
            typeElement to typeElement.toTypeSpec(true, classInspector)
        } else {
            enclosingElement to typeSpec
        }
        // Generates the proxies for each function of the type.
        elements.mapNotNull { element ->
            val method = specificationUtils.findFunction(typeSpec, element)
            method?.let {
                remainingElements.remove(element)
                element to method
            }
        }.forEach { (element, function) ->
            val methodVisibility = kotlinVisibilityUtils.detectLowestVisibility(typeSpec, element)
            if (methodVisibility != KModifier.PRIVATE) {
                generateTestableFunction(
                    typeElement = receiverTypeElement,
                    typeSpec = receiverSpec,
                    function = function,
                    visibility = methodVisibility,
                    testableClassFile = testableClassFile
                )
            } else {
                processingEnv.messager.printMessage(
                    Diagnostic.Kind.WARNING,
                    "No Catadioptre proxy could be generated for the function ${enclosingElement.asClassName()}.${function.element}, because one of the used types is private"
                )
            }
        }

        // Generates the proxies for each annotates property of the type.
        elements.mapNotNull { element ->
            val property = specificationUtils.findProperty(typeSpec, element)
            property?.let {
                remainingElements.remove(element)
                element to property
            }
        }.forEach { (element, propSpec) ->
            val visibility = kotlinVisibilityUtils.detectLowestVisibility(typeSpec, propSpec.type)
            if (visibility != KModifier.PRIVATE) {
                generateTestableProperty(
                    typeElement = receiverTypeElement,
                    typeSpec = receiverSpec,
                    property = propSpec,
                    annotation = element.getAnnotation(KTestable::class.java),
                    visibility = visibility,
                    testableClassFile = testableClassFile
                )
            } else {
                processingEnv.messager.printMessage(
                    Diagnostic.Kind.WARNING,
                    "No Catadioptre proxy could be generated for the property ${enclosingElement.asClassName()}.${propSpec.name}, because its type is private"
                )
            }
        }

        remainingElements.forEach {
            processingEnv.messager.printMessage(
                Diagnostic.Kind.WARNING,
                "No Catadioptre proxy could be generated for member ${enclosingElement.asClassName()}.${it.simpleName}, because some source elements (Kotlin function, property getter) were not found"
            )
        }
    }

    /**
     * Generates the specification for the extension function that calls the invisible one, using reflection,
     * while keeping the same signature.
     *
     * @param typeElement the type that encloses the function.
     * @param typeSpec the KotlinPoet [TypeSpec] that represents the [typeElement].
     * @param function the details of the function to be proxied.
     * @param visibility the visibility to apply to the generated function.
     * @param testableClassFile the file where the proxy function has to be added.
     */
    private fun generateTestableFunction(
        typeElement: TypeElement,
        typeSpec: TypeSpec,
        function: AnnotatedFunction,
        visibility: KModifier,
        testableClassFile: FileSpec.Builder
    ) {
        val receiver = typeElement.asType().asTypeName()
        val suspended = KModifier.SUSPEND in function.spec.modifiers
        var functionBuilder = function.spec.toBuilder()
            .receiver(receiver)
            .jvmName(function.spec.name)
            .apply {
                // Removes all the annotations, in case they are not in the classpath.
                annotations.clear()
                // Removes all the modifiers (open, visibility, abstract...) to only later apply the expected ones.
                modifiers.clear()

                if (suspended) {
                    addModifiers(KModifier.SUSPEND)
                }
                // The parameters are copied, without the default values.
                val existingParameters = parameters.map {
                    ParameterSpec.builder(it.name, it.type, it.modifiers).build()
                }
                parameters.clear()
                addParameters(existingParameters)

                // When the enclosing class as variable types, they are applied to the proxy function
                // to maintain a consistency.
                typeSpec.typeVariables.forEach {
                    addTypeVariable(it)
                }
            }
            // Adds the expected visibility.
            .addModifiers(visibility)

            .returns(function.spec.returnType)
            // The code body is erased to be replaced.
            .clearBody()

        val invocationFunctionPrefix = if (suspended) "coInvoke" else "invoke"

        // Defines the code body to use Catadioptre facilities to call the private function.
        if (function.spec.parameters.isEmpty()) {
            testableClassFile.addImport(CATADIOPTRE_UTILS_PACKAGE_NAME, "${invocationFunctionPrefix}NoArgs")
            functionBuilder =
                functionBuilder.addStatement("""return this.${invocationFunctionPrefix}NoArgs("${function.spec.name}")""")
        } else {
            testableClassFile.addImport(CATADIOPTRE_UTILS_PACKAGE_NAME, "${invocationFunctionPrefix}Invisible", "named")
            var statement = """return this.${invocationFunctionPrefix}Invisible("${function.spec.name}", """
            statement += function.spec.parameters.joinToString(", ") { param ->
                """named("${param.name}", ${param.name})"""
            } + ")"
            functionBuilder = functionBuilder.addStatement(statement)
        }
        testableClassFile.addFunction(functionBuilder.build())
    }

    /**
     * Generates the functions to manipulate the private properties: getter, setter and clearer.
     *
     * @param typeElement the type that encloses the function.
     * @param typeSpec the KotlinPoet [TypeSpec] that represents the [typeElement].
     * @param property the KotlinPoet [PropertySpec] representing the private property to proxy.
     * @param annotation the annotation set onto the property, that defines the requirements for proxy generation.
     * @param visibility the visibility to apply to the generated functions.
     * @param testableClassFile the file where the proxy functions have to be added.
     */
    private fun generateTestableProperty(
        typeElement: TypeElement,
        typeSpec: TypeSpec,
        property: PropertySpec,
        annotation: KTestable,
        visibility: KModifier,
        testableClassFile: FileSpec.Builder
    ) {
        if (annotation.getter) {
            generateGetter(typeElement, typeSpec, property, visibility, testableClassFile)
        }
        if (annotation.setter) {
            generateSetter(typeElement, typeSpec, property, visibility, testableClassFile)
        }
        if (annotation.clearer && property.type.isNullable) {
            generateCleaner(typeElement, typeSpec, property, visibility, testableClassFile)
        }
    }

    /**
     * Generates the proxy getter to a private property.
     *
     * @param typeElement the type that encloses the function.
     * @param typeSpec the KotlinPoet [TypeSpec] that represents the [typeElement].
     * @param property the KotlinPoet [PropertySpec] representing the private property to proxy.
     * @param visibility the visibility to apply to the generated functions.
     * @param testableClassFile the file where the proxy functions have to be added.
     */
    private fun generateGetter(
        typeElement: TypeElement,
        typeSpec: TypeSpec,
        property: PropertySpec,
        visibility: KModifier,
        testableClassFile: FileSpec.Builder
    ) {
        testableClassFile.addImport(CATADIOPTRE_UTILS_PACKAGE_NAME, "getProperty")
        testableClassFile.addFunction(
            FunSpec.builder(property.name)
                .prepareFunctionForProperty(typeElement, typeSpec, visibility, false)
                .returns(getPropertyType(property.type))
                .addStatement("return this.getProperty(\"${property.name}\")")
                .build()
        )
    }

    /**
     * Generates the proxy setter to a private property.
     *
     * @param typeElement the type that encloses the function.
     * @param typeSpec the KotlinPoet [TypeSpec] that represents the [typeElement].
     * @param property the KotlinPoet [PropertySpec] representing the private property to proxy.
     * @param visibility the visibility to apply to the generated functions.
     * @param testableClassFile the file where the proxy functions have to be added.
     */
    private fun generateSetter(
        typeElement: TypeElement,
        typeSpec: TypeSpec,
        property: PropertySpec,
        visibility: KModifier,
        testableClassFile: FileSpec.Builder
    ) {
        testableClassFile.addImport(CATADIOPTRE_UTILS_PACKAGE_NAME, "setProperty")
        testableClassFile.addFunction(
            FunSpec.builder(property.name)
                .addParameter("value", getPropertyType(property.type))
                .addStatement("this.setProperty(\"${property.name}\", value)")
                .prepareFunctionForProperty(typeElement, typeSpec, visibility, true)
                .build()
        )
    }

    /**
     * Determines the actual type of the property.
     */
    private fun getPropertyType(propertyType: TypeName): TypeName {
        return when (propertyType) {
            is WildcardTypeName -> {
                propertyType.outTypes.first().let { outType ->
                    if (propertyType.isNullable) {
                        outType.copy(nullable = true)
                    } else {
                        outType.copy(nullable = false)
                    }
                }
            }

            else -> propertyType
        }
    }

    /**
     * Generates the proxy setter to a clean the value of a property (set it to null).
     *
     * @param typeElement the type that encloses the function.
     * @param typeSpec the KotlinPoet [TypeSpec] that represents the [typeElement].
     * @param property the KotlinPoet [PropertySpec] representing the private property to proxy.
     * @param visibility the visibility to apply to the generated functions.
     * @param testableClassFile the file where the proxy functions have to be added.
     */
    private fun generateCleaner(
        typeElement: TypeElement,
        typeSpec: TypeSpec,
        property: PropertySpec,
        visibility: KModifier,
        testableClassFile: FileSpec.Builder
    ) {
        testableClassFile.addImport(CATADIOPTRE_UTILS_PACKAGE_NAME, "clearProperty")
        testableClassFile.addFunction(
            FunSpec.builder("clear" + property.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() })
                .addStatement("this.clearProperty(\"${property.name}\")")
                .prepareFunctionForProperty(typeElement, typeSpec, visibility, true)
                .build()
        )
    }

    /**
     * Prepares the specification for a function to manipulate a property.
     */
    private fun FunSpec.Builder.prepareFunctionForProperty(
        enclosingElement: TypeElement,
        declaringType: TypeSpec,
        visibility: KModifier,
        returnsDeclaring: Boolean = false
    ): FunSpec.Builder {
        val receiver = enclosingElement.asType().asTypeName()
        addModifiers(visibility)
            .receiver(receiver)
            .apply {
                declaringType.typeVariables.forEach {
                    addTypeVariable(it)
                }
            }

        if (returnsDeclaring) {
            returns(enclosingElement.asType().asTypeName())
                .addStatement("return this")
        }
        return this
    }


}
