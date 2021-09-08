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
package io.aerisconsulting.catadioptre.kotlin

import com.squareup.kotlinpoet.KModifier

/**
 * Priority-sorted visibility for types in the [KotlinTestableProcessor].
 * Packaged-protected is currently not supported by Kotlin.
 *
 * @property klassModifiers list of the equivalent KModifier from KotlinPoet, the first being the default instance.
 *
 * @author Gabriel Moraes
 */
internal enum class KClassVisibility(val klassModifiers: List<KModifier>) {
    PRIVATE(listOf(KModifier.PRIVATE, KModifier.PROTECTED)),
    INTERNAL(listOf(KModifier.INTERNAL)),
    PUBLIC(listOf(KModifier.PUBLIC));

    companion object {

        @JvmStatic
        fun getByVisibility(klassModifier: KModifier): KClassVisibility {
            val klassVisibility = values().firstOrNull { klassModifier in it.klassModifiers }
            return klassVisibility ?: throw IllegalArgumentException("KModifier $klassModifier not supported")
        }
    }
}
