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

import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField

/**
 * Wrapper to proceed with the mutation of [property] on [instance].
 *
 * @author Eric Jessé
 */
class DynamicSetter<T>(
    private val instance: Any,
    private val property: KProperty1<T, *>
) {

    infix fun being(value: Any?) {
        set(value)
    }

    private fun set(value: Any?) {
        if (property is KMutableProperty<*>) {
            property.isAccessible = true
            property.setter.call(instance, value)
        } else {
            property.javaField!!.also {
                it.isAccessible = true
                it.set(instance, value)
            }
        }
    }

}
