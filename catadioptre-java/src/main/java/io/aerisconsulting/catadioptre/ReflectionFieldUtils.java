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

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

/**
 * Utils to access to invisible fields on instances using reflection.
 *
 * @author Eric Jessé
 */
public class ReflectionFieldUtils {

	/**
	 * This class only contains static methods.
	 */
	private ReflectionFieldUtils() {
	}

	/**
	 * Sets {@code value} in the field called {@code name} on {@code instance}.
	 * <p>
	 * Usage: {@code ReflectionFieldUtils.setField(myInstance, " value ", 456) }
	 *
	 * @param instance the instance owning the field
	 * @param name the name of the field
	 * @param value the value to set on the field
	 * @param <T> the type of the instance
	 * @return the instance in order to chain the calls
	 */
	public static <T> T setField(T instance, String name, Object value) {
		final Field field = findField(instance.getClass(), name);
		try {
			field.set(instance, value);
		} catch (IllegalAccessException e) {
			throw new CatadioptreException(e);
		}
		return instance;
	}

	/**
	 * Reads the value from the field called {@code name} on {@code instance}.
	 * <p>
	 * Usage: {@code int value = ReflectionFieldUtils.getField<Integer></>(myInstance, "value") }
	 */
	public static <R> R getField(Object instance, String name) {
		try {
			//noinspection unchecked
			return (R) findField(instance.getClass(), name).get(instance);
		} catch (IllegalAccessException e) {
			throw new CatadioptreException(e);
		}
	}

	/**
	 * Sets the field called {@code name} to null on {@code instance}.
	 * <p>
	 * Usage: {@code ReflectionFieldUtils.clearField(myInstance, "value") }
	 */
	public static <T> T clearField(T instance, String name) {
		setField(instance, name, null);
		return instance;
	}

	/**
	 * Searches the declared field with the provided name on the class or one of its ancestors.
	 */
	private static Field findField(Class<?> instanceClass, String name) {
		final Optional<Field> field = Arrays.stream(instanceClass.getDeclaredFields())
				.filter(f -> f.getName().equals(name))
				.findFirst();
		if (field.isPresent()) {
			field.get().setAccessible(true);
			return field.get();
		}
		if (!instanceClass.getSuperclass().equals(Object.class)) {
			return findField(instanceClass.getSuperclass(), name);
		}
		throw new CatadioptreException(new NoSuchFieldException("Field " + name + " was not found"));
	}
}
