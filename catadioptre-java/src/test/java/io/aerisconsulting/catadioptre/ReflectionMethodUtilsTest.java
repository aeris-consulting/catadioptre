package io.aerisconsulting.catadioptre;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

class ReflectionMethodUtilsTest {

	@Test
	void shouldExecuteAPrivateMethodWithoutArgument()
			throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
		// given
		final ReflectionUtilsObject object = new ReflectionUtilsObject();

		// when
		int result = ReflectionMethodUtils.executeInvisible(object, "returnValue");

		// then
		Assertions.assertEquals(123, result);
	}

	@Test
	void shouldExecuteAPrivateMethodWithArguments()
			throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
		// given
		final ReflectionUtilsObject object = new ReflectionUtilsObject();

		// when
		double result = ReflectionMethodUtils.executeInvisible(object, "divide", 10, 2);

		// then
		Assertions.assertEquals(5.0, result);
	}

	@Test
	void shouldExecuteAPrivateMethodWithVariableArguments()
			throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
		// given
		final ReflectionUtilsObject object = new ReflectionUtilsObject();

		// when
		double result = ReflectionMethodUtils.executeInvisible(object, "divideSum", 2,
				Argument.ofVarargs(Integer.class, 1, 3, 6));

		// then
		Assertions.assertEquals(5.0, result);
	}

	@Test
	void shouldExecuteAPrivateMethodWithCloseSignature()
			throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
		// given
		final ReflectionUtilsObject object = new ReflectionUtilsObject();
		final List<Integer> values = new ArrayList<>();
		values.add(1);
		values.add(3);
		values.add(6);

		// when
		double result = ReflectionMethodUtils.executeInvisible(object, "divideSum", 2, values);

		// then
		Assertions.assertEquals(5.0, result);
	}

	@Test
	void shouldExecuteAPrivateMethodWithCloseSignatureFromParentWithVariableArguments()
			throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
		// given
		final ReflectionUtilsObject object = new ReflectionUtilsObject();

		// when
		double result = ReflectionMethodUtils.executeInvisible(object, "divideSum", 2.5,
				Argument.ofVarargs(Number.class, 1, 3, 6));

		// then
		Assertions.assertEquals(4.0, result);
	}

	@Test
	void shouldExecuteAnInheritedMethodWithoutArgument()
			throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
		// given
		final ReflectionUtilsObject object = new ReflectionUtilsObject();

		// when
		int result = ReflectionMethodUtils.executeInvisible(object, "returnInheritedValue");

		// then
		Assertions.assertEquals(789, result);
	}

	@Test
	void shouldExecuteAInheritedMethodWithArguments()
			throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
		// given
		final ReflectionUtilsObject object = new ReflectionUtilsObject();

		// when
		double result = ReflectionMethodUtils.executeInvisible(object, "inheritedDivide", 10, 2);

		// then
		Assertions.assertEquals(5.0, result);
	}

	@Test
	void shouldExecuteAInheritedMethodWithVariableArguments()
			throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
		// given
		final ReflectionUtilsObject object = new ReflectionUtilsObject();

		// when
		double result = ReflectionMethodUtils.executeInvisible(object, "inheritedDivideSum", 2,
				Argument.ofVarargs(Integer.class, 1, 3, 6));

		// then
		Assertions.assertEquals(5.0, result);
	}

	@Test
	void shouldThrowOriginalCauseOfException()
			throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
		// given
		final ReflectionUtilsObject object = new ReflectionUtilsObject();

		// when
		Throwable cause = Assertions.assertThrows(CatadioptreOriginalCauseException.class, () ->
				ReflectionMethodUtils.executeInvisible(object, "throwException")
		);

		Assertions.assertEquals(IllegalArgumentException.class, cause.getCause().getClass());
		Assertions.assertEquals("This is the exception", cause.getCause().getMessage());
	}

	@Test
	void shouldExecuteMethodWhenTheArgumentIsAMockOfAnAbstractClassAndTwoCandidateMethods() {
		// given
		final ReflectionUtilsObject object = new ReflectionUtilsObject();
		final AbstractWrapper mock = Mockito.mock(AbstractWrapper.class);
		Mockito.when(mock.getValue()).thenReturn("the value");

		// when
		String result = ReflectionMethodUtils.executeInvisible(object, "extractValue", mock);

		// then
		Assertions.assertEquals("the value", result);
	}

    @Test
    void shouldThrowIllegalArgumentExceptionWhenNullArgumentPassedWithoutWrapper() {
        // given
        final ReflectionUtilsObject object = new ReflectionUtilsObject();

        // when & then
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () ->
                ReflectionMethodUtils.executeInvisible(object, "divide", null, 2)
        );
        Assertions.assertTrue(exception.getMessage().contains("argument"));
        Assertions.assertTrue(exception.getMessage().contains("null"));
        Assertions.assertTrue(exception.getMessage().contains("Argument.ofNull()"));
    }

    @Test
    void shouldExecuteMethodWithNullArgumentWhenWrappedInArgument() {
        // given
        final ReflectionUtilsObject object = new ReflectionUtilsObject();

        // when & then - calling with null will cause NullPointerException in the method
        Assertions.assertThrows(CatadioptreOriginalCauseException.class, () ->
                ReflectionMethodUtils.executeInvisible(object, "divide",
                        Argument.ofNull(Number.class), 2)
        );
    }

    @Test
    void shouldThrowExceptionWhenMethodNotFound() {
        // given
        final ReflectionUtilsObject object = new ReflectionUtilsObject();

        // when & then
        CatadioptreException exception = Assertions.assertThrows(CatadioptreException.class, () ->
                ReflectionMethodUtils.executeInvisible(object, "nonExistentMethod")
        );
        Assertions.assertNotNull(exception.getCause());
        Assertions.assertTrue(exception.getCause() instanceof NoSuchMethodException);
        Assertions.assertTrue(exception.getCause().getMessage().contains("nonExistentMethod"));
    }

    @Test
    void shouldThrowExceptionWhenMethodNotFoundWithWrongSignature() {
        // given
        final ReflectionUtilsObject object = new ReflectionUtilsObject();

        // when & then
        CatadioptreException exception = Assertions.assertThrows(CatadioptreException.class, () ->
                ReflectionMethodUtils.executeInvisible(object, "divide", "wrong", "types")
        );
        Assertions.assertNotNull(exception.getCause());
        Assertions.assertTrue(exception.getCause() instanceof NoSuchMethodException);
    }

    @Test
    void shouldExecuteMethodWithNoArguments() {
        // given
        final ReflectionUtilsObject object = new ReflectionUtilsObject();

        // when
        int result = ReflectionMethodUtils.executeInvisible(object, "returnValue");

        // then
        Assertions.assertEquals(123, result);
    }

    @Test
    void shouldExecuteMethodWithMixedArgumentTypes() {
        // given
        final ReflectionUtilsObject object = new ReflectionUtilsObject();

        // when
        double result = ReflectionMethodUtils.executeInvisible(object, "divide", 100, 10);

        // then
        Assertions.assertEquals(10.0, result);
    }

    @Test
    void shouldHandlePrimitiveTypeArguments() {
        // given
        final ReflectionUtilsObject object = new ReflectionUtilsObject();
        int primitiveInt = 20;
        int primitiveDivider = 4;

        // when
        double result = ReflectionMethodUtils.executeInvisible(object, "divide", primitiveInt, primitiveDivider);

        // then
        Assertions.assertEquals(5.0, result);
    }

    @Test
    void shouldHandleBoxedPrimitiveTypeArguments() {
        // given
        final ReflectionUtilsObject object = new ReflectionUtilsObject();
        Integer boxedInt = Integer.valueOf(30);
        Integer boxedDivider = Integer.valueOf(6);

        // when
        double result = ReflectionMethodUtils.executeInvisible(object, "divide", boxedInt, boxedDivider);

        // then
        Assertions.assertEquals(5.0, result);
    }

    @Test
    void shouldExecuteMethodThatThrowsException() {
        // given
        final ReflectionUtilsObject object = new ReflectionUtilsObject();

        // when & then - method throws exception which is wrapped
        Assertions.assertThrows(CatadioptreOriginalCauseException.class, () ->
                ReflectionMethodUtils.executeInvisible(object, "throwException")
        );
    }

    @Test
    void shouldFindMethodInSuperclassHierarchy() {
        // given
        final ReflectionUtilsObject object = new ReflectionUtilsObject();

        // when
        int result = ReflectionMethodUtils.executeInvisible(object, "returnInheritedValue");

        // then
        Assertions.assertEquals(789, result);
    }

    @Test
    void shouldExecuteInheritedMethodWithVariableArguments() {
        // given
        final ReflectionUtilsObject object = new ReflectionUtilsObject();

        // when
        double result = ReflectionMethodUtils.executeInvisible(object, "inheritedDivideSum", 3,
                Argument.ofVarargs(Integer.class, 3, 6, 9));

        // then
        Assertions.assertEquals(6.0, result);
    }

    @Test
    void shouldSelectCorrectOverloadedMethod() {
        // given
        final ReflectionUtilsObject object = new ReflectionUtilsObject();

        // when - call with List parameter
        java.util.List<Integer> values = java.util.Arrays.asList(2, 4, 6);
        double resultList = ReflectionMethodUtils.executeInvisible(object, "divideSum", 2, values);

        // then
        Assertions.assertEquals(6.0, resultList);
    }

    @Test
    void shouldHandleEmptyVarargsArray() {
        // given
        final ReflectionUtilsObject object = new ReflectionUtilsObject();

        // when
        double result = ReflectionMethodUtils.executeInvisible(object, "divideSum", 1,
                Argument.ofVarargs(Integer.class));

        // then
        Assertions.assertEquals(0.0, result);
    }

    @Test
    void shouldWrapInvocationTargetExceptionAsCatadioptreOriginalCauseException() {
        // given
        final ReflectionUtilsObject object = new ReflectionUtilsObject();

        // when
        CatadioptreOriginalCauseException exception = Assertions.assertThrows(
                CatadioptreOriginalCauseException.class, () ->
                        ReflectionMethodUtils.executeInvisible(object, "throwException")
        );

        // then
        Assertions.assertNotNull(exception.getCause());
        Assertions.assertEquals(IllegalArgumentException.class, exception.getCause().getClass());
        Assertions.assertEquals("This is the exception", exception.getCause().getMessage());
    }

    @Test
    void shouldExecuteMethodWithSuperclassParameterType() {
        // given
        final ReflectionUtilsObject object = new ReflectionUtilsObject();
        Integer integerValue = 50;

        // when - Integer is a Number, so it should match divide(Number, int)
        double result = ReflectionMethodUtils.executeInvisible(object, "divide", integerValue, 5);

        // then
        Assertions.assertEquals(10.0, result);
    }
}
