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

import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation to mark a Java private or protected method or field in order to generate the testing code at compilation
 * time.
 *
 * @author Eric Jessé
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(SOURCE)
public @interface Testable {

	/**
	 * When the annotated element is a property, generates the getter when set to true.
	 */
	boolean getter() default true;

	/**
	 * When the annotated element is a property, generates the setter when set to true.
	 */
	boolean setter() default true;

	/**
	 * When the annotated element is a property, generates the clear function when set to true and the type is
	 * nullable.
	 */
	boolean clearer() default true;

}
