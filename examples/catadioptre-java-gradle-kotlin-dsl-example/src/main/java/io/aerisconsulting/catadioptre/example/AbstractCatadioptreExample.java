package io.aerisconsulting.catadioptre.example;

import io.aerisconsulting.catadioptre.Testable;
import java.util.Arrays;
import java.util.Objects;

abstract class AbstractCatadioptreExample<T extends Number, V> implements CatadioptreInterface<T> {

	@Testable
	private T typedProperty = null;

	@Testable
	private V typedProperty2 = null;

	protected AbstractCatadioptreExample(final T typedProperty, final V defaultArgumentTypedProperty2) {
		this.typedProperty = typedProperty;
		this.typedProperty2 = defaultArgumentTypedProperty2;
	}

	@Testable
	private Double divideSum(double divider, Double... valuesToSum) {
		return Arrays.stream(valuesToSum).filter(Objects::nonNull).mapToDouble(d -> d).sum() / divider;
	}

	@Testable
	private int getAnything() {
		return 123;
	}
}
