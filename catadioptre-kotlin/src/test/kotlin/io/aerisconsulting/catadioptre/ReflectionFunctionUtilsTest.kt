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
import assertk.assertions.isIn
import assertk.assertions.isNull
import assertk.assertions.isSameAs
import kotlinx.coroutines.test.runBlockingTest
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
        val cause = assertThrows<CatadioptreOriginalCauseException> {
            instance.invokeInvisible("throwException")
        }.cause

        assertThat(cause?.javaClass).isEqualTo(java.lang.RuntimeException::class.java)
    }

    @Test
    internal fun `should execute a private suspended function without argument`() = runBlockingTest {
        // given
        val instance = SuspendedReflectionUtilsObject()

        // when
        val value: Int = instance coInvokeNoArgs "returnValue"

        // then
        assertThat(value).isEqualTo(123)
    }

    @Test
    internal fun `should execute a private suspended function with keyed null argument`() = runBlockingTest {
        // given
        val instance = SuspendedReflectionUtilsObject()

        // when
        val value: Int? = instance.coInvokeInvisible("returnProvidedOrValue", namedNull<Int>("default"))

        // then
        assertThat(value).isNull()
    }

    @Test
    internal fun `should execute a private suspended function with omitted argument`() = runBlockingTest {
        // given
        val instance = SuspendedReflectionUtilsObject()

        // when
        val value: Int = instance.coInvokeInvisible("returnProvidedOrValue", omitted<Int>())

        // then
        assertThat(value).isEqualTo(123)
    }

    @Test
    internal fun `should execute a private suspended function with all indexed arguments`() = runBlockingTest {
        // given
        val instance = SuspendedReflectionUtilsObject()

        // when
        val value: Int = instance.coInvokeInvisible("divide", 12, 6)

        // then
        assertThat(value).isEqualTo(2)
    }

    @Test
    internal fun `should execute a private suspended function with all keyed arguments`() = runBlockingTest {
        // given
        val instance = SuspendedReflectionUtilsObject()

        // when
        val value: Int = instance.coInvokeInvisible("divide", named("value", 18), named("divider", 6))
        // then
        assertThat(value).isEqualTo(3)
    }

    @Test
    internal fun `should execute a private suspended function with an omitted value`() = runBlockingTest {
        // given
        val instance = SuspendedReflectionUtilsObject()

        // when
        val value: Int = instance.coInvokeInvisible("divide", omitted<Int>(), 2)

        // then
        assertThat(value).isEqualTo(5)
    }

    @Test
    internal fun `should execute a private suspended function with an argument and vararg`() = runBlockingTest {
        // given
        val instance = SuspendedReflectionUtilsObject()

        // when
        val value: Int = instance.coInvokeInvisible("divideSum", 2, vararg(1, 3, 6))

        // then
        assertThat(value).isEqualTo(5)
    }

    @Test
    internal fun `should execute a private suspended function with an argument and null for the vararg`() =
        runBlockingTest {
            // given
            val instance = SuspendedReflectionUtilsObject()

            // when
            val value: Int = instance.coInvokeInvisible("divideSum", 2, nullOf<Int>())

            // then
            assertThat(value).isEqualTo(0)
        }


    @Test
    internal fun `should execute an inherited suspended function without argument`() = runBlockingTest {
        // given
        val instance = SuspendedReflectionUtilsObject()

        // when
        val value: Int = instance coInvokeNoArgs "returnInheritedValue"

        // then
        assertThat(value).isEqualTo(789)
    }


    @Test
    internal fun `should execute an inherited suspended function with omitted argument`() = runBlockingTest {
        // given
        val instance = SuspendedReflectionUtilsObject()

        // when
        val value: Int = instance.coInvokeInvisible("returnInheritedProvidedOrValue", omitted<Int>())

        // then
        assertThat(value).isEqualTo(789)
    }

    @Test
    internal fun `should execute an inherited suspended function with all indexed arguments`() = runBlockingTest {
        // given
        val instance = SuspendedReflectionUtilsObject()

        // when
        val value: Int = instance.coInvokeInvisible("inheritedDivide", 12, 6)

        // then
        assertThat(value).isEqualTo(2)
    }

    @Test
    internal fun `should execute an inherited suspended function with an argument and vararg`() = runBlockingTest {
        // given
        val instance = SuspendedReflectionUtilsObject()

        // when
        val value: Int = instance.coInvokeInvisible("inheritedDivideSum", 2, vararg(1, 3, 6))

        // then
        assertThat(value).isEqualTo(5)
    }

    @Test
    internal fun `should throw original exception when executing suspended function`() = runBlockingTest {
        // given
        val instance = SuspendedReflectionUtilsObject()

        // when
        val cause = assertThrows<CatadioptreOriginalCauseException> {
            instance.coInvokeInvisible("throwExceptionSuspended")
        }.cause
        assertThat(cause?.javaClass).isEqualTo(java.lang.RuntimeException::class.java)
    }

}
