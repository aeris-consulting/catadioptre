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

import kotlin.reflect.KClass
import kotlin.reflect.KParameter

/**
 * Descriptor for a function argument passed by the caller.
 *
 * @author Eric Jessé
 */
data class Argument constructor(
    internal val value: Any?,
    internal val type: Parameter,
    internal val isOmitted: Boolean? = null
) {
    internal fun matches(parameter: KParameter): Boolean {
        val result = type.matches(parameter)
                || (parameter.type.classifier as? KClass<*>)?.isInstance(value) == true
        if (result) {
            this.type.actualParameter = parameter
        }
        return result
    }
}
