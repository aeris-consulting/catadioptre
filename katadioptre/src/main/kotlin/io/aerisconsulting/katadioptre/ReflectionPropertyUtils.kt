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

import kotlin.reflect.KProperty
import kotlin.reflect.jvm.isAccessible

/**
 * Sets [value] in the property or field called [propertyName] of the instance.
 *
 * Usage:
 * ```
 * instance.setProperty("value", 456)
 * ```
 */
fun <T : Any> T.setProperty(propertyName: String, value: Any?): T {
    this.withProperty(propertyName).being(value)
    return this
}

/**
 * Infix form of [withProperty].
 *
 * Usage:
 * ```
 * instance withProperty "value" being 456
 * ```
 */
infix fun Any.withProperty(propertyName: String): DynamicSetter<Any> {
    val property = findProperty<Any>(this::class, propertyName)
        ?: throw IllegalArgumentException("The property $propertyName could not be found")
    return DynamicSetter(this, property)
}

/**
 * Sets the property or field called [propertyName] to null.
 *
 * Usage:
 * ```
 * instance clearProperty "value"
 * ```
 */
infix fun <T : Any> T.clearProperty(propertyName: String): T {
    this.withProperty(propertyName).being(null)
    return this
}


/**
 * Returns the value of the property or field called [propertyName] of the instance.
 *
 * Usage:
 * ```
 * val value: Int = instance getProperty "value"
 * ```
 */
@Suppress("UNCHECKED_CAST")
infix fun <T> Any.getProperty(propertyName: String): T {
    val property = findProperty<T>(this::class, propertyName)
    return if (property is KProperty<*>) {
        property.isAccessible = true
        property.getter.call(this) as T
    } else {
        throw IllegalArgumentException("The property $propertyName could not be found")
    }
}
