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

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import assertk.assertions.prop
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ReflectionFunctionUtilsTest {

    @Test
    internal fun `should execute a private function without argument`() {
        // given
        val instance = ReflectionUtilsObject()

        // when
        val value: Int = instance invokeNoArgs "returnValue"

        // then
        assertThat(value).isEqualTo(123)
    }

    @Test
    internal fun `should execute a private function with keyed null argument`() {
        // given
        val instance = ReflectionUtilsObject()

        // when
        val value: Int? = instance.invokeInvisible("returnProvidedOrValue", namedNull<Int>("default"))

        // then
        assertThat(value).isNull()
    }

    @Test
    internal fun `should execute a private function with omitted argument`() {
        // given
        val instance = ReflectionUtilsObject()

        // when
        val value: Int = instance.invokeInvisible("returnProvidedOrValue", omitted<Int>())

        // then
        assertThat(value).isEqualTo(123)
    }

    @Test
    internal fun `should execute a private function with all indexed arguments`() {
        // given
        val instance = ReflectionUtilsObject()

        // when
        val value: Int = instance.invokeInvisible("divide", 12, 6)

        // then
        assertThat(value).isEqualTo(2)
    }

    @Test
    internal fun `should execute a private function with all keyed arguments`() {
        // given
        val instance = ReflectionUtilsObject()

        // when
        val value: Int = instance.invokeInvisible("divide", named("value", 18), named("divider", 6))
        // then
        assertThat(value).isEqualTo(3)
    }

    @Test
    internal fun `should execute a private function with an omitted value`() {
        // given
        val instance = ReflectionUtilsObject()

        // when
        val value: Int = instance.invokeInvisible("divide", omitted<Int>(), 2)

        // then
        assertThat(value).isEqualTo(5)
    }

    @Test
    internal fun `should execute a private function with an argument and vararg`() {
        // given
        val instance = ReflectionUtilsObject()

        // when
        val value: Int = instance.invokeInvisible("divideSum", 2, vararg(1, 3, 6))

        // then
        assertThat(value).isEqualTo(5)
    }

    @Test
    internal fun `should execute a private function with an argument and null for the vararg`() {
        // given
        val instance = ReflectionUtilsObject()

        // when
        val value: Int = instance.invokeInvisible("divideSum", 2, nullOf<Int>())

        // then
        assertThat(value).isEqualTo(0)
    }


    @Test
    internal fun `should execute an inherited function without argument`() {
        // given
        val instance = ReflectionUtilsObject()

        // when
        val value: Int = instance invokeNoArgs "returnInheritedValue"

        // then
        assertThat(value).isEqualTo(789)
    }


    @Test
    internal fun `should execute an inherited function with omitted argument`() {
        // given
        val instance = ReflectionUtilsObject()

        // when
        val value: Int = instance.invokeInvisible("returnInheritedProvidedOrValue", omitted<Int>())

        // then
        assertThat(value).isEqualTo(789)
    }

    @Test
    internal fun `should execute an inherited function with all indexed arguments`() {
        // given
        val instance = ReflectionUtilsObject()

        // when
        val value: Int = instance.invokeInvisible("inheritedDivide", 12, 6)

        // then
        assertThat(value).isEqualTo(2)
    }

    @Test
    internal fun `should execute an inherited function with an argument and vararg`() {
        // given
        val instance = ReflectionUtilsObject()

        // when
        val value: Int = instance.invokeInvisible("inheritedDivideSum", 2, vararg(1, 3, 6))

        // then
        assertThat(value).isEqualTo(5)
    }

    @Test
    internal fun `should throw original exception when executing function`() {
        // given
        val instance = ReflectionUtilsObject()

        // when
        val exception = assertThrows<Exception> {
            instance.invokeInvisible("throwException")
        }

        assertThat(exception).isInstanceOf(IllegalArgumentException::class)
            .prop(IllegalArgumentException::message).isEqualTo("This is the exception")
    }

    @Test
    internal fun `should execute a function when the argument is a mock of an abstract class and two candidate methods`() =
        runBlocking {
            // given
            val instance = ReflectionUtilsObject()
            val mock = mockk<AbstractWrapper> {
                every { value } returns "the value"
            }

            // when
            val value: String = instance.invokeInvisible("extractValue", mock)

            // then
            assertThat(value).isEqualTo("the value")
        }

    @Test
    internal fun `should execute a private suspended function without argument`() = runBlocking {
        // given
        val instance = SuspendedReflectionUtilsObject()

        // when
        val value: Int = instance coInvokeNoArgs "returnValue"

        // then
        assertThat(value).isEqualTo(123)
    }

    @Test
    internal fun `should execute a private suspended function with keyed null argument`() = runBlocking {
        // given
        val instance = SuspendedReflectionUtilsObject()

        // when
        val value: Int? = instance.coInvokeInvisible("returnProvidedOrValue", namedNull<Int>("default"))

        // then
        assertThat(value).isNull()
    }

    @Test
    internal fun `should execute a private suspended function with omitted argument`() = runBlocking {
        // given
        val instance = SuspendedReflectionUtilsObject()

        // when
        val value: Int = instance.coInvokeInvisible("returnProvidedOrValue", omitted<Int>())

        // then
        assertThat(value).isEqualTo(123)
    }

    @Test
    internal fun `should execute a private suspended function with all indexed arguments`() = runBlocking {
        // given
        val instance = SuspendedReflectionUtilsObject()

        // when
        val value: Int = instance.coInvokeInvisible("divide", 12, 6)

        // then
        assertThat(value).isEqualTo(2)
    }

    @Test
    internal fun `should execute a private suspended function with all keyed arguments`() = runBlocking {
        // given
        val instance = SuspendedReflectionUtilsObject()

        // when
        val value: Int = instance.coInvokeInvisible("divide", named("value", 18), named("divider", 6))
        // then
        assertThat(value).isEqualTo(3)
    }

    @Test
    internal fun `should execute a private suspended function with an omitted value`() = runBlocking {
        // given
        val instance = SuspendedReflectionUtilsObject()

        // when
        val value: Int = instance.coInvokeInvisible("divide", omitted<Int>(), 2)

        // then
        assertThat(value).isEqualTo(5)
    }

    @Test
    internal fun `should execute a private suspended function with an argument and vararg`() = runBlocking {
        // given
        val instance = SuspendedReflectionUtilsObject()

        // when
        val value: Int = instance.coInvokeInvisible("divideSum", 2, vararg(1, 3, 6))

        // then
        assertThat(value).isEqualTo(5)
    }

    @Test
    internal fun `should execute a private suspended function with an argument and null for the vararg`() =
        runBlocking {
            // given
            val instance = SuspendedReflectionUtilsObject()

            // when
            val value: Int = instance.coInvokeInvisible("divideSum", 2, nullOf<Int>())

            // then
            assertThat(value).isEqualTo(0)
        }


    @Test
    internal fun `should execute an inherited suspended function without argument`() = runBlocking {
        // given
        val instance = SuspendedReflectionUtilsObject()

        // when
        val value: Int = instance coInvokeNoArgs "returnInheritedValue"

        // then
        assertThat(value).isEqualTo(789)
    }


    @Test
    internal fun `should execute an inherited suspended function with omitted argument`() = runBlocking {
        // given
        val instance = SuspendedReflectionUtilsObject()

        // when
        val value: Int = instance.coInvokeInvisible("returnInheritedProvidedOrValue", omitted<Int>())

        // then
        assertThat(value).isEqualTo(789)
    }

    @Test
    internal fun `should execute an inherited suspended function with all indexed arguments`() = runBlocking {
        // given
        val instance = SuspendedReflectionUtilsObject()

        // when
        val value: Int = instance.coInvokeInvisible("inheritedDivide", 12, 6)

        // then
        assertThat(value).isEqualTo(2)
    }

    @Test
    internal fun `should execute an inherited suspended function with an argument and vararg`() = runBlocking {
        // given
        val instance = SuspendedReflectionUtilsObject()

        // when
        val value: Int = instance.coInvokeInvisible("inheritedDivideSum", 2, vararg(1, 3, 6))

        // then
        assertThat(value).isEqualTo(5)
    }

    @Test
    internal fun `should throw original exception when executing suspended function`() = runBlocking {
        // given
        val instance = SuspendedReflectionUtilsObject()

        // when
        val exception = assertThrows<Exception> {
            instance.coInvokeInvisible("throwExceptionSuspended")
        }

        assertThat(exception).isInstanceOf(IllegalArgumentException::class)
            .prop(IllegalArgumentException::message).isEqualTo("This is the exception")
    }
}
