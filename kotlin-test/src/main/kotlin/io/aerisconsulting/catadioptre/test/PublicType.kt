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

@Suppress("kotlin:S1144", "kotlin:S1172")
class PublicType {

    @KTestable
    private fun callMethodWithInternalClass(internalType: InternalType) {
        println("Calling method using a parameter with internal visibility")
    }

    @KTestable
    private fun callMethodWithListInternalClass(internalType: List<InternalType>, text: String) {
        println("Calling method with generic as a type with internal visibility")
    }

    @KTestable
    @jakarta.transaction.Transactional // Adds an annotation absent from the test compilation classpath.
    protected fun callMethodThrowingException(example: String) {
        throw IllegalStateException(example)
    }
}
