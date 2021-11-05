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

import io.aerisconsulting.catadioptre.Testable;
import java.util.Map;

@SuppressWarnings("java:S1186")
public class WithGeneric {

	@Testable
	private Map<String, Converted> filterMap(Map<String, Converted> values) {
		return values;
	}

	@Testable
	private void processGenericStarAndHasNoReturnedValue(Specification<?, Object, ?> spec) {
	}

	@Testable
	private void processGenericReferencingItself(
			Specification<Object, Object, ? extends Specification<Object, Object, ? extends Specification<Object, Object, ?>>> spec) {
	}
}

interface Converted {

}

@SuppressWarnings("java:S119")
interface Specification<INPUT, OUTPUT, SELF extends Specification<INPUT, OUTPUT, SELF>> {

}
