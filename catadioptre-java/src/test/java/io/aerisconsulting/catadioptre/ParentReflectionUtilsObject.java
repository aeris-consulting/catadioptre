package io.aerisconsulting.catadioptre;

import java.util.Arrays;
import java.util.Objects;

public class ParentReflectionUtilsObject {

	private final Integer inheritedValue;

	public ParentReflectionUtilsObject(final int inheritedValue) {
		this.inheritedValue = inheritedValue;
	}

	private Integer returnInheritedValue() {
		return inheritedValue;
	}

	protected Integer getInheritedValue() {
		return inheritedValue;
	}

	private double inheritedDivide(Number value, int divider) {
		return value.intValue() / divider;
	}

	private double inheritedDivideSum(int divider, Integer... values) {
		return Arrays.stream(values).filter(Objects::nonNull).mapToInt(i -> i).sum() / divider;
	}

	private double divideSum(double divider, Number... values) {
		return Arrays.stream(values).filter(Objects::nonNull).mapToDouble(i -> i.doubleValue()).sum() / divider;
	}
}
