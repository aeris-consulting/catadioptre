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
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.memberExtensionFunctions
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.superclasses
import kotlin.reflect.full.valueParameters

/**
 * Searches a property in the class or any of its parents.
 *
 * @author Eric Jessé
 */
@Suppress("UNCHECKED_CAST")
internal fun <T> findProperty(instanceClass: KClass<*>, propertyName: String): KProperty1<T, *>? {
    return (instanceClass.takeIf { it.memberProperties.firstOrNull { it.name == propertyName } != null }
        ?: instanceClass.superclasses.firstOrNull { it.memberProperties.find { it.name == propertyName } != null })
        ?.memberProperties?.firstOrNull { it.name == propertyName } as KProperty1<T, *>?
}

/**
 * Searches a function in the class or any of its parents.
 *
 * @author Eric Jessé
 */
@Suppress("UNCHECKED_CAST")
internal fun findFunction(
    instanceClass: KClass<*>,
    functionName: String,
    arguments: List<Argument>
): KFunction<*>? {
    val functions =
        instanceClass.memberFunctions + instanceClass.memberExtensionFunctions + instanceClass.superclasses.flatMap {
            it.memberFunctions + it.memberExtensionFunctions
        }
    val functionsWithName = functions.filter { it.name == functionName }
    return if (functionsWithName.size == 1) {
        functionsWithName.first().apply {
            areParametersMatching(arguments)
            // If some arguments were not resolved, the one with the same index is applied.
            arguments.map(Argument::type).forEachIndexed { index, argumentType ->
                if (argumentType.actualParameter == null) {
                    argumentType.actualParameter = this.valueParameters[index]
                }
            }
        }
    } else {
        functionsWithName.firstOrNull { it.areParametersMatching(arguments) }
    }
}

/**
 * Verifies whether all the parameters definitions passed from the caller are matching the ones from [this] [KFunction].
 *
 * @author Eric Jessé
 */
private fun KFunction<*>.areParametersMatching(searchedArguments: List<Argument>): Boolean {
    val valueParameters = this.valueParameters.filter { it.kind == KParameter.Kind.VALUE }
    // If there is a parameter for the instance, we have to shift the arguments indices to the left.
    val offsetIndex = if (this.instanceParameter == null) 0 else -1
    return if (searchedArguments.size == valueParameters.size) {
        valueParameters.all { searchedArguments[it.index + offsetIndex].matches(it) }
    } else {
        false
    }
}

