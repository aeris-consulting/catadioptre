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

import java.lang.reflect.Array;

/**
 * Definition of an argument to pass to a method for reflective call.
 *
 * @author Eric Jessé
 */
public class Argument {

	private final Object value;

	private final Class<?> type;

	private Argument(final Object value, final Class<?> type) {
		this.value = value;
		this.type = type;
	}

	public static Argument ofNotNull(final Object value) {
		return new Argument(value, value.getClass());
	}

	public static Argument ofNull(final Class<?> type) {
		return new Argument(null, type);
	}

	public static <T> Argument ofVarargs(final Class<T> type, T... values) {
		final Object arguments = Array.newInstance(type, values.length);
		for (int i = 0; i < values.length; i++) {
			Array.set(arguments, i, values[i]);
		}
		return new Argument(arguments, arguments.getClass());
	}

	public Object getValue() {
		return value;
	}

	public Class<?> getType() {
		return type;
	}

	@Override
	public String toString() {
		return "Argument(value: " + value + ", type:" + type + ')';
	}
}

