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

/**
 * Executes the niladic method [methodName] on this instance and returns the result.
 *
 * Usage:
 * ```
 * val value: Int = instance invokeNoArgs "returnValue"
 * ```
 */
@Suppress("UNCHECKED_CAST")
infix fun <T> Any.invokeNoArgs(methodName: String): T {
    return invokeInvisible(methodName)
}


/**
 * Executes the niladic suspend method [methodName] on this instance and returns the result.
 *
 * Usage:
 * ```
 * val value: Int = instance coInvokeNoArgs "returnValue"
 * ```
 */
@Suppress("UNCHECKED_CAST")
suspend infix fun <T> Any.coInvokeNoArgs(methodName: String): T {
    return coInvokeInvisible(methodName)
}

/**
 * Executes the monadic or polyadic method [methodName] on this instance with the provided arguments
 * and returns the result.
 *
 * While this method is generally used to execute invisible methods in the current scope (private or internal),
 * it can be applied for any method.
 *
 * Usage:
 * ```
 * val value: Int = instance.invokeInvisible("divide", 12, 6)
 * ```
 *
 * See also [named], [namedNull], [nullOf], [omitted] and [vararg] to create arguments using different strategies.
 */
@Suppress("UNCHECKED_CAST")
fun <T> Any.invokeInvisible(methodName: String, vararg arguments: Any?): T {
    return DynamicCall<T>(this, methodName).apply { withArgs(*arguments) }.execute()
}


/**
 * Executes the monadic or polyadic method [methodName] on this instance with the provided arguments
 * and returns the result.
 *
 * While this method is generally used to execute invisible methods in the current scope (private or internal),
 * it can be applied for any method.
 *
 * Usage:
 * ```
 * val value: Int = instance.coInvokeInvisible("divide", 12, 6)
 * ```
 *
 * See also [named], [namedNull], [nullOf], [omitted] and [vararg] to create arguments using different strategies.
 */
@Suppress("UNCHECKED_CAST")
suspend fun <T> Any.coInvokeInvisible(methodName: String, vararg arguments: Any?): T {
    return DynamicCall<T>(this, methodName).apply { withArgs(*arguments) }.coExecute()
}

/**
 * Creates a named argument for a call of [invokeInvisible].
 */
fun named(name: String, value: Any?): Argument {
    return if (value is Argument) {
        value.type.name = name
        return value
    } else {
        Argument(value, Parameter(null, name))
    }
}

/**
 * Creates a named null argument for a call of [invokeInvisible].
 */
inline fun <reified T> namedNull(name: String) = Argument(null, Parameter(T::class, name))

/**
 * Creates a null argument of the provided type for a call of [invokeInvisible].
 */
inline fun <reified T> nullOf() = Argument(null, Parameter(T::class))

/**
 * Creates an omitted argument of the provided type for a call of [invokeInvisible].
 * The default value of the argument will be applied at the execution.
 */
inline fun <reified T> omitted() = Argument(null, Parameter(T::class, isOptional = true), true)

/**
 * Creates a variable-length argument for a call of [invokeInvisible].
 */
inline fun <reified T> vararg(vararg values: T) =
    Argument(values, Parameter(emptyArray<T>()::class, isVararg = true), false)

