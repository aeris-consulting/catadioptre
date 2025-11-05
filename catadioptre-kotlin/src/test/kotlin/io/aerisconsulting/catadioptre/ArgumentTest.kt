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
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import org.junit.jupiter.api.Test
import kotlin.reflect.full.valueParameters

/**
 * Comprehensive tests for the Argument class covering all matching scenarios.
 */
class ArgumentTest {

    private class TestClass {
        fun stringFunction(arg: String) {}
        fun intFunction(value: Int) {}
        fun numberFunction(value: Number) {}
        fun anyFunction(value: Any) {}
        fun nullableFunction(value: String?) {}
    }

    @Test
    fun `should match argument by Parameter type matching`() {
        // given
        val function = TestClass::stringFunction
        val kParameter = function.valueParameters[0]
        val parameter = Parameter(String::class)
        val argument = Argument("test value", parameter)

        // when
        val result = argument.matches(kParameter)

        // then
        assertThat(result).isTrue()
        assertThat(parameter.actualParameter).isNotNull()
    }

    @Test
    fun `should match argument by value instance check when Parameter does not match`() {
        // given
        val function = TestClass::anyFunction
        val kParameter = function.valueParameters[0]
        val parameter = Parameter(String::class)
        val argument = Argument("test value", parameter)

        // when
        val result = argument.matches(kParameter)

        // then
        assertThat(result).isTrue()
    }

    @Test
    fun `should match null value argument`() {
        // given
        val function = TestClass::nullableFunction
        val kParameter = function.valueParameters[0]
        val parameter = Parameter(String::class)
        val argument = Argument(null, parameter)

        // when
        val result = argument.matches(kParameter)

        // then
        assertThat(result).isTrue()
    }

    @Test
    fun `should not match when neither Parameter matches nor value is instance`() {
        // given
        val function = TestClass::intFunction
        val kParameter = function.valueParameters[0]
        val parameter = Parameter(String::class) // Wrong type
        val argument = Argument("string value", parameter) // String value for Int parameter

        // when
        val result = argument.matches(kParameter)

        // then
        assertThat(result).isFalse()
    }

    @Test
    fun `should set actualParameter on type when match succeeds`() {
        // given
        val function = TestClass::stringFunction
        val kParameter = function.valueParameters[0]
        val parameter = Parameter(String::class)
        val argument = Argument("test", parameter)
        assertThat(parameter.actualParameter).isNull()

        // when
        argument.matches(kParameter)

        // then
        assertThat(parameter.actualParameter).isNotNull()
    }

    @Test
    fun `should not set actualParameter when match fails`() {
        // given
        val function = TestClass::intFunction
        val kParameter = function.valueParameters[0]
        val parameter = Parameter(String::class)
        val argument = Argument("wrong type", parameter)
        assertThat(parameter.actualParameter).isNull()

        // when
        argument.matches(kParameter)

        // then
        assertThat(parameter.actualParameter).isNull()
    }

    @Test
    fun `should match Integer value to Number parameter`() {
        // given
        val function = TestClass::numberFunction
        val kParameter = function.valueParameters[0]
        val parameter = Parameter(Int::class)
        val argument = Argument(42, parameter)

        // when
        val result = argument.matches(kParameter)

        // then
        assertThat(result).isTrue()
    }

    @Test
    fun `should match with omitted flag set to true`() {
        // given
        val function = TestClass::stringFunction
        val kParameter = function.valueParameters[0]
        val parameter = Parameter(String::class)
        val argument = Argument("value", parameter, isOmitted = true)

        // when
        val result = argument.matches(kParameter)

        // then
        assertThat(result).isTrue()
    }

    @Test
    fun `should match with omitted flag set to false`() {
        // given
        val function = TestClass::stringFunction
        val kParameter = function.valueParameters[0]
        val parameter = Parameter(String::class)
        val argument = Argument("value", parameter, isOmitted = false)

        // when
        val result = argument.matches(kParameter)

        // then
        assertThat(result).isTrue()
    }

    @Test
    fun `should match with omitted flag set to null`() {
        // given
        val function = TestClass::stringFunction
        val kParameter = function.valueParameters[0]
        val parameter = Parameter(String::class)
        val argument = Argument("value", parameter, isOmitted = null)

        // when
        val result = argument.matches(kParameter)

        // then
        assertThat(result).isTrue()
    }

    @Test
    fun `should match when value is exact type`() {
        // given
        val function = TestClass::stringFunction
        val kParameter = function.valueParameters[0]
        val parameter = Parameter(Any::class) // Parameter doesn't match exactly
        val argument = Argument("exact string", parameter)

        // when - Should match by isInstance check
        val result = argument.matches(kParameter)

        // then
        assertThat(result).isTrue()
    }

    @Test
    fun `should match complex object types`() {
        // given
        data class CustomType(val value: String)

        class TestClassWithCustomType {
            fun customFunction(custom: CustomType) {}
        }

        val function = TestClassWithCustomType::customFunction
        val kParameter = function.valueParameters[0]
        val customValue = CustomType("test")
        val parameter = Parameter(CustomType::class)
        val argument = Argument(customValue, parameter)

        // when
        val result = argument.matches(kParameter)

        // then
        assertThat(result).isTrue()
    }

    @Test
    fun `should handle vararg arguments`() {
        // given
        class TestClassWithVararg {
            fun varargFunction(vararg values: String) {}
        }

        val function = TestClassWithVararg::varargFunction
        val kParameter = function.valueParameters[0]
        val parameter = Parameter(Array<String>::class, isVararg = true)
        val argument = Argument(arrayOf("a", "b", "c"), parameter)

        // when
        val result = argument.matches(kParameter)

        // then
        assertThat(result).isTrue()
    }
}