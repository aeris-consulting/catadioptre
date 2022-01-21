package io.aerisconsulting.catadioptre

open class ParentReflectionUtilsObject(
    private val inheritedValue: Int
) {

    protected fun returnInheritedValue() = inheritedValue

    private fun returnInheritedProvidedOrValue(default: Int? = inheritedValue): Int? = default

    private fun inheritedDivide(value: Number = 10, divider: Int) = value.toInt() / divider

    private fun inheritedDivideSum(divider: Int = 1, vararg values: Int) = values.sum() / divider

}

abstract class AbstractWrapper {
    abstract val value: String
}

abstract class AbstractOtherWrapper {
    abstract val value: String
}

class ReflectionUtilsObject(
    private val value: Int? = 123,
    inheritedValue: Int = 789
) : ParentReflectionUtilsObject(inheritedValue) {

    val visibleValue: Int?
        get() = value

    val visibleInheritedValue: Int
        get() = returnInheritedValue()

    private fun returnValue() = value

    private fun returnProvidedOrValue(default: Int? = value): Int? = default

    private fun divide(value: Number = 10, divider: Int) = value.toInt() / divider

    private fun divideSum(divider: Int = 1, vararg values: Int?): Int {
        return values.filterNotNull().sum() / divider
    }

    private fun extractValue(wrapper: AbstractWrapper): String = wrapper.value

    private fun extractValue(wrapper: AbstractOtherWrapper): String = wrapper.value

    private fun throwException(): Nothing = throw IllegalArgumentException("This is the exception")

}

@Suppress("RedundantSuspendModifier")
open class SuspendedParentReflectionUtilsObject(
    private val inheritedValue: Int
) {

    protected suspend fun returnInheritedValue() = inheritedValue

    private suspend fun returnInheritedProvidedOrValue(default: Int? = inheritedValue): Int? = default

    private suspend fun inheritedDivide(value: Number = 10, divider: Int) = value.toInt() / divider

    private suspend fun inheritedDivideSum(divider: Int = 1, vararg values: Int) = values.sum() / divider

    private suspend fun throwExceptionSuspended(): Nothing = throw IllegalArgumentException("This is the exception")

}

@Suppress("RedundantSuspendModifier")
class SuspendedReflectionUtilsObject(
    private val value: Int? = 123,
    inheritedValue: Int = 789
) : SuspendedParentReflectionUtilsObject(inheritedValue) {

    private suspend fun returnValue() = value

    private suspend fun returnProvidedOrValue(default: Int? = value): Int? = default

    private suspend fun divide(value: Number = 10, divider: Int) = value.toInt() / divider

    private suspend fun divideSum(divider: Int = 1, vararg values: Int?): Int {
        return values.filterNotNull().sum() / divider
    }

}
