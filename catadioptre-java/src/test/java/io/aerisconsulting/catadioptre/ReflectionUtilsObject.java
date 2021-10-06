package io.aerisconsulting.catadioptre;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ReflectionUtilsObject extends ParentReflectionUtilsObject {

	private final Integer value = 123;

	public ReflectionUtilsObject() {
		super(789);
	}

	public Integer getValue() {
		return value;
	}

	private Integer returnValue() {
		return value;
	}

	private double divide(Number value, int divider) {
		return value.intValue() / divider;
	}

	private double divideSum(int divider, Integer... values) {
		return Arrays.stream(values).filter(Objects::nonNull).mapToInt(i -> i).sum() / divider;
	}

	private double divideSum(int divider, List<Integer> values) {
		return values.stream().filter(Objects::nonNull).mapToInt(i -> i).sum() / divider;
	}

	private void throwException() {
		throw new RuntimeException("");
	}
}
