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
import org.junit.jupiter.api.Test

internal class ReflectionPropertyUtilsTest {

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

}


