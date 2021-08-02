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
package io.aerisconsulting.katadioptre.example

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.isSameAs
import io.aerisconsulting.katadioptre.example.katadioptre.clearDefaultProperty
import io.aerisconsulting.katadioptre.example.katadioptre.defaultArgumentTypedProperty
import io.aerisconsulting.katadioptre.example.katadioptre.defaultArgumentTypedProperty2
import io.aerisconsulting.katadioptre.example.katadioptre.defaultProperty
import io.aerisconsulting.katadioptre.example.katadioptre.divideSum
import io.aerisconsulting.katadioptre.example.katadioptre.getAnything
import io.aerisconsulting.katadioptre.example.katadioptre.multiplySum
import org.junit.jupiter.api.Test
import java.util.Optional

@java.lang.SuppressWarnings("kotlin:S100")
internal class KatadioptreExampleTest {

    @Test
    internal fun `should read the value of default property`() {
        val instance = KatadioptreExample()

        val result = instance.defaultProperty()

        assertThat(result).isEqualTo(mapOf("any" to 1.0))
    }

    @Test
    internal fun `should write the value of default property`() {
        val instance = KatadioptreExample()

        val result = instance.defaultProperty(mapOf("other" to 2.0))

        assertThat(result).isSameAs(instance)
        assertThat(instance.defaultProperty()).isEqualTo(mapOf("other" to 2.0))
    }

    @Test
    internal fun `should clear the value of nullable default property`() {
        val instance = KatadioptreExample()

        val result = instance.clearDefaultProperty()

        assertThat(result).isSameAs(instance)
        assertThat(instance.defaultProperty()).isNull()
    }

    @Test
    internal fun `should write and read the value of inherited typed parameter as double`() {
        val instance = KatadioptreExample()

        val result = instance.defaultArgumentTypedProperty(2.0)

        assertThat(result).isSameAs(instance)
        assertThat(instance.defaultArgumentTypedProperty()).isEqualTo(2.0)
    }

    @Test
    internal fun `should write and read the value of inherited typed parameter as string`() {
        val instance = KatadioptreExample()

        val result = instance.defaultArgumentTypedProperty2(Optional.of("this is the value"))

        assertThat(result).isSameAs(instance)
        assertThat(instance.defaultArgumentTypedProperty2()).isEqualTo(Optional.of("this is the value"))
    }

    @Test
    internal fun `should execute the function without argument`() {
        val instance = KatadioptreExample()

        val result = instance.multiplySum(2.0, arrayOf(1.0, 3.0, 6.0))

        assertThat(result).isEqualTo(20.0)
    }

    @Test
    internal fun `should execute the function of parent without argument`() {
        val instance = KatadioptreExample()

        val result = instance.getAnything()

        assertThat(result).isEqualTo(123)
    }

    @Test
    internal fun `should execute the function of parent with variable arguments`() {
        val instance = KatadioptreExample()

        val result = instance.divideSum(2.0, arrayOf(1.0, 3.0, 6.0))

        assertThat(result).isEqualTo(5.0)
    }

}
