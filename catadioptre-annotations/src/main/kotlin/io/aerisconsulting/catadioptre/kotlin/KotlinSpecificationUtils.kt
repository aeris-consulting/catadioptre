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
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import java.util.Locale
import javax.lang.model.element.ExecutableElement
import javax.lang.model.type.TypeMirror

@OptIn(DelicateKotlinPoetApi::class)
internal class KotlinSpecificationUtils(
    private val unitType: TypeMirror
) {

    /**
     * Finds the Kotlin property matching the [getter], if it exists.
     */
    fun findProperty(
        enclosingElement: TypeSpec,
        getter: ExecutableElement
    ): PropertySpec? {
        val mayBePropertyName = getter.simpleName.toString().substringAfter("get")
            .substringBefore("$").replaceFirstChar { it.lowercase(Locale.getDefault()) }
        return mayBePropertyName.takeIf { it.isNotBlank() && getter.returnType != unitType }?.let { propertyName ->
            enclosingElement.propertySpecs.firstOrNull { it.name == propertyName }
        }
    }

    /**
     * Finds the Kotlin function from [typeSpec] corresponding to [function].
     */
    fun findFunction(typeSpec: TypeSpec, function: ExecutableElement): AnnotatedFunction? {
        val specCandidates = typeSpec.funSpecs.filter { funSpec ->
            val comparableArgumentsCount = if (funSpec.modifiers.contains(KModifier.SUSPEND)) {
                funSpec.parameters.size + 1 // Add the Continuation added by Kotlin on suspend functions.
            } else {
                funSpec.parameters.size
            }
            funSpec.name == function.simpleName.toString()
                    && comparableArgumentsCount == function.parameters.size
        }

        return if (specCandidates.size == 1) {
            AnnotatedFunction(function, specCandidates.first())
        } else if (specCandidates.isNotEmpty()) {
            // Due to polymorphism, it is possible to have several methods with same name but different signatures.
            specCandidates.firstOrNull { funSpec ->
                funSpec.parameters.mapIndexed { index, parameterSpec ->
                    areTypesEqual(parameterSpec.type, function.parameters[index].asType().asTypeName())
                }.all { it }
            }?.let { AnnotatedFunction(function, it) }
        } else {
            null
        }

    }

    /**
     * Verifies whether a Kotlin type and a Java type are equal.
     */
    fun areTypesEqual(kotlinType: TypeName, javaType: TypeName): Boolean {
        val nonNullableKotlinType = if (kotlinType.isNullable) kotlinType.copy(nullable = false) else kotlinType
        return if (nonNullableKotlinType.toString() == javaType.toString()) {
            true
        } else if (nonNullableKotlinType is ParameterizedTypeName && javaType is ParameterizedTypeName) {
            areTypesEqual(nonNullableKotlinType.rawType, javaType.rawType)
        } else {
            JAVA_TO_KOTLIN_EQUIVALENT[javaType.toString()] == nonNullableKotlinType.toString()
        }
    }

    companion object {

        val JAVA_TO_KOTLIN_EQUIVALENT = listOf(
            // Built-in types.
            Any::class,
            Array::class,
            Boolean::class,
            BooleanArray::class,
            Byte::class,
            ByteArray::class,
            Char::class,
            CharArray::class,
            Double::class,
            DoubleArray::class,
            Enum::class,
            Float::class,
            FloatArray::class,
            Int::class,
            IntArray::class,
            Long::class,
            LongArray::class,
            Nothing::class,
            Number::class,
            Short::class,
            ShortArray::class,
            String::class,
            Throwable::class,
            Annotation::class,
            CharSequence::class,
            Comparable::class,

            // Collections.
            Collection::class,
            MutableCollection::class,
            Map::class,
            MutableMap::class,
            List::class,
            MutableList::class,
            Set::class,
            MutableSet::class,
            Iterable::class,
            MutableIterable::class,
            Iterator::class,
            MutableIterator::class,
            ListIterator::class,
            MutableListIterator::class,
            ArrayList::class,
            HashMap::class,
            HashSet::class,
            LinkedHashMap::class,
            LinkedHashSet::class,
            RandomAccess::class,
        ).associate { it.java.canonicalName to it.qualifiedName }

    }
}
