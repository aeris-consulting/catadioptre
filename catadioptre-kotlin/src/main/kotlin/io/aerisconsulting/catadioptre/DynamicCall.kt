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

import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.safeCast

/**
 * Wrapper to proceed with the execution of a function with name [functionName] on [instance].
 *
 * @author Eric Jessé
 */
internal class DynamicCall<T>(
    private val instance: Any,
    private val functionName: String
) {

    private var arguments: List<Argument> = emptyList()

    @Suppress("UNCHECKED_CAST")
    internal fun execute(): T {
        val function = findFunction(instance::class, functionName, arguments.map(Argument::type))
        return if (function != null) {
            val allArguments = prepareFunction(function)
            try {
                function.callBy(allArguments) as T
            } catch (targetException: InvocationTargetException) {
                throw targetException.cause!!
            }
        } else {
            throw IllegalArgumentException("The function $functionName could not be found for the arguments $arguments")
        }
    }

    private fun prepareFunction(function: KFunction<*>): MutableMap<KParameter, Any?> {
        function.isAccessible = true
        val allArguments = mutableMapOf<KParameter, Any?>()
        function.instanceParameter?.let { param -> allArguments[param] = instance }
        allArguments += arguments.filterNot { it.isOmitted == true }
            .associate { getArgument(it.type.actualParameter!!, it.value) }
        return allArguments
    }

    @Suppress("UNCHECKED_CAST")
    internal suspend fun coExecute(): T {
        val function = findFunction(instance::class, functionName, arguments.map(Argument::type))
        return if (function != null) {
            val allArguments = prepareFunction(function)
            try {
                function.callSuspendBy(allArguments) as T
            } catch (targetException: InvocationTargetException) {
                throw targetException.cause!!
            }
        } else {
            throw IllegalArgumentException("The function $functionName could not be found for the arguments $arguments")
        }
    }

    private fun getArgument(parameter: KParameter, value: Any?): Pair<KParameter, Any?> {
        @Suppress("UNCHECKED_CAST")
        val result = if (parameter.isVararg) {
            val parameterType = (parameter.type.classifier as KClass<Array<*>>)
            if (value != null) {
                val values = value.takeIf { it is Array<*> } as? Array<*> ?: arrayOf(value)
                // Cast the received array or create one with the right type and copy the values.
                parameterType.safeCast(values) ?: java.lang.reflect.Array.newInstance(
                    parameterType.java.componentType,
                    values.size
                ).also {
                    values.forEachIndexed { index, v -> java.lang.reflect.Array.set(it, index, v) }
                }
            } else {
                // Create an empty array if the value is null.
                java.lang.reflect.Array.newInstance(parameterType.java.componentType, 0)
            }
        } else {
            value
        }

        return parameter to result
    }

    fun withArgs(vararg values: Any?) {
        arguments = values.map {
            if (it is Argument) {
                it
            } else if (it == null) {
                Argument(value = null, Parameter(Any::class))
            } else {
                Argument(it, Parameter(it::class))
            }
        }
    }

    fun named(name: String, value: Any?): Argument {
        return if (value is Argument) {
            value.type.name = name
            return value
        } else {
            Argument(value, Parameter(null, name))
        }
    }

    inline fun <reified T> namedNull(name: String) = Argument(null, Parameter(T::class, name))

    inline fun <reified T> nullOf() = Argument(null, Parameter(T::class))

    inline fun <reified T> omitted() = Argument(null, Parameter(T::class, isOptional = true), true)

    inline fun <reified T> vararg(vararg values: T) =
        Argument(values, Parameter(emptyArray<T>()::class, isVararg = true), false)

}
