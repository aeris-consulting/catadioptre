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

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.metadata.ImmutableKmClass
import com.squareup.kotlinpoet.metadata.ImmutableKmFunction
import com.squareup.kotlinpoet.metadata.ImmutableKmProperty
import com.squareup.kotlinpoet.metadata.ImmutableKmType
import com.squareup.kotlinpoet.metadata.ImmutableKmTypeParameter
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.isNullable
import kotlinx.metadata.KmClassifier
import kotlinx.metadata.KmVariance
import kotlinx.metadata.KmVariance.IN
import kotlinx.metadata.KmVariance.INVARIANT
import javax.lang.model.element.ExecutableElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Types

@KotlinPoetMetadataPreview
internal class KotlinSpecificationUtils(
    private val typeUtils: Types,
    private val unitType: TypeMirror
) {

    /**
     * Generates a [TypeName] for the type of property or parameter.
     */
    fun createTypeName(declaringType: ImmutableKmClass, type: ImmutableKmType): TypeName {
        return when (val classifier = type.classifier) {
            is KmClassifier.Class ->
                createTypeNameForClass(declaringType, type, classifier.name.replace('/', '.'))
            is KmClassifier.TypeParameter -> createTypeNameForTypeParameter(
                declaringType,
                declaringType.typeParameters[classifier.id]
            )
            else -> throw IllegalArgumentException("Unsupported type $classifier")
        }
    }

    /**
     * Generates a [TypeName] for the type of property or parameter, when it is a class.
     */
    private fun createTypeNameForClass(declaringType: ImmutableKmClass, type: ImmutableKmType, name: String): TypeName {
        var className = ClassName(name.substringBeforeLast("."), name.substringAfterLast("."))
        if (type.isNullable) {
            className = className.copy(nullable = true) as ClassName
        }
        return if (type.arguments.isNotEmpty()) {
            className.parameterizedBy(type.arguments.map { arg ->
                when (arg.variance) {
                    INVARIANT -> createTypeName(declaringType, arg.type!!)
                    null -> STAR
                    else -> createVariantTypeName(arg.variance!!, declaringType, arg.type!!)
                }
            })
        } else {
            className
        }
    }

    /**
     * Creates a [TypeName] for a variant.
     */
    private fun createVariantTypeName(
        variance: KmVariance,
        declaringType: ImmutableKmClass,
        type: ImmutableKmType
    ): TypeName {
        return WildcardTypeName.run {
            if (variance == IN) {
                consumerOf(createTypeName(declaringType, type))
            } else {
                producerOf(createTypeName(declaringType, type))
            }
        }
    }

    /**
     * Generates a [TypeName] for the type of property or parameter, when it is a type argument from the enclosing class.
     */
    fun createTypeNameForTypeParameter(
        declaringType: ImmutableKmClass,
        type: ImmutableKmTypeParameter
    ): TypeVariableName {
        return TypeVariableName(type.name, bounds = type.upperBounds.map { createTypeName(declaringType, it) })
    }

    /**
     * Finds the Kotlin property matching the [getter], if it exists.
     */
    fun findProperty(
        enclosingElement: ImmutableKmClass,
        getter: ExecutableElement
    ): ImmutableKmProperty? {
        val mayBePropertyName = getter.simpleName.toString().substringAfter("get")
            .substringBefore("$").decapitalize()
        return mayBePropertyName.takeIf { it.isNotBlank() && getter.returnType != unitType }?.let { propertyName ->
            enclosingElement.properties.firstOrNull { it.name == propertyName }
        }
    }

    /**
     * Finds the Kotlin function from [enclosingElement] corresponding to [function].
     */
    fun findFunction(
        enclosingElement: ImmutableKmClass,
        function: ExecutableElement
    ): ImmutableKmFunction {
        return enclosingElement.functions.first { function.jvmMethodSignature(typeUtils) == "${it.signature}" }
    }

}
