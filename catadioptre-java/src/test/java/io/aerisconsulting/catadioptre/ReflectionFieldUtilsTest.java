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

    @Test
    void shouldThrowExceptionWhenFieldDoesNotExist() {
        // given
        final ReflectionUtilsObject object = new ReflectionUtilsObject();

        // when & then
        CatadioptreException exception = Assertions.assertThrows(CatadioptreException.class, () ->
                ReflectionFieldUtils.getField(object, "nonExistentField")
        );
        Assertions.assertNotNull(exception.getCause());
        Assertions.assertTrue(exception.getCause() instanceof NoSuchFieldException);
        Assertions.assertTrue(exception.getCause().getMessage().contains("nonExistentField"));
    }

    @Test
    void shouldThrowExceptionWhenSettingNonExistentField() {
        // given
        final ReflectionUtilsObject object = new ReflectionUtilsObject();

        // when & then
        CatadioptreException exception = Assertions.assertThrows(CatadioptreException.class, () ->
                ReflectionFieldUtils.setField(object, "nonExistentField", 123)
        );
        Assertions.assertNotNull(exception.getCause());
        Assertions.assertTrue(exception.getCause() instanceof NoSuchFieldException);
    }

    @Test
    void shouldThrowExceptionWhenClearingNonExistentField() {
        // given
        final ReflectionUtilsObject object = new ReflectionUtilsObject();

        // when & then
        CatadioptreException exception = Assertions.assertThrows(CatadioptreException.class, () ->
                ReflectionFieldUtils.clearField(object, "nonExistentField")
        );
        Assertions.assertNotNull(exception.getCause());
        Assertions.assertTrue(exception.getCause() instanceof NoSuchFieldException);
    }

    @Test
    void shouldAllowMethodChainingForSetField() {
        // given
        final ReflectionUtilsObject object = new ReflectionUtilsObject();

        // when
        ReflectionUtilsObject result = ReflectionFieldUtils.setField(
                ReflectionFieldUtils.setField(object, "value", 100),
                "inheritedValue", 200);

        // then
        Assertions.assertSame(object, result);
        Assertions.assertEquals(100, object.getValue());
        Assertions.assertEquals(200, object.getInheritedValue());
    }

    @Test
    void shouldAllowMethodChainingForClearField() {
        // given
        final ReflectionUtilsObject object = new ReflectionUtilsObject();

        // when
        ReflectionUtilsObject result = ReflectionFieldUtils.clearField(
                ReflectionFieldUtils.clearField(object, "value"),
                "inheritedValue");

        // then
        Assertions.assertSame(object, result);
        Assertions.assertNull(object.getValue());
        Assertions.assertNull(object.getInheritedValue());
    }

    @Test
    void shouldSetFieldToNullValue() {
        // given
        final ReflectionUtilsObject object = new ReflectionUtilsObject();

        // when
        ReflectionFieldUtils.setField(object, "value", null);

        // then
        Assertions.assertNull(object.getValue());
    }

    @Test
    void shouldGetNullFieldValue() {
        // given
        final ReflectionUtilsObject object = new ReflectionUtilsObject();
        ReflectionFieldUtils.setField(object, "value", null);

        // when
        Integer result = ReflectionFieldUtils.getField(object, "value");

        // then
        Assertions.assertNull(result);
    }

    @Test
    void shouldSetAndGetDifferentFieldTypes() {
        // given
        final ReflectionUtilsObject object = new ReflectionUtilsObject();
        final int expectedInt = 999;
        final Integer expectedInheritedInt = 888;

        // when
        ReflectionFieldUtils.setField(object, "value", expectedInt);
        ReflectionFieldUtils.setField(object, "inheritedValue", expectedInheritedInt);

        // then
        Integer resultValue = ReflectionFieldUtils.getField(object, "value");
        Integer resultInheritedValue = ReflectionFieldUtils.getField(object, "inheritedValue");
        Assertions.assertEquals(expectedInt, resultValue);
        Assertions.assertEquals(expectedInheritedInt, resultInheritedValue);
    }

    @Test
    void shouldPreserveInstanceReferenceAfterClear() {
        // given
        final ReflectionUtilsObject object = new ReflectionUtilsObject();
        final ReflectionUtilsObject expected = object;

        // when
        ReflectionUtilsObject result = ReflectionFieldUtils.clearField(object, "value");

        // then
        Assertions.assertSame(expected, result);
    }

    @Test
    void shouldPreserveInstanceReferenceAfterSet() {
        // given
        final ReflectionUtilsObject object = new ReflectionUtilsObject();
        final ReflectionUtilsObject expected = object;

        // when
        ReflectionUtilsObject result = ReflectionFieldUtils.setField(object, "value", 456);

        // then
        Assertions.assertSame(expected, result);
    }

}
