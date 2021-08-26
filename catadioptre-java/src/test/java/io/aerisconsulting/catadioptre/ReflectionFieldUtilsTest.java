package io.aerisconsulting.catadioptre;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ReflectionFieldUtilsTest {

	@Test
	void shouldSetTheFieldValue() throws NoSuchFieldException, IllegalAccessException {
		// given
		int value = (int) (Math.random() * 10_000);
		final ReflectionUtilsObject object = new ReflectionUtilsObject();

		// when
		ReflectionUtilsObject result = ReflectionFieldUtils.setField(object, "value", value);

		//then
		Assertions.assertSame(object, result);
		Assertions.assertEquals(value, object.getValue());
	}

	@Test
	void shouldGetThePropertyValue() throws NoSuchFieldException, IllegalAccessException {
		// given
		final ReflectionUtilsObject object = new ReflectionUtilsObject();

		// when
		int result = ReflectionFieldUtils.getField(object, "value");

		//then
		Assertions.assertEquals(123, result);
	}

	@Test
	void shouldClearThePropertyValue() throws NoSuchFieldException, IllegalAccessException {
		// given
		final ReflectionUtilsObject object = new ReflectionUtilsObject();

		// when
		ReflectionUtilsObject result = ReflectionFieldUtils.clearField(object, "value");

		//then
		Assertions.assertSame(object, result);
		Assertions.assertNull(object.getValue());
	}

	@Test
	void shouldSetTheInheritedFieldValue() throws NoSuchFieldException, IllegalAccessException {
		// given
		int value = (int) (Math.random() * 10_000);
		final ReflectionUtilsObject object = new ReflectionUtilsObject();

		// when
		ReflectionUtilsObject result = ReflectionFieldUtils.setField(object, "inheritedValue", value);

		//then
		Assertions.assertSame(object, result);
		Assertions.assertEquals(value, object.getInheritedValue());
	}

	@Test
	void shouldGetTheInheritedPropertyValue() throws NoSuchFieldException, IllegalAccessException {
		// given
		final ReflectionUtilsObject object = new ReflectionUtilsObject();

		// when
		int result = ReflectionFieldUtils.getField(object, "inheritedValue");

		//then
		Assertions.assertEquals(789, result);
	}

	@Test
	void shouldClearTheInheritedPropertyValue() throws NoSuchFieldException, IllegalAccessException {
		// given
		final ReflectionUtilsObject object = new ReflectionUtilsObject();

		// when
		ReflectionUtilsObject result = ReflectionFieldUtils.clearField(object, "inheritedValue");

		//then
		Assertions.assertSame(object, result);
		Assertions.assertNull(object.getInheritedValue());
	}

}
