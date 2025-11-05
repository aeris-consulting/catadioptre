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
import assertk.assertions.isNull
import assertk.assertions.isTrue
import org.junit.jupiter.api.Test
import kotlin.reflect.full.valueParameters

/**
 * Comprehensive tests for the Parameter class covering all matching scenarios.
 */
class ParameterTest {

    private class TestClass {
        fun simpleFunction(arg: String) {}
        fun functionWithInt(value: Int) {}
        fun functionWithNumber(value: Number) {}
        fun varargFunction(vararg values: String) {}
        fun optionalFunction(value: String = "default") {}
        fun multipleParams(name: String, age: Int, optional: Boolean = false) {}
        fun nullableParam(value: String?) {}
    }

    @Test
    fun `should match parameter by classifier only`() {
        // given
        val function = TestClass::simpleFunction
        val kParameter = function.valueParameters[0]
        val parameter = Parameter(String::class)

        // when
        val result = parameter.matches(kParameter)

        // then
        assertThat(result).isTrue()
        // Note: actualParameter is not set by matches(), it's set later by findFunction()
    }

    @Test
    fun `should match parameter by name and classifier`() {
        // given
        val function = TestClass::simpleFunction
        val kParameter = function.valueParameters[0]
        val parameter = Parameter(String::class, name = "arg")

        // when
        val result = parameter.matches(kParameter)

        // then
        assertThat(result).isTrue()
    }

    @Test
    fun `should not match when name is different`() {
        // given
        val function = TestClass::simpleFunction
        val kParameter = function.valueParameters[0]
        val parameter = Parameter(String::class, name = "wrongName")

        // when
        val result = parameter.matches(kParameter)

        // then
        assertThat(result).isFalse()
    }

    @Test
    fun `should match when classifier is null`() {
        // given
        val function = TestClass::simpleFunction
        val kParameter = function.valueParameters[0]
        val parameter = Parameter(classifier = null)

        // when
        val result = parameter.matches(kParameter)

        // then
        assertThat(result).isTrue()
    }

    @Test
    fun `should match subclass to superclass parameter`() {
        // given
        val function = TestClass::functionWithNumber
        val kParameter = function.valueParameters[0]
        val parameter = Parameter(Int::class) // Int is a subclass of Number

        // when
        val result = parameter.matches(kParameter)

        // then
        assertThat(result).isTrue()
    }

    @Test
    fun `should not match superclass to subclass parameter`() {
        // given
        val function = TestClass::functionWithInt
        val kParameter = function.valueParameters[0]
        val parameter = Parameter(Number::class) // Number is superclass of Int

        // when
        val result = parameter.matches(kParameter)

        // then
        assertThat(result).isFalse()
    }

    @Test
    fun `should match vararg parameter`() {
        // given
        val function = TestClass::varargFunction
        val kParameter = function.valueParameters[0]
        val parameter = Parameter(Array<String>::class, isVararg = true)

        // when
        val result = parameter.matches(kParameter)

        // then
        assertThat(result).isTrue()
    }

    @Test
    fun `should not match vararg when isVararg is false`() {
        // given
        val function = TestClass::varargFunction
        val kParameter = function.valueParameters[0]
        val parameter = Parameter(Array<String>::class, isVararg = false)

        // when
        val result = parameter.matches(kParameter)

        // then
        assertThat(result).isFalse()
    }

    @Test
    fun `should match optional parameter`() {
        // given
        val function = TestClass::optionalFunction
        val kParameter = function.valueParameters[0]
        val parameter = Parameter(String::class, isOptional = true)

        // when
        val result = parameter.matches(kParameter)

        // then
        assertThat(result).isTrue()
    }

    @Test
    fun `should not match optional when isOptional is false`() {
        // given
        val function = TestClass::optionalFunction
        val kParameter = function.valueParameters[0]
        val parameter = Parameter(String::class, isOptional = false)

        // when
        val result = parameter.matches(kParameter)

        // then
        assertThat(result).isFalse()
    }

    @Test
    fun `should match when all criteria are met`() {
        // given
        val function = TestClass::optionalFunction
        val kParameter = function.valueParameters[0]
        val parameter = Parameter(
            classifier = String::class,
            name = "value",
            isOptional = true,
            isVararg = false
        )

        // when
        val result = parameter.matches(kParameter)

        // then
        assertThat(result).isTrue()
    }

    @Test
    fun `should not match when one criterion fails`() {
        // given
        val function = TestClass::optionalFunction
        val kParameter = function.valueParameters[0]
        val parameter = Parameter(
            classifier = String::class,
            name = "wrongName",  // Wrong name
            isOptional = true,
            isVararg = false
        )

        // when
        val result = parameter.matches(kParameter)

        // then
        assertThat(result).isFalse()
    }

    @Test
    fun `should match vararg with component type`() {
        // given
        val function = TestClass::varargFunction
        val kParameter = function.valueParameters[0]
        // String component type for String[] vararg
        val parameter = Parameter(String::class, isVararg = true)

        // when
        val result = parameter.matches(kParameter)

        // then
        assertThat(result).isTrue()
    }

    @Test
    fun `actualParameter should remain null after match`() {
        // given
        val function = TestClass::simpleFunction
        val kParameter = function.valueParameters[0]
        val parameter = Parameter(String::class)
        assertThat(parameter.actualParameter).isNull()

        // when
        parameter.matches(kParameter)

        // then
        // actualParameter is not set by matches(), it remains null
        assertThat(parameter.actualParameter).isNull()
    }

    @Test
    fun `should not set actualParameter when match fails`() {
        // given
        val function = TestClass::simpleFunction
        val kParameter = function.valueParameters[0]
        val parameter = Parameter(Int::class) // Wrong type
        assertThat(parameter.actualParameter).isNull()

        // when
        parameter.matches(kParameter)

        // then
        assertThat(parameter.actualParameter).isNull()
    }

    @Test
    fun `should match when name is null regardless of actual parameter name`() {
        // given
        val function = TestClass::multipleParams
        val kParameter = function.valueParameters[0]
        val parameter = Parameter(String::class, name = null)

        // when
        val result = parameter.matches(kParameter)

        // then
        assertThat(result).isTrue()
    }

    @Test
    fun `should match when isVararg is null regardless of actual vararg status`() {
        // given
        val function = TestClass::simpleFunction
        val kParameter = function.valueParameters[0]
        val parameter = Parameter(String::class, isVararg = null)

        // when
        val result = parameter.matches(kParameter)

        // then
        assertThat(result).isTrue()
    }

    @Test
    fun `should match when isOptional is null regardless of actual optional status`() {
        // given
        val function = TestClass::optionalFunction
        val kParameter = function.valueParameters[0]
        val parameter = Parameter(String::class, isOptional = null)

        // when
        val result = parameter.matches(kParameter)

        // then
        assertThat(result).isTrue()
    }
}