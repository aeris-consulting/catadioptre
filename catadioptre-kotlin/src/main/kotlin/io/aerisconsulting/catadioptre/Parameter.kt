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
package io.aerisconsulting.catadioptre

import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.isSuperclassOf

/**
 * Descriptor for a parameter of a function.
 *
 * @author Eric Jessé
 */
data class Parameter(
    private val classifier: KClass<*>?,
    internal var name: String? = null,
    internal var isVararg: Boolean? = null,
    internal var isOptional: Boolean? = null
) {

    internal var actualParameter: KParameter? = null

    fun matches(parameter: KParameter): Boolean {
        val result = (name == null || name == parameter.name)
                && (isVararg == null || parameter.isVararg == isVararg)
                && (isOptional == null || parameter.isOptional == isOptional)
                && typesAreMatching(parameter)

        if (result) {
            this.actualParameter = parameter
        }
        return result
    }

    private fun typesAreMatching(parameter: KParameter): Boolean {
        return when {
            classifier == null || parameter.type.classifier == null -> true
            parameter.isVararg -> {
                val expectedType = classifier.java.componentType?.kotlin ?: classifier
                (parameter.type.classifier as KClass<*>).java.componentType.kotlin.isSuperclassOf(expectedType)
            }
            else -> (parameter.type.classifier as KClass<*>).isSuperclassOf(classifier)
        }
    }
}
