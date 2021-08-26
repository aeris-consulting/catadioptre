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
package io.aerisconsulting.catadioptre.java;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;

/**
 * Utils to specify elements for the generation of the proxy methods.
 *
 * @author Eric Jessé
 */
class JavaSpecificationUtils {

	/**
	 * Generates a {@link TypeName} representing the provided {@link TypeElement}.
	 */
	public TypeName createTypeName(final TypeElement element) {
		final ClassName result = ClassName.get(element);
		if (element.getTypeParameters().isEmpty()) {
			return result;
		} else {
			final TypeName[] types = new TypeName[element.getTypeParameters().size()];
			for (int i = 0; i < element.getTypeParameters().size(); i++) {
				types[i] = createTypeName(element.getTypeParameters().get(i));
			}
			return ParameterizedTypeName.get(result, types);
		}
	}

	private TypeName createTypeName(final TypeParameterElement element) {
		final TypeName[] bounds = new TypeName[element.getBounds().size()];
		for (int i = 0; i < element.getBounds().size(); i++) {
			bounds[i] = createTypeName(element.getBounds().get(i));
		}
		return TypeVariableName.get(element.getSimpleName().toString(), bounds);
	}

	private TypeName createTypeName(final TypeMirror type) {
		return TypeName.get(type);
	}
}
