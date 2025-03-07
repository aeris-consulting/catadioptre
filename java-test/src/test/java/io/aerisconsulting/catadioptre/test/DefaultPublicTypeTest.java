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

import io.aerisconsulting.catadioptre.CatadioptreOriginalCauseException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DefaultPublicTypeTest {

	@Test
	@DisplayName("should execute the private method in class with default visibility")
	void shouldExecuteThePrivateToLowerCaseFunction() {
		PackageType instance = new PackageType();
		String result = TestablePackageType.toLowerCase(instance, "T");
		Assertions.assertThat(result).isEqualTo("t");
	}

	@Test
	@DisplayName("should throw the same exception than the real method")
	void shouldThrowSameExceptionRealMethod() {
		PackageType instance = new PackageType();

		Assertions.assertThatExceptionOfType(CatadioptreOriginalCauseException.class).isThrownBy(() -> {
			TestablePackageType.callMethodThrowingException(instance, "test");
		}).withCauseInstanceOf(IllegalStateException.class);
	}
}
