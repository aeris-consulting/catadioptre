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
package io.aerisconsulting.catadioptre.example

import io.aerisconsulting.catadioptre.example.catadioptre.callMethodThrowingException
import io.aerisconsulting.catadioptre.example.catadioptre.callMethodWithInternalClass
import io.aerisconsulting.catadioptre.example.catadioptre.callMethodWithListInternalClass
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class PublicCatadioptreExampleTest {

    @Test
    internal fun `should call method on public class with internal modifier parameter`() {
        val instance = PublicCatadioptreExample()
        val instanceExample = CatadioptreExample()

        instance.callMethodWithInternalClass(instanceExample)
    }

    @Test
    internal fun `should call method on public class with internal modifier parameter used in generic type`() {
        val instance = PublicCatadioptreExample()
        val instanceExample = CatadioptreExample()

        instance.callMethodWithListInternalClass(listOf(instanceExample), "test")
    }

    @Test
    internal fun `should throw the same exception that the real method`() {
        val instance = PublicCatadioptreExample()

        assertThrows<IllegalStateException> {
            instance.callMethodThrowingException("test")
        }
    }
}
