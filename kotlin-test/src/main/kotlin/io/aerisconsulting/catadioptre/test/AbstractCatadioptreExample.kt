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

import io.aerisconsulting.catadioptre.KTestable

abstract class AbstractCatadioptreExample<T : Number, V : Any> : CatadioptreInterface<T> {

    @KTestable
    private var defaultArgumentTypedProperty: T? = null

    @KTestable
    private var defaultArgumentTypedProperty2: V? = null

    private var defaultDivider: Double = 1.0

    @KTestable
    @Suppress("kotlin:S1144")
    private fun divideSum(divider: Double = defaultDivider, vararg valuesToSum: Double?): Double {
        return valuesToSum.filterNotNull().sum() / divider
    }

    @KTestable
    @Suppress("kotlin:S1144")
    private fun getAnything() = 123

    @KTestable
    @Suppress("kotlin:S1144")
    private fun <W : Number> sumAsDouble(part1: T, part2: W): Double {
        return part1.toDouble() + part2.toDouble()
    }

    companion object {

        @KTestable
        @Suppress("kotlin:S1144")
        private fun returnTwo() = 2.0

    }
}

interface CatadioptreInterface<T : Any>
