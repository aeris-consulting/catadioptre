/*
 * Copyright 2021 AERIS-Consulting e.U.
 *
 * AERIS-Consulting e.U. licenses this file to you under the Apache License, version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.aerisconsulting.catadioptre;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Utils to execute invisible methods on instances using reflection.
 *
 * @author Eric Jessé
 */
public class ReflectionMethodUtils {

	private static final Map<Class<?>, Class<?>> PRIMITIVE_MAPPING = new HashMap<>();

	static {
		PRIMITIVE_MAPPING.put(Byte.TYPE, Byte.class);
		PRIMITIVE_MAPPING.put(Short.TYPE, Short.class);
		PRIMITIVE_MAPPING.put(Integer.TYPE, Integer.class);
		PRIMITIVE_MAPPING.put(Long.TYPE, Long.class);
		PRIMITIVE_MAPPING.put(Float.TYPE, Float.class);
		PRIMITIVE_MAPPING.put(Double.TYPE, Double.class);
		PRIMITIVE_MAPPING.put(Boolean.TYPE, Boolean.class);
		PRIMITIVE_MAPPING.put(Character.TYPE, Character.class);
	}

	/**
	 * This class only contains static methods.
	 */
	private ReflectionMethodUtils() {
	}

	/**
	 * Executes a method that cannot be accessible in the caller scope.
	 *
	 * @param instance the instance for the "this" of the executed method
	 * @param name the name of the method to execute
	 * @param value the arguments to pass to the method, which can be eiter
	 * @param <T> the type of the result
	 * @return the result of the execution of the method {@code name} on {@code instance}
	 */
	public static <T> T executeInvisible(Object instance, String name, Object... value) {
		final Argument[] argumentsDefinitions = new Argument[value.length];
		final ArrayList<Object> argumentsValues = new ArrayList<>();
		for (int i = 0; i < value.length; i++) {
			Object arg = value[i];
			if (arg instanceof Argument) {
				final Argument argument = (Argument) arg;
				argumentsDefinitions[i] = argument;
				argumentsValues.add(argument.getValue());
			} else if (arg != null) {
				argumentsDefinitions[i] = Argument.ofNotNull(arg);
				argumentsValues.add(arg);
			} else {
				throw new IllegalArgumentException("The argument " + i
						+ " is null and its type cannot be detected. Use Argument.ofNull() instead.");
			}
		}
		final Method method = findMethod(instance.getClass(), name, argumentsDefinitions);
		try {
			//noinspection unchecked
			return (T) method.invoke(instance, argumentsValues.toArray());
		} catch (InvocationTargetException | IllegalAccessException e) {
			throw new CatadioptreException(e);
		}
	}

	/**
	 * Searches the declared method with the provided signature on the class or one of its ancestors.
	 */
	private static Method findMethod(Class<?> instanceClass, String name, Argument[] argumentDefinitions) {
		final Optional<Method> method = Arrays.stream(instanceClass.getDeclaredMethods())
				.filter(m -> m.getName().equals(name)
						&& areArgumentsAssignable(m.getParameterTypes(), argumentDefinitions)
				)
				.findFirst();
		if (method.isPresent()) {
			method.get().setAccessible(true);
			return method.get();
		}
		if (!instanceClass.getSuperclass().equals(Object.class)) {
			return findMethod(instanceClass.getSuperclass(), name, argumentDefinitions);
		}
		throw new CatadioptreException(new NoSuchMethodException("Method " + name + " with arguments "
				+ Arrays.stream(argumentDefinitions).map(Argument::toString).collect(Collectors.joining(","))
				+ " was not found"));
	}

	/**
	 * Determines if each parameter type of a method, is either the class or a superclass or superinterface of the type
	 * of the argument with the same index.
	 */
	private static boolean areArgumentsAssignable(Class<?>[] parameterTypes, Argument[] argumentDefinitions) {
		if (parameterTypes.length != argumentDefinitions.length) {
			return false;
		}
		for (int i = 0; i < parameterTypes.length; i++) {
			if (!isAssignable(parameterTypes[i], argumentDefinitions[i])) {
				return false;
			}
		}
		return true;
	}

	private static boolean isAssignable(Class<?> parameterType, Argument argumentDefinition) {
		// Primitive types are converted to the boxing types.
		final Class<?> actualParameterType =
				parameterType.isPrimitive() ? PRIMITIVE_MAPPING.get(parameterType) : parameterType;
		// If the argument is variable, we build an array to extract the type from it.
		final Class<?> actualArgumentType =
				argumentDefinition.getType().isPrimitive() ? PRIMITIVE_MAPPING.get(argumentDefinition.getType())
						: argumentDefinition.getType();

		return actualParameterType.isAssignableFrom(actualArgumentType);
	}
}
