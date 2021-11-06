package io.aerisconsulting.catadioptre.java;

import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;

/**
 * Service in charge of verifying the visibility of the elements used by methods and fields in order to verify their
 * accessibility in the class declaring the proxies.
 *
 * @author Eric Jessé
 */
class JavaVisibilityUtils {

	/**
	 * Default private constructor, the class only has static methods.
	 */
	private JavaVisibilityUtils() {
	}

	/**
	 * Determines whether all the types in the context of the {@link ExecutableElement} can be accessible from a public
	 * method of the module.
	 *
	 * @param methodElement the element to inspect
	 */
	static boolean canBePublic(final ExecutableElement methodElement) {
		final Set<Modifier> collectedModifiers = new HashSet<>();
		collectVisibilities(methodElement, collectedModifiers);
		return !collectedModifiers.contains(Modifier.PRIVATE);
	}

	/**
	 * Determines whether all the types in the context of the {@link VariableElement} can be accessible from a public
	 * method of the module.
	 *
	 * @param variableElement the element to inspect
	 */
	static boolean canBePublic(final VariableElement variableElement) {
		final Set<Modifier> collectedModifiers = new HashSet<>();
		collectVisibilities(variableElement.asType(), collectedModifiers);
		return !collectedModifiers.contains(Modifier.PRIVATE);
	}

	/**
	 * Collects all the private and public visibilities of the {@link ExecutableElement}.
	 *
	 * @param methodElement the element to inspect
	 * @param collectedModifiers the set containing the collected modifiers
	 */
	private static void collectVisibilities(final ExecutableElement methodElement,
			final Set<Modifier> collectedModifiers) {
		if (methodElement.getEnclosingElement().getModifiers().contains(Modifier.PRIVATE)) {
			// If the class is private, we cannot generate proxy for its methods.
			collectedModifiers.add(Modifier.PRIVATE);
		} else {
			collectVisibilities(methodElement.getReturnType(), collectedModifiers);
			for (final VariableElement parameter : methodElement.getParameters()) {
				collectVisibilities(parameter.asType(), collectedModifiers);
			}
		}
	}

	/**
	 * Collects all the private and public visibilities of the {@link TypeMirror}.
	 *
	 * @param type the type to inspect
	 * @param collectedModifiers the set containing the collected modifiers
	 */
	private static void collectVisibilities(final TypeMirror type, final Set<Modifier> collectedModifiers) {
		if (type instanceof PrimitiveType) {
			collectedModifiers.add(Modifier.PUBLIC);
		} else if (type instanceof ArrayType) {
			collectVisibilities(((ArrayType) type).getComponentType(), collectedModifiers);
		} else if (type instanceof DeclaredType) {
			collectVisibilities((DeclaredType) type, collectedModifiers);
		} else if (type instanceof TypeVariable) {
			collectVisibilities(((TypeVariable) type).getUpperBound(), collectedModifiers);
		} else if (type instanceof WildcardType) {
			final WildcardType wildcardType = (WildcardType) type;
			if (wildcardType.getExtendsBound() != null) {
				collectVisibilities(wildcardType.getExtendsBound(), collectedModifiers);
			}
		} else if (!(type instanceof NoType)) {
			throw new IllegalArgumentException("Not supported type: " + type + " being a " + type.getClass());
		}
	}

	/**
	 * Collects all the private and public visibilities of the {@link DeclaredType} and its type arguments.
	 *
	 * @param type the type to inspect
	 * @param collectedModifiers the set containing the collected modifiers
	 */
	private static void collectVisibilities(final DeclaredType type, final Set<Modifier> collectedModifiers) {
		final Element element = type.asElement();
		if (element.getModifiers().contains(Modifier.PRIVATE)) {
			collectedModifiers.add(Modifier.PRIVATE);
		} else {
			collectedModifiers.add(Modifier.PUBLIC);
		}
		for (final TypeMirror typeArgument : type.getTypeArguments()) {
			collectVisibilities(typeArgument, collectedModifiers);
		}
	}

}
