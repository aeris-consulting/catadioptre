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
package io.aerisconsulting.catadioptre.test

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.isSameAs
import io.aerisconsulting.catadioptre.test.catadioptre.clearDefaultProperty
import io.aerisconsulting.catadioptre.test.catadioptre.defaultArgumentTypedProperty
import io.aerisconsulting.catadioptre.test.catadioptre.defaultArgumentTypedProperty2
import io.aerisconsulting.catadioptre.test.catadioptre.defaultProperty
import io.aerisconsulting.catadioptre.test.catadioptre.divideSum
import io.aerisconsulting.catadioptre.test.catadioptre.getAnything
import io.aerisconsulting.catadioptre.test.catadioptre.multiplySum
import io.aerisconsulting.catadioptre.test.catadioptre.self
import io.aerisconsulting.catadioptre.test.catadioptre.sumAsDouble
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.util.Optional

internal class InternalTypeTest {

    @Test
    internal fun `should get self as type argument`() {
        val instance = InternalType()

        val result = instance.self()

        assertThat(result).isSameAs(result)
    }

    @Test
    internal fun `should read the value of default property`() {
        val instance = InternalType()

        val result = instance.defaultProperty()

        assertThat(result).isEqualTo(mapOf("any" to 1.0))
    }

    @Test
    internal fun `should write the value of default property`() {
        val instance = InternalType()

        val result = instance.defaultProperty(mapOf("other" to 2.0))

        assertThat(result).isSameAs(instance)
        assertThat(instance.defaultProperty()).isEqualTo(mapOf("other" to 2.0))
    }

    @Test
    internal fun `should clear the value of nullable default property`() {
        val instance = InternalType()

        val result = instance.clearDefaultProperty()

        assertThat(result).isSameAs(instance)
        assertThat(instance.defaultProperty()).isNull()
    }

    @Test
    internal fun `should write and read the value of inherited typed parameter as double`() {
        val instance = InternalType()

        val result = instance.defaultArgumentTypedProperty(2.0)

        assertThat(result).isSameAs(instance)
        assertThat(instance.defaultArgumentTypedProperty()).isEqualTo(2.0)
    }

    @Test
    internal fun `should write and read the value of inherited typed parameter as string`() {
        val instance = InternalType()

        val result = instance.defaultArgumentTypedProperty2(Optional.of("this is the value"))

        assertThat(result).isSameAs(instance)
        assertThat(instance.defaultArgumentTypedProperty2()).isEqualTo(Optional.of("this is the value"))
    }

    @Test
    internal fun `should execute the function without argument`() {
        val instance = InternalType()

        val result = instance.multiplySum(2.0, 1.0, 3.0, 6.0)

        assertThat(result).isEqualTo(20.0)
    }

    @Test
    internal fun `should execute the function of parent without argument`() {
        val instance = InternalType()

        val result = instance.getAnything()

        assertThat(result).isEqualTo(123)
    }

    @Test
    internal fun `should execute the suspend function of parent with variable arguments`() = runBlocking {
        val instance = InternalType()

        val result = instance.divideSum(2.0, 1.0, 3.0, 6.0)

        assertThat(result).isEqualTo(5.0)
    }

    @Test
    internal fun `should execute the function of parent with variable type arguments`() {
        val instance = InternalType()

        val result = instance.sumAsDouble(2.546, 1534L)

        assertThat(result).isEqualTo(1536.546)
    }

}
