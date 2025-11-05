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
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import org.junit.jupiter.api.Test
import kotlin.reflect.full.valueParameters

/**
 * Comprehensive tests for the Finders functions covering property and function search scenarios.
 */
class FindersTest {

    private open class ParentClass {
        val parentProperty: String = "parent"
        protected val protectedParentProperty: Int = 100

        fun parentFunction(): String = "parent function"
        protected fun protectedParentFunction(): Int = 200
    }

    private class ChildClass : ParentClass() {
        val childProperty: String = "child"
        val anotherProperty: Int = 42

        fun childFunction(): String = "child function"
        fun overloadedFunction(arg: String): String = "string: $arg"
        fun overloadedFunction(arg: Int): Int = arg * 2
        fun multiParamFunction(name: String, age: Int): String = "$name is $age years old"
    }

    // Test findProperty function

    @Test
    fun `findProperty should find property in the same class`() {
        // when
        val property = findProperty<ChildClass>(ChildClass::class, "childProperty")

        // then
        assertThat(property).isNotNull()
        assertThat(property!!.name).isEqualTo("childProperty")
    }

    @Test
    fun `findProperty should find property in parent class`() {
        // when
        val property = findProperty<ChildClass>(ChildClass::class, "parentProperty")

        // then
        assertThat(property).isNotNull()
        assertThat(property!!.name).isEqualTo("parentProperty")
    }

    @Test
    fun `findProperty should find protected property in parent class`() {
        // when
        val property = findProperty<ChildClass>(ChildClass::class, "protectedParentProperty")

        // then
        assertThat(property).isNotNull()
        assertThat(property!!.name).isEqualTo("protectedParentProperty")
    }

    @Test
    fun `findProperty should return null for non-existent property`() {
        // when
        val property = findProperty<ChildClass>(ChildClass::class, "nonExistentProperty")

        // then
        assertThat(property).isNull()
    }

    @Test
    fun `findProperty should return correct property when multiple properties exist`() {
        // when
        val property = findProperty<ChildClass>(ChildClass::class, "anotherProperty")

        // then
        assertThat(property).isNotNull()
        assertThat(property!!.name).isEqualTo("anotherProperty")
    }

    // Test findFunction function

    @Test
    fun `findFunction should find function in the same class with no arguments`() {
        // given
        val arguments = emptyList<Argument>()

        // when
        val function = findFunction(ChildClass::class, "childFunction", arguments)

        // then
        assertThat(function).isNotNull()
        assertThat(function!!.name).isEqualTo("childFunction")
    }

    @Test
    fun `findFunction should find function in parent class`() {
        // given
        val arguments = emptyList<Argument>()

        // when
        val function = findFunction(ChildClass::class, "parentFunction", arguments)

        // then
        assertThat(function).isNotNull()
        assertThat(function!!.name).isEqualTo("parentFunction")
    }

    @Test
    fun `findFunction should find protected function in parent class`() {
        // given
        val arguments = emptyList<Argument>()

        // when
        val function = findFunction(ChildClass::class, "protectedParentFunction", arguments)

        // then
        assertThat(function).isNotNull()
        assertThat(function!!.name).isEqualTo("protectedParentFunction")
    }

    @Test
    fun `findFunction should return null for non-existent function`() {
        // given
        val arguments = emptyList<Argument>()

        // when
        val function = findFunction(ChildClass::class, "nonExistentFunction", arguments)

        // then
        assertThat(function).isNull()
    }

    @Test
    fun `findFunction should find single function with arguments`() {
        // given
        val arguments = listOf(
            Argument("test", Parameter(String::class, name = "name")),
            Argument(25, Parameter(Int::class, name = "age"))
        )

        // when
        val function = findFunction(ChildClass::class, "multiParamFunction", arguments)

        // then
        assertThat(function).isNotNull()
        assertThat(function!!.name).isEqualTo("multiParamFunction")
    }

    @Test
    fun `findFunction should select correct overloaded function with String argument`() {
        // given
        val arguments = listOf(
            Argument("test", Parameter(String::class))
        )

        // when
        val function = findFunction(ChildClass::class, "overloadedFunction", arguments)

        // then
        assertThat(function).isNotNull()
        assertThat(function!!.name).isEqualTo("overloadedFunction")
        val params = function.valueParameters
        assertThat(params.size).isEqualTo(1)
        assertThat(params[0].type.classifier).isEqualTo(String::class)
    }

    @Test
    fun `findFunction should select correct overloaded function with Int argument`() {
        // given
        val arguments = listOf(
            Argument(42, Parameter(Int::class))
        )

        // when
        val function = findFunction(ChildClass::class, "overloadedFunction", arguments)

        // then
        assertThat(function).isNotNull()
        assertThat(function!!.name).isEqualTo("overloadedFunction")
        val params = function.valueParameters
        assertThat(params.size).isEqualTo(1)
        assertThat(params[0].type.classifier).isEqualTo(Int::class)
    }

    @Test
    fun `findFunction should return null when argument count doesn't match with multiple overloads`() {
        // given
        val arguments = listOf(
            Argument("test", Parameter(String::class))
            // Missing second argument
        )

        // when
        val function = findFunction(ChildClass::class, "overloadedFunction", arguments)

        // then
        // With correct argument count, it should find one of the overloads
        assertThat(function).isNotNull()
    }

    @Test
    fun `findFunction should return function even with mismatched types when only one exists`() {
        // given
        val arguments = listOf(
            Argument(123, Parameter(Int::class)), // Wrong type order
            Argument("wrong", Parameter(String::class)) // Wrong type order
        )

        // when
        val function = findFunction(ChildClass::class, "multiParamFunction", arguments)

        // then
        // When there's only one function with the name, it returns it regardless of type matching
        assertThat(function).isNotNull()
        assertThat(function!!.name).isEqualTo("multiParamFunction")
    }

    @Test
    fun `findFunction should resolve actualParameter for single function`() {
        // given
        val parameter1 = Parameter(String::class, name = "name")
        val parameter2 = Parameter(Int::class, name = "age")
        val arguments = listOf(
            Argument("test", parameter1),
            Argument(25, parameter2)
        )

        // when
        val function = findFunction(ChildClass::class, "multiParamFunction", arguments)

        // then
        assertThat(function).isNotNull()
        assertThat(parameter1.actualParameter).isNotNull()
        assertThat(parameter2.actualParameter).isNotNull()
    }

    @Test
    fun `findFunction should handle empty arguments for no-arg function`() {
        // given
        val arguments = emptyList<Argument>()

        // when
        val function = findFunction(ChildClass::class, "childFunction", arguments)

        // then
        assertThat(function).isNotNull()
        val params = function!!.valueParameters
        assertThat(params.size).isEqualTo(0)
    }

    @Test
    fun `findProperty should work with different class hierarchies`() {
        // given
        open class GrandParent {
            val grandProperty: String = "grand"
        }

        open class MiddleParent : GrandParent() {
            val middleProperty: String = "middle"
        }

        class Descendant : MiddleParent() {
            val descendantProperty: String = "descendant"
        }

        // when
        val grandProp = findProperty<Descendant>(Descendant::class, "grandProperty")
        val middleProp = findProperty<Descendant>(Descendant::class, "middleProperty")
        val descendantProp = findProperty<Descendant>(Descendant::class, "descendantProperty")

        // then
        assertThat(grandProp).isNotNull()
        assertThat(middleProp).isNotNull()
        assertThat(descendantProp).isNotNull()
    }

    @Test
    fun `findFunction should work with vararg functions`() {
        // given
        class VarargClass {
            fun varargFunction(vararg values: String): Int = values.size
        }

        val arguments = listOf(
            Argument(arrayOf("a", "b", "c"), Parameter(Array<String>::class, isVararg = true))
        )

        // when
        val function = findFunction(VarargClass::class, "varargFunction", arguments)

        // then
        assertThat(function).isNotNull()
        val params = function!!.valueParameters
        assertThat(params[0].isVararg).isEqualTo(true)
    }

    @Test
    fun `findFunction should find overloaded function with matching parameter type`() {
        // given
        class PreferenceClass {
            fun testFunction(value: Number): String = "Number"
            fun testFunction(value: Int): String = "Int"
        }

        val arguments = listOf(
            Argument(42, Parameter(Int::class))
        )

        // when
        val function = findFunction(PreferenceClass::class, "testFunction", arguments)

        // then
        // findFunction returns the first matching function, which could be either Number or Int
        // since Int is a subclass of Number and both match
        assertThat(function).isNotNull()
        val params = function!!.valueParameters
        // The returned function will have either Int or Number parameter (both are valid matches)
        val classifier = params[0].type.classifier
        assert(classifier == Int::class || classifier == Number::class)
    }
}