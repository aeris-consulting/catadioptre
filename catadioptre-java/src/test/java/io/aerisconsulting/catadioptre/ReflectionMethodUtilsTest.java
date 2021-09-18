package io.aerisconsulting.catadioptre;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
	void shouldThrowOriginalCauseOfException() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
		// given
		final ReflectionUtilsObject object = new ReflectionUtilsObject();

		// when
		Throwable cause = Assertions.assertThrows(CatadioptreOriginalCauseException.class, () ->
				ReflectionMethodUtils.executeInvisible(object, "throwException")
		);

		Assertions.assertEquals(RuntimeException.class, cause.getCause().getClass());
	}
}
