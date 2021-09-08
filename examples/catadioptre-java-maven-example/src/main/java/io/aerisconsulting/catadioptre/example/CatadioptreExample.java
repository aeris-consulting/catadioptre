package io.aerisconsulting.catadioptre.example;

import io.aerisconsulting.catadioptre.Testable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class CatadioptreExample extends AbstractCatadioptreExample<Double, Optional<String>> {

	@Testable
	private final Map<String, Double> markers;

	public CatadioptreExample(final Map<String, Double> markers, final Double typedProperty,
			final Optional<String> typedProperty2) {
		super(typedProperty, typedProperty2);
		this.markers = markers;
	}

	@Testable
	private List<DefaultCatadioptreExample> callMethodWithLowVisibilityReturnType() {
		return Collections.singletonList(new DefaultCatadioptreExample());
	}

	@Testable
	private Double multiplySum(double multiplier, Double... valuesToSum) {
		return Arrays.stream(valuesToSum).filter(Objects::nonNull).mapToDouble(d -> d).sum() * multiplier;
	}
}
