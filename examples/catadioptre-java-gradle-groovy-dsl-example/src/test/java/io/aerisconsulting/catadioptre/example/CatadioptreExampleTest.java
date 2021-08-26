package io.aerisconsulting.catadioptre.example;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CatadioptreExampleTest {

	@Test
	@DisplayName("should read the value of default property")
	void shouldReadTheValueOfDefaultProperty() {
		Map<String, Double> defaultProperty = new HashMap<>();
		defaultProperty.put("any", 1.0);
		CatadioptreExample instance = new CatadioptreExample(defaultProperty, 1.0, Optional.empty());

		Map<String, Double> result = TestableCatadioptreExample.markers(instance);

		Assertions.assertThat(result).isNotNull().hasSize(1).containsEntry("any", 1.0);
	}

	@Test
	@DisplayName("should write the value of default property")
	void shouldWriteTheValueOfDefaultProperty() {
		Map<String, Double> defaultProperty = new HashMap<>();
		defaultProperty.put("any", 1.0);
		CatadioptreExample instance = new CatadioptreExample(defaultProperty, 1.0, Optional.empty());

		defaultProperty = new HashMap<>();
		defaultProperty.put("other", 2.0);
		CatadioptreExample result = TestableCatadioptreExample.markers(instance, defaultProperty);

		Assertions.assertThat(result).isSameAs(instance);
		Assertions.assertThat(TestableCatadioptreExample.markers(result)).isNotNull().hasSize(1)
				.containsEntry("other", 2.0);
	}

	@Test
	@DisplayName("should clear the value of default property")
	void shouldClearTheValueOfDefaultProperty() {
		Map<String, Double> defaultProperty = new HashMap<>();
		defaultProperty.put("any", 1.0);
		CatadioptreExample instance = new CatadioptreExample(defaultProperty, 1.0, Optional.empty());

		CatadioptreExample result = TestableCatadioptreExample.clearMarkers(instance);

		Assertions.assertThat(result).isSameAs(instance);
		Assertions.assertThat(TestableCatadioptreExample.markers(result)).isNull();
	}

	@Test
	@DisplayName("should write and read the value of inherited typed parameter as double")
	void shouldWriteAndReadTheValueOfInheritedTypedParameterAsDouble() {
		Map<String, Double> defaultProperty = new HashMap<>();
		defaultProperty.put("any", 1.0);
		CatadioptreExample instance = new CatadioptreExample(defaultProperty, 1.0, Optional.empty());

		CatadioptreExample result = TestableAbstractCatadioptreExample.typedProperty(instance, 2.0);

		Assertions.assertThat(result).isSameAs(instance);
		Assertions.assertThat(TestableAbstractCatadioptreExample.typedProperty(result)).isEqualTo(2.0);

	}

	@Test
	@DisplayName("should write and read the value of inherited typed parameter as string")
	void shouldWriteAndReadTheValueOfInheritedTypedParameterAsString() {
		Map<String, Double> defaultProperty = new HashMap<>();
		defaultProperty.put("any", 1.0);
		CatadioptreExample instance = new CatadioptreExample(defaultProperty, 1.0, Optional.empty());

		CatadioptreExample result = TestableAbstractCatadioptreExample.typedProperty2(instance,
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
		CatadioptreExample instance = new CatadioptreExample(defaultProperty, 1.0, Optional.empty());

		double result = TestableCatadioptreExample.multiplySum(instance, 2.0, new Double[]{1.0, 3.0, 6.0});

		Assertions.assertThat(result).isEqualTo(20.0);
	}

	@Test
	@DisplayName("should execute the function of parent without argument")
	void shouldExecuteTheFunctionOfParentWithoutArgument() {
		Map<String, Double> defaultProperty = new HashMap<>();
		defaultProperty.put("any", 1.0);
		CatadioptreExample instance = new CatadioptreExample(defaultProperty, 1.0, Optional.empty());

		int result = TestableAbstractCatadioptreExample.getAnything(instance);

		Assertions.assertThat(result).isEqualTo(123);
	}

	@Test
	@DisplayName("should execute the function of parent with variable arguments")
	void shouldExecuteTheFunctionOfParentWithVariableArguments() {
		Map<String, Double> defaultProperty = new HashMap<>();
		defaultProperty.put("any", 1.0);
		CatadioptreExample instance = new CatadioptreExample(defaultProperty, 1.0, Optional.empty());

		double result = TestableAbstractCatadioptreExample.divideSum(instance, 2.0, new Double[]{1.0, 3.0, 6.0});

		Assertions.assertThat(result).isEqualTo(5.0);
	}
}
