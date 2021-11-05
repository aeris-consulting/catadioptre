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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PublicTypeTest {

	@Test
	@DisplayName("should read the value of default property")
	void shouldReadTheValueOfDefaultProperty() {
		Map<String, Double> defaultProperty = new HashMap<>();
		defaultProperty.put("any", 1.0);
		PublicType instance = new PublicType(defaultProperty, 1.0, Optional.empty());

		Map<String, Double> result = TestablePublicType.markers(instance);

		Assertions.assertThat(result).isNotNull().hasSize(1).containsEntry("any", 1.0);
	}

	@Test
	@DisplayName("should write the value of default property")
	void shouldWriteTheValueOfDefaultProperty() {
		Map<String, Double> defaultProperty = new HashMap<>();
		defaultProperty.put("any", 1.0);
		PublicType instance = new PublicType(defaultProperty, 1.0, Optional.empty());

		defaultProperty = new HashMap<>();
		defaultProperty.put("other", 2.0);
		PublicType result = TestablePublicType.markers(instance, defaultProperty);

		Assertions.assertThat(result).isSameAs(instance);
		Assertions.assertThat(TestablePublicType.markers(result)).isNotNull().hasSize(1)
				.containsEntry("other", 2.0);
	}

	@Test
	@DisplayName("should clear the value of default property")
	void shouldClearTheValueOfDefaultProperty() {
		Map<String, Double> defaultProperty = new HashMap<>();
		defaultProperty.put("any", 1.0);
		PublicType instance = new PublicType(defaultProperty, 1.0, Optional.empty());

		PublicType result = TestablePublicType.clearMarkers(instance);

		Assertions.assertThat(result).isSameAs(instance);
		Assertions.assertThat(TestablePublicType.markers(result)).isNull();
	}

	@Test
	@DisplayName("should write and read the value of inherited typed parameter as double")
	void shouldWriteAndReadTheValueOfInheritedTypedParameterAsDouble() {
		Map<String, Double> defaultProperty = new HashMap<>();
		defaultProperty.put("any", 1.0);
		PublicType instance = new PublicType(defaultProperty, 1.0, Optional.empty());

		PublicType result = TestableAbstractCatadioptreExample.typedProperty(instance, 2.0);

		Assertions.assertThat(result).isSameAs(instance);
		Assertions.assertThat(TestableAbstractCatadioptreExample.typedProperty(result)).isEqualTo(2.0);

	}

	@Test
	@DisplayName("should write and read the value of inherited typed parameter as string")
	void shouldWriteAndReadTheValueOfInheritedTypedParameterAsString() {
		Map<String, Double> defaultProperty = new HashMap<>();
		defaultProperty.put("any", 1.0);
		PublicType instance = new PublicType(defaultProperty, 1.0, Optional.empty());

		PublicType result = TestableAbstractCatadioptreExample.typedProperty2(instance,
				Optional.of("this is the value"));

		Assertions.assertThat(result).isSameAs(instance);
		Assertions.assertThat(TestableAbstractCatadioptreExample.typedProperty2(result))
				.isEqualTo(Optional.of("this is the value"));
	}

	@Test
	@DisplayName("should execute the function without argument")
	void shouldExecuteTheFunctionWithoutArgument() {
		Map<String, Double> defaultProperty = new HashMap<>();
		defaultProperty.put("any", 1.0);
		PublicType instance = new PublicType(defaultProperty, 1.0, Optional.empty());

		double result = TestablePublicType.multiplySum(instance, 2.0, new Double[]{1.0, 3.0, 6.0});

		Assertions.assertThat(result).isEqualTo(20.0);
	}

	@Test
	@DisplayName("should execute the function of parent without argument")
	void shouldExecuteTheFunctionOfParentWithoutArgument() {
		Map<String, Double> defaultProperty = new HashMap<>();
		defaultProperty.put("any", 1.0);
		PublicType instance = new PublicType(defaultProperty, 1.0, Optional.empty());

		int result = TestableAbstractCatadioptreExample.getAnything(instance);

		Assertions.assertThat(result).isEqualTo(123);
	}

	@Test
	@DisplayName("should execute the function of parent with variable arguments")
	void shouldExecuteTheFunctionOfParentWithVariableArguments() {
		Map<String, Double> defaultProperty = new HashMap<>();
		defaultProperty.put("any", 1.0);
		PublicType instance = new PublicType(defaultProperty, 1.0, Optional.empty());

		double result = TestableAbstractCatadioptreExample.divideSum(instance, 2.0, new Double[]{1.0, 3.0, 6.0});

		Assertions.assertThat(result).isEqualTo(5.0);
	}

	@Test
	@DisplayName("should execute the function with parameters using lowest visibility")
	void shouldExecuteTheFunctionWithParametersOfLowerVisibility() {
		Map<String, Double> defaultProperty = new HashMap<>();
		defaultProperty.put("any", 1.0);
		PublicType instance = new PublicType(defaultProperty, 1.0, Optional.empty());

		List<PackageType> result = TestablePublicType.callMethodWithLowVisibilityReturnType(instance);

		Assertions.assertThat(result).hasSize(1);
	}
}
