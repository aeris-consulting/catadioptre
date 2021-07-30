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

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test

internal class ReflectionUtilsTest {

    @Test
    internal fun `should set the property`() {
        // given
        val instance = ReflectionUtilsObject()

        // when
        instance.setProperty("value", 456)

        // then
        assertThat(instance.visibleValue).isEqualTo(456)
    }

    @Test
    internal fun `should set the property with infix function`() {
        // given
        val instance = ReflectionUtilsObject()

        // when
        instance withProperty "value" being 456

        // then
        assertThat(instance.visibleValue).isEqualTo(456)
    }

    @Test
    internal fun `should clear the property`() {
        // given
        val instance = ReflectionUtilsObject()

        // when
        instance clearProperty "value"

        // then
        assertThat(instance.visibleValue).isNull()
    }

    @Test
    internal fun `should set the null property`() {
        // given
        val instance = ReflectionUtilsObject()

        // when
        instance.setProperty("value", null)

        // then
        assertThat(instance.visibleValue).isNull()
    }

    @Test
    internal fun `should get the property`() {
        // given
        val instance = ReflectionUtilsObject()

        // when
        val value: Int = instance getProperty "value"

        // then
        assertThat(value).isEqualTo(123)
    }

    @Test
    internal fun `should set the inherited property`() {
        // given
        val instance = ReflectionUtilsObject()

        // when
        instance.setProperty("inheritedValue", 1451)

        // then
        assertThat(instance.visibleInheritedValue).isEqualTo(1451)
    }

    @Test
    internal fun `should get the inherited property`() {
        // given
        val instance = ReflectionUtilsObject()

        // when
        val value: Int = instance.getProperty("inheritedValue")

        // then
        assertThat(value).isEqualTo(789)
    }

    @Test
    internal fun `should execute a private method without argument`() {
        // given
        val instance = ReflectionUtilsObject()

        // when
        val value: Int = instance invokeNoArgs "returnValue"

        // then
        assertThat(value).isEqualTo(123)
    }

    @Test
    internal fun `should execute a private method with keyed null argument`() {
        // given
        val instance = ReflectionUtilsObject()

        // when
        val value: Int? = instance.invokeInvisible("returnProvidedOrValue", namedNull<Int>("default"))

        // then
        assertThat(value).isNull()
    }

    @Test
    internal fun `should execute a private method with omitted argument`() {
        // given
        val instance = ReflectionUtilsObject()

        // when
        val value: Int = instance.invokeInvisible("returnProvidedOrValue", omitted<Int>())

        // then
        assertThat(value).isEqualTo(123)
    }

    @Test
    internal fun `should execute a private method with all indexed arguments`() {
        // given
        val instance = ReflectionUtilsObject()

        // when
        val value: Int = instance.invokeInvisible("divide", 12, 6)

        // then
        assertThat(value).isEqualTo(2)
    }

    @Test
    internal fun `should execute a private method with all keyed arguments`() {
        // given
        val instance = ReflectionUtilsObject()

        // when
        val value: Int = instance.invokeInvisible("divide", named("value", 18), named("divider", 6))
        // then
        assertThat(value).isEqualTo(3)
    }

    @Test
    internal fun `should execute a private method with an omitted value`() {
        // given
        val instance = ReflectionUtilsObject()

        // when
        val value: Int = instance.invokeInvisible("divide", omitted<Int>(), 2)

        // then
        assertThat(value).isEqualTo(5)
    }

    @Test
    internal fun `should execute a private method with an argument and vararg`() {
        // given
        val instance = ReflectionUtilsObject()

        // when
        val value: Int = instance.invokeInvisible("divideSum", 2, vararg(1, 3, 6))

        // then
        assertThat(value).isEqualTo(5)
    }

    @Test
    internal fun `should execute a private method with an argument and null for the vararg`() {
        // given
        val instance = ReflectionUtilsObject()

        // when
        val value: Int = instance.invokeInvisible("divideSum", 2, nullOf<Int>())

        // then
        assertThat(value).isEqualTo(0)
    }


    @Test
    internal fun `should execute an inherited method without argument`() {
        // given
        val instance = ReflectionUtilsObject()

        // when
        val value: Int = instance invokeNoArgs "returnInheritedValue"

        // then
        assertThat(value).isEqualTo(789)
    }


    @Test
    internal fun `should execute an inherited method with omitted argument`() {
        // given
        val instance = ReflectionUtilsObject()

        // when
        val value: Int = instance.invokeInvisible("returnInheritedProvidedOrValue", omitted<Int>())

        // then
        assertThat(value).isEqualTo(789)
    }

    @Test
    internal fun `should execute an inherited method with all indexed arguments`() {
        // given
        val instance = ReflectionUtilsObject()

        // when
        val value: Int = instance.invokeInvisible("inheritedDivide", 12, 6)

        // then
        assertThat(value).isEqualTo(2)
    }

    @Test
    internal fun `should execute an inherited method with an argument and vararg`() {
        // given
        val instance = ReflectionUtilsObject()

        // when
        val value: Int = instance.invokeInvisible("inheritedDivideSum", 2, vararg(1, 3, 6))

        // then
        assertThat(value).isEqualTo(5)
    }

    @Test
    internal fun `should execute a private suspended method without argument`() = runBlockingTest {
        // given
        val instance = SuspendedReflectionUtilsObject()

        // when
        val value: Int = instance coInvokeNoArgs "returnValue"

        // then
        assertThat(value).isEqualTo(123)
    }

    @Test
    internal fun `should execute a private suspended method with keyed null argument`() = runBlockingTest {
        // given
        val instance = SuspendedReflectionUtilsObject()

        // when
        val value: Int? = instance.coInvokeInvisible("returnProvidedOrValue", namedNull<Int>("default"))

        // then
        assertThat(value).isNull()
    }

    @Test
    internal fun `should execute a private suspended method with omitted argument`() = runBlockingTest {
        // given
        val instance = SuspendedReflectionUtilsObject()

        // when
        val value: Int = instance.coInvokeInvisible("returnProvidedOrValue", omitted<Int>())

        // then
        assertThat(value).isEqualTo(123)
    }

    @Test
    internal fun `should execute a private suspended method with all indexed arguments`() = runBlockingTest {
        // given
        val instance = SuspendedReflectionUtilsObject()

        // when
        val value: Int = instance.coInvokeInvisible("divide", 12, 6)

        // then
        assertThat(value).isEqualTo(2)
    }

    @Test
    internal fun `should execute a private suspended method with all keyed arguments`() = runBlockingTest {
        // given
        val instance = SuspendedReflectionUtilsObject()

        // when
        val value: Int = instance.coInvokeInvisible("divide", named("value", 18), named("divider", 6))
        // then
        assertThat(value).isEqualTo(3)
    }

    @Test
    internal fun `should execute a private suspended method with an omitted value`() = runBlockingTest {
        // given
        val instance = SuspendedReflectionUtilsObject()

        // when
        val value: Int = instance.coInvokeInvisible("divide", omitted<Int>(), 2)

        // then
        assertThat(value).isEqualTo(5)
    }

    @Test
    internal fun `should execute a private suspended method with an argument and vararg`() = runBlockingTest {
        // given
        val instance = SuspendedReflectionUtilsObject()

        // when
        val value: Int = instance.coInvokeInvisible("divideSum", 2, vararg(1, 3, 6))

        // then
        assertThat(value).isEqualTo(5)
    }

    @Test
    internal fun `should execute a private suspended method with an argument and null for the vararg`() =
        runBlockingTest {
            // given
            val instance = SuspendedReflectionUtilsObject()

            // when
            val value: Int = instance.coInvokeInvisible("divideSum", 2, nullOf<Int>())

            // then
            assertThat(value).isEqualTo(0)
        }


    @Test
    internal fun `should execute an inherited suspended method without argument`() = runBlockingTest {
        // given
        val instance = SuspendedReflectionUtilsObject()

        // when
        val value: Int = instance coInvokeNoArgs "returnInheritedValue"

        // then
        assertThat(value).isEqualTo(789)
    }


    @Test
    internal fun `should execute an inherited suspended method with omitted argument`() = runBlockingTest {
        // given
        val instance = ReflectionUtilsObject()

        // when
        val value: Int = instance.coInvokeInvisible("returnInheritedProvidedOrValue", omitted<Int>())

        // then
        assertThat(value).isEqualTo(789)
    }

    @Test
    internal fun `should execute an inherited suspended method with all indexed arguments`() = runBlockingTest {
        // given
        val instance = ReflectionUtilsObject()

        // when
        val value: Int = instance.coInvokeInvisible("inheritedDivide", 12, 6)

        // then
        assertThat(value).isEqualTo(2)
    }

    @Test
    internal fun `should execute an inherited suspended method with an argument and vararg`() = runBlockingTest {
        // given
        val instance = ReflectionUtilsObject()

        // when
        val value: Int = instance.coInvokeInvisible("inheritedDivideSum", 2, vararg(1, 3, 6))

        // then
        assertThat(value).isEqualTo(5)
    }

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

}

open class ParentReflectionUtilsObject(
    private val inheritedValue: Int
) {

    protected fun returnInheritedValue() = inheritedValue

    private fun returnInheritedProvidedOrValue(default: Int? = inheritedValue): Int? = default

    private fun inheritedDivide(value: Number = 10, divider: Int) = value.toInt() / divider

    private fun inheritedDivideSum(divider: Int = 1, vararg values: Int) = values.sum() / divider

}

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

open class SuspendedParentReflectionUtilsObject(
    private val inheritedValue: Int
) {

    protected suspend fun returnInheritedValue() = inheritedValue

    private suspend fun returnInheritedProvidedOrValue(default: Int? = inheritedValue): Int? = default

    private suspend fun inheritedDivide(value: Number = 10, divider: Int) = value.toInt() / divider

    private suspend fun inheritedDivideSum(divider: Int = 1, vararg values: Int) = values.sum() / divider

}
