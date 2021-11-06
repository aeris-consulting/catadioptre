package io.aerisconsulting.catadioptre.kotlin

import com.squareup.kotlinpoet.DelicateKotlinPoetApi
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.isInternal
import com.squareup.kotlinpoet.metadata.isPublic
import com.squareup.kotlinpoet.metadata.toImmutableKmClass
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.ArrayType
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.NoType
import javax.lang.model.type.PrimitiveType
import javax.lang.model.type.TypeMirror
import javax.lang.model.type.TypeVariable
import javax.lang.model.type.WildcardType
import javax.lang.model.util.Types

@DelicateKotlinPoetApi("Awareness of delicate aspect")
@KotlinPoetMetadataPreview
internal class KotlinVisibilityUtils(
    private val typeUtils: Types
) {

    /**
     * Determines whether all the types in the context of the [ExecutableElement] can be accessible from a public
     * method of the module.
     *
     * @param methodElement the element to inspect
     */
    fun detectLowestVisibility(methodElement: ExecutableElement): KModifier {
        val collectedModifiers: MutableSet<KClassVisibility> = HashSet()
        collectVisibilities(methodElement, collectedModifiers)
        return collectedModifiers.minByOrNull { it.ordinal }?.klassModifiers?.first() ?: KModifier.PUBLIC
    }

    /**
     * Collects all the private, internal and public visibilities of the [ExecutableElement].
     *
     * @param methodElement the element to inspect
     * @param collectedModifiers the set containing the collected modifiers
     */
    private fun collectVisibilities(
        methodElement: ExecutableElement,
        collectedModifiers: MutableSet<KClassVisibility>
    ) {
        methodElement.receiverType?.let { collectVisibilities(it, collectedModifiers) }
        methodElement.enclosingElement.asType()?.let { collectVisibilities(it, collectedModifiers) }
        collectVisibilities(methodElement.returnType, collectedModifiers)
        for (parameter in methodElement.parameters) {
            collectVisibilities(parameter.asType(), collectedModifiers)
        }
    }

    /**
     * Collects all the private, internal and public visibilities of the [TypeMirror].
     *
     * @param type the type to inspect
     * @param collectedModifiers the set containing the collected modifiers
     */
    private fun collectVisibilities(type: TypeMirror, collectedModifiers: MutableSet<KClassVisibility>) {
        when (type) {
            is PrimitiveType -> {
                collectedModifiers.add(KClassVisibility.PUBLIC)
            }
            is ArrayType -> {
                collectVisibilities(type.componentType, collectedModifiers)
            }
            is DeclaredType -> {
                collectVisibilities(type, collectedModifiers)
            }
            is TypeVariable -> {
                collectVisibilities(type.upperBound, collectedModifiers)
            }
            is WildcardType -> {
                type.extendsBound?.let { collectVisibilities(it, collectedModifiers) }
            }
            is NoType -> {
                // Nothing to do.
            }
            else -> {
                throw IllegalArgumentException("Not supported type: " + type + " being a " + type.javaClass)
            }
        }
    }

    /**
     * Collects all the private, internal and public visibilities of the [DeclaredType] and its type arguments.
     *
     * @param type the type to inspect
     * @param collectedModifiers the set containing the collected modifiers
     */
    private fun collectVisibilities(type: DeclaredType, collectedModifiers: MutableSet<KClassVisibility>) {
        try {
            val element = (type.asElement() as TypeElement).toImmutableKmClass()
            when {
                element.isPublic -> collectedModifiers.add(KClassVisibility.PUBLIC)
                element.isInternal -> collectedModifiers.add(KClassVisibility.INTERNAL)
                else -> collectedModifiers.add(KClassVisibility.PRIVATE)
            }
        } catch (e: Exception) {
            // When no Kotlin metadata can be found for a type, let's work with Java style.
            if (Modifier.PUBLIC in type.asElement().modifiers) {
                collectedModifiers.add(KClassVisibility.PUBLIC)
            } else {
                collectedModifiers.add(KClassVisibility.PRIVATE)
            }
        }

        for (typeArgument in type.typeArguments) {
            collectVisibilities(typeArgument, collectedModifiers)
        }
    }
}