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
package io.aerisconsulting.catadioptre.test;

import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

@SuppressWarnings("java:S2699")
class WithGenericTest {

	WithGeneric instance = new WithGeneric();

	@Test
	void shouldCompileCallOfAFunctionWithGenericsAsArgumentAndReturnedValue() {
		Map<String, Converted> argument = new HashMap();
		argument.put("1", mock(Converted.class));

		TestableWithGeneric.filterMap(instance, argument);
	}

	@Test
	void shouldCompileCallOfAFunctionWithGenericStarAsArgument() {
		TestableWithGeneric.processGenericStarAndHasNoReturnedValue(instance, mock(Specification.class));
	}

	@Test
	void shouldCompileCallOfAFunctionWithGenericReferencingItselfAsArgument() {
		TestableWithGeneric.processGenericReferencingItself(instance, mock(Specification.class));
	}

}