package io.aerisconsulting.katadioptre

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.full.memberExtensionFunctions
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.superclasses
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField
import kotlin.reflect.safeCast

/**
 * Sets [value] in the property or field called [propertyName] of the instance.
 *
 * Usage:
 * ```
 * instance.setProperty("value", 456)
 * ```
 */
fun <T : Any> T.setProperty(propertyName: String, value: Any?): T {
    this.withProperty(propertyName).being(value)
    return this
}

/**
 * Infix form of [withProperty].
 *
 * Usage:
 * ```
 * instance withProperty "value" being 456
 * ```
 */
infix fun Any.withProperty(propertyName: String): DynamicSetter<Any> {
    val property = findProperty<Any>(this::class, propertyName)
        ?: throw RuntimeException("The property $propertyName could not be found")
    return DynamicSetter(this, property)
}

/**
 * Sets the property or field called [propertyName] to null.
 *
 * Usage:
 * ```
 * instance clearProperty "value"
 * ```
 */
infix fun <T : Any> T.clearProperty(propertyName: String): T {
    this.withProperty(propertyName).being(null)
    return this
}


/**
 * Returns the value of the property or field called [propertyName] of the instance.
 *
 * Usage:
 * ```
 * val value: Int = instance getProperty "value"
 * ```
 */
@Suppress("UNCHECKED_CAST")
infix fun <T> Any.getProperty(propertyName: String): T {
    val property = findProperty<T>(this::class, propertyName)
    return if (property is KProperty<*>) {
        property.isAccessible = true
        property.getter.call(this) as T
    } else {
        throw RuntimeException("The property $propertyName could not be found")
    }
}

@Suppress("UNCHECKED_CAST")
private fun <T> findProperty(instanceClass: KClass<*>, propertyName: String): KProperty1<T, *>? {
    return (instanceClass.takeIf { it.memberProperties.firstOrNull { it.name == propertyName } != null }
        ?: instanceClass.superclasses.firstOrNull { it.memberProperties.find { it.name == propertyName } != null })
        ?.memberProperties?.firstOrNull { it.name == propertyName } as KProperty1<T, *>?
}


class DynamicSetter<T>(
    private val instance: Any,
    private val property: KProperty1<T, *>
) {

    infix fun being(value: Any?) {
        set(value)
    }

    private fun set(value: Any?) {
        if (property is KMutableProperty<*>) {
            property.isAccessible = true
            property.setter.call(instance, value)
        } else {
            property.javaField!!.also {
                it.isAccessible = true
                it.set(instance, value)
            }
        }
    }

}

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
infix suspend fun <T> Any.coInvokeNoArgs(methodName: String): T {
    return coInvokeInvisible(methodName)
}

/**
 * Executes the monadic or polyadic method [methodName] on this instance with the provided arguments and returns the result.
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
 * Executes the monadic or polyadic method [methodName] on this instance with the provided arguments and returns the result.
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
        Argument(value, ArgumentType(null, name))
    }
}

/**
 * Creates a named null argument for a call of [invokeInvisible].
 */
inline fun <reified T> namedNull(name: String) = Argument(null, ArgumentType(T::class, name))

/**
 * Creates a null argument of the provided type for a call of [invokeInvisible].
 */
inline fun <reified T> nullOf() = Argument(null, ArgumentType(T::class))

/**
 * Creates an omitted argument of the provided type for a call of [invokeInvisible]. The default value of the argument will
 * be applied at the execution.
 */
inline fun <reified T> omitted() = Argument(null, ArgumentType(T::class, isOptional = true), true)

/**
 * Creates a variable-length argument for a call of [invokeInvisible].
 */
inline fun <reified T> vararg(vararg values: T) =
    Argument(values, ArgumentType(emptyArray<T>()::class, isVararg = true), false)

internal class DynamicCall<T>(
    private val instance: Any,
    private val methodName: String
) {

    private var arguments: List<Argument> = emptyList()

    @Suppress("UNCHECKED_CAST")
    internal fun execute(): T {
        val function = findFunction(instance::class, methodName, arguments.map(Argument::type))
        return if (function != null) {
            val allArguments = prepareFunction(function)
            function.callBy(allArguments) as T
        } else {
            throw RuntimeException("The function $methodName could not be found with the arguments $arguments")
        }
    }

    private fun prepareFunction(function: KFunction<*>): MutableMap<KParameter, Any?> {
        function.isAccessible = true
        val allArguments = mutableMapOf<KParameter, Any?>()
        function.instanceParameter?.let { param -> allArguments[param] = instance }
        allArguments += arguments.filterNot { it.isOmitted == true }
            .associate { getArgument(it.type.parameter!!, it.value) }
        return allArguments
    }

    @Suppress("UNCHECKED_CAST")
    internal suspend fun coExecute(): T {
        val function = findFunction(instance::class, methodName, arguments.map(Argument::type))
        return if (function != null) {
            val allArguments = prepareFunction(function)
            function.callSuspendBy(allArguments) as T
        } else {
            throw RuntimeException("The function $methodName could not be found with the arguments $arguments")
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
                Argument(value = null, ArgumentType(Any::class))
            } else {
                Argument(it, ArgumentType(it::class))
            }
        }
    }

    fun named(name: String, value: Any?): Argument {
        return if (value is Argument) {
            value.type.name = name
            return value
        } else {
            Argument(value, ArgumentType(null, name))
        }
    }

    inline fun <reified T> namedNull(name: String) = Argument(null, ArgumentType(T::class, name))

    inline fun <reified T> nullOf() = Argument(null, ArgumentType(T::class))

    inline fun <reified T> omitted() = Argument(null, ArgumentType(T::class, isOptional = true), true)

    inline fun <reified T> vararg(vararg values: T) =
        Argument(values, ArgumentType(emptyArray<T>()::class, isVararg = true), false)

}

@Suppress("UNCHECKED_CAST")
private fun findFunction(
    instanceClass: KClass<*>,
    functionName: String,
    argumentTypes: List<ArgumentType>
): KFunction<*>? {
    val functions =
        instanceClass.memberFunctions + instanceClass.memberExtensionFunctions + instanceClass.superclasses.flatMap {
            it.memberFunctions + it.memberExtensionFunctions
        }
    val functionsWithName = functions.filter { it.name == functionName }
    return if (functionsWithName.size == 1) {
        functionsWithName.first().apply {
            areArgumentMatching(argumentTypes)
            // If some arguments were not resolved, the one with the same index is applied.
            argumentTypes.forEachIndexed { index, argumentType ->
                if (argumentType.parameter == null) {
                    argumentType.parameter = this.valueParameters[index]
                }
            }
        }
    } else {
        functionsWithName.firstOrNull { it.name == functionName && it.areArgumentMatching(argumentTypes) }
    }
}

private fun KFunction<*>.areArgumentMatching(searchedArguments: List<ArgumentType>): Boolean {
    val nonInstanceParameters = this.valueParameters.filterNot { it.kind == KParameter.Kind.INSTANCE }
    // If there is a parameter for the instance, we have to shift the arguments indices to the left.
    val offsetIndex = if (this.instanceParameter == null) 0 else -1
    return if (searchedArguments.size == nonInstanceParameters.size) {
        nonInstanceParameters.all { searchedArguments[it.index + offsetIndex].matches(it) }
    } else {
        false
    }
}

data class Argument constructor(
    internal val value: Any?,
    internal val type: ArgumentType,
    internal val isOmitted: Boolean? = null
)

data class ArgumentType(
    private val classifier: KClass<*>?,
    internal var name: String? = null,
    internal var isVararg: Boolean? = null,
    internal var isOptional: Boolean? = null
) {

    internal var parameter: KParameter? = null

    fun matches(parameter: KParameter): Boolean {
        val result = (name == null || name == parameter.name)
                && (isVararg == null || parameter.isVararg == isVararg)
                && (isOptional == null || parameter.isOptional == isOptional)
                && typesAreMatching(parameter)

        if (result) {
            this.parameter = parameter
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
