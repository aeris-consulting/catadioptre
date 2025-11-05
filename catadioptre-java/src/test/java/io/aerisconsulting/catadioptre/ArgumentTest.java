package io.aerisconsulting.catadioptre;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive tests for the Argument class covering all factory methods and edge cases.
 */
class ArgumentTest {

    @Test
    void ofNotNull_shouldCreateArgumentWithInferredType() {
        // given
        String value = "test value";

        // when
        Argument result = Argument.ofNotNull(value);

        // then
        Assertions.assertEquals(value, result.getValue());
        Assertions.assertEquals(String.class, result.getType());
    }

    @Test
    void ofNotNull_shouldInferTypeFromInteger() {
        // given
        Integer value = 42;

        // when
        Argument result = Argument.ofNotNull(value);

        // then
        Assertions.assertEquals(value, result.getValue());
        Assertions.assertEquals(Integer.class, result.getType());
    }

    @Test
    void ofNotNull_shouldInferTypeFromCustomObject() {
        // given
        ReflectionUtilsObject value = new ReflectionUtilsObject();

        // when
        Argument result = Argument.ofNotNull(value);

        // then
        Assertions.assertEquals(value, result.getValue());
        Assertions.assertEquals(ReflectionUtilsObject.class, result.getType());
    }

    @Test
    void ofNotNull_shouldThrowNullPointerExceptionForNullValue() {
        // when & then
        Assertions.assertThrows(NullPointerException.class, () -> Argument.ofNotNull(null));
    }

    @Test
    void ofNull_shouldCreateArgumentWithNullValueAndSpecifiedType() {
        // when
        Argument result = Argument.ofNull(String.class);

        // then
        Assertions.assertNull(result.getValue());
        Assertions.assertEquals(String.class, result.getType());
    }

    @Test
    void ofNull_shouldAllowPrimitiveWrapperTypes() {
        // when
        Argument result = Argument.ofNull(Integer.class);

        // then
        Assertions.assertNull(result.getValue());
        Assertions.assertEquals(Integer.class, result.getType());
    }

    @Test
    void ofNull_shouldAllowCustomTypes() {
        // when
        Argument result = Argument.ofNull(ReflectionUtilsObject.class);

        // then
        Assertions.assertNull(result.getValue());
        Assertions.assertEquals(ReflectionUtilsObject.class, result.getType());
    }

    @Test
    void ofVarargs_shouldCreateArrayArgumentWithNoElements() {
        // when
        Argument result = Argument.ofVarargs(String.class);

        // then
        Assertions.assertNotNull(result.getValue());
        Assertions.assertTrue(result.getValue().getClass().isArray());
        Assertions.assertEquals(String[].class, result.getType());
        String[] array = (String[]) result.getValue();
        Assertions.assertEquals(0, array.length);
    }

    @Test
    void ofVarargs_shouldCreateArrayArgumentWithSingleElement() {
        // when
        Argument result = Argument.ofVarargs(String.class, "element1");

        // then
        Assertions.assertNotNull(result.getValue());
        Assertions.assertEquals(String[].class, result.getType());
        String[] array = (String[]) result.getValue();
        Assertions.assertEquals(1, array.length);
        Assertions.assertEquals("element1", array[0]);
    }

    @Test
    void ofVarargs_shouldCreateArrayArgumentWithMultipleElements() {
        // when
        Argument result = Argument.ofVarargs(Integer.class, 1, 2, 3, 4, 5);

        // then
        Assertions.assertNotNull(result.getValue());
        Assertions.assertEquals(Integer[].class, result.getType());
        Integer[] array = (Integer[]) result.getValue();
        Assertions.assertEquals(5, array.length);
        Assertions.assertEquals(1, array[0]);
        Assertions.assertEquals(5, array[4]);
    }

    @Test
    void ofVarargs_shouldHandleNullElementsInVarargs() {
        // when
        Argument result = Argument.ofVarargs(String.class, "first", null, "third");

        // then
        String[] array = (String[]) result.getValue();
        Assertions.assertEquals(3, array.length);
        Assertions.assertEquals("first", array[0]);
        Assertions.assertNull(array[1]);
        Assertions.assertEquals("third", array[2]);
    }

    @Test
    void ofVarargs_shouldWorkWithPrimitiveWrapperTypes() {
        // when
        Argument result = Argument.ofVarargs(Double.class, 1.5, 2.5, 3.5);

        // then
        Assertions.assertEquals(Double[].class, result.getType());
        Double[] array = (Double[]) result.getValue();
        Assertions.assertEquals(3, array.length);
        Assertions.assertEquals(1.5, array[0]);
    }

    @Test
    void ofVarargs_shouldWorkWithCustomObjectTypes() {
        // given
        ReflectionUtilsObject obj1 = new ReflectionUtilsObject();
        ReflectionUtilsObject obj2 = new ReflectionUtilsObject();

        // when
        Argument result = Argument.ofVarargs(ReflectionUtilsObject.class, obj1, obj2);

        // then
        Assertions.assertEquals(ReflectionUtilsObject[].class, result.getType());
        ReflectionUtilsObject[] array = (ReflectionUtilsObject[]) result.getValue();
        Assertions.assertEquals(2, array.length);
        Assertions.assertSame(obj1, array[0]);
        Assertions.assertSame(obj2, array[1]);
    }

    @Test
    void toString_shouldProvideReadableRepresentationForNotNull() {
        // given
        Argument argument = Argument.ofNotNull("test");

        // when
        String result = argument.toString();

        // then
        Assertions.assertTrue(result.contains("test"));
        Assertions.assertTrue(result.contains("String"));
    }

    @Test
    void toString_shouldProvideReadableRepresentationForNull() {
        // given
        Argument argument = Argument.ofNull(Integer.class);

        // when
        String result = argument.toString();

        // then
        Assertions.assertTrue(result.contains("null"));
        Assertions.assertTrue(result.contains("Integer"));
    }

    @Test
    void toString_shouldProvideReadableRepresentationForVarargs() {
        // given
        Argument argument = Argument.ofVarargs(String.class, "a", "b");

        // when
        String result = argument.toString();

        // then
        Assertions.assertTrue(result.contains("[Ljava.lang.String"));
    }

    @Test
    void getValue_shouldReturnCorrectValue() {
        // given
        String expected = "value";
        Argument argument = Argument.ofNotNull(expected);

        // when
        Object result = argument.getValue();

        // then
        Assertions.assertEquals(expected, result);
    }

    @Test
    void getType_shouldReturnCorrectType() {
        // given
        Argument argument = Argument.ofNotNull("value");

        // when
        Class<?> result = argument.getType();

        // then
        Assertions.assertEquals(String.class, result);
    }

    @Test
    void ofVarargs_shouldCreateIndependentArrays() {
        // given
        Argument arg1 = Argument.ofVarargs(Integer.class, 1, 2, 3);
        Argument arg2 = Argument.ofVarargs(Integer.class, 4, 5, 6);

        // when
        Integer[] array1 = (Integer[]) arg1.getValue();
        Integer[] array2 = (Integer[]) arg2.getValue();

        // then
        Assertions.assertNotSame(array1, array2);
        Assertions.assertEquals(1, array1[0]);
        Assertions.assertEquals(4, array2[0]);
    }
}