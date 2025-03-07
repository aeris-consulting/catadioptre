package io.aerisconsulting.catadioptre.kotlin

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.DelicateKotlinPoetApi
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.metadata.specs.ClassInspector
import com.squareup.kotlinpoet.metadata.specs.toTypeSpec
import com.squareup.kotlinpoet.tag
import java.util.concurrent.ConcurrentHashMap
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
import javax.lang.model.util.Elements
import kotlin.metadata.KmClass
import kotlin.metadata.Visibility
import kotlin.metadata.visibility

@DelicateKotlinPoetApi("Awareness of delicate aspect")
internal class KotlinVisibilityUtils(
    private val classInspector: ClassInspector,
    private val elementsUtils: Elements
) {

    /**
     * Cache for the resolved Kotlin types as [KmClass].
     */
    private val typesCache = ConcurrentHashMap<String, KmClass?>()

    /**
     * Cache for the resolved visibility of types.
     */
    private val visibilityCache = ConcurrentHashMap<String, KClassVisibility>()

    /**
     * Determines whether all the types in the context of the [ExecutableElement] can be accessible from a public
     * method of the module.
     *
     * @param methodElement the element to inspect
     */
    fun detectLowestVisibility(typeSpec: TypeSpec, methodElement: ExecutableElement): KModifier {
        val collectedModifiers = mutableSetOf<KClassVisibility>()
        if (KModifier.INTERNAL in typeSpec.modifiers) {
            collectedModifiers += KClassVisibility.INTERNAL
        } else if (KModifier.PRIVATE in typeSpec.modifiers) {
            collectedModifiers += KClassVisibility.PRIVATE
        }
        collectVisibilities(methodElement, collectedModifiers)
        return collectedModifiers.minByOrNull { it.ordinal }?.klassModifiers?.first() ?: KModifier.PUBLIC
    }


    /**
     * Determines whether all the types in the context of the [ExecutableElement] can be accessible from a public
     * method of the module.
     *
     * @param methodElement the element to inspect
     */
    fun detectLowestVisibility(typeSpec: TypeSpec, typeName: TypeName): KModifier {
        val collectedModifiers = mutableSetOf<KClassVisibility>()
        if (KModifier.INTERNAL in typeSpec.modifiers) {
            collectedModifiers += KClassVisibility.INTERNAL
        } else if (KModifier.PRIVATE in typeSpec.modifiers) {
            collectedModifiers += KClassVisibility.PRIVATE
        }
        collectVisibilities(typeName, collectedModifiers)
        return collectedModifiers.minByOrNull { it.ordinal }?.klassModifiers?.first() ?: KModifier.PUBLIC
    }

    private fun collectVisibilities(
        typeName: TypeName,
        collectedModifiers: MutableSet<KClassVisibility>
    ) {
        if (typeName is ParameterizedTypeName) {
            collectVisibilities(typeName.rawType, collectedModifiers)
            typeName.typeArguments.forEach {
                collectVisibilities(it, collectedModifiers)
            }
        } else if (typeName is ClassName) {
            visibilityCache[typeName.canonicalName]?.also { collectedModifiers += it } ?: run {
                runCatching {
                    elementsUtils.getTypeElement(typeName.canonicalName).asType()
                }.getOrNull()?.let {
                    collectVisibilities(it, collectedModifiers)
                }
            }
        }
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
            is PrimitiveType -> collectedModifiers.add(KClassVisibility.PUBLIC)
            is ArrayType -> collectVisibilities(type.componentType, collectedModifiers)
            is DeclaredType -> collectVisibilities(type, collectedModifiers)
            is TypeVariable -> collectVisibilities(type.upperBound, collectedModifiers)
            is WildcardType -> type.extendsBound?.let { collectVisibilities(it, collectedModifiers) }
            is NoType -> {
                // Nothing to do.
            }

            else -> throw IllegalArgumentException("Not supported type: " + type + " being a " + type.javaClass)
        }
    }

    /**
     * Collects all the private, internal and public visibilities of the [DeclaredType] and its type arguments.
     *
     * @param type the type to inspect
     * @param collectedModifiers the set containing the collected modifiers
     */
    private fun collectVisibilities(type: DeclaredType, collectedModifiers: MutableSet<KClassVisibility>) {
        val element = (type.asElement() as TypeElement)
        val typeVisibility = visibilityCache.computeIfAbsent(element.toString()) {
            val kmClass = typesCache.computeIfAbsent(element.toString()) {
                runCatching { element.toTypeSpec(false, classInspector).tag<KmClass>() }.getOrNull()
            }
            when {
                kmClass?.visibility == Visibility.PRIVATE -> KClassVisibility.PRIVATE
                kmClass?.visibility == Visibility.INTERNAL -> KClassVisibility.INTERNAL
                Modifier.PRIVATE in element.modifiers -> KClassVisibility.PRIVATE
                else -> KClassVisibility.PUBLIC
            }
        }
        collectedModifiers.add(typeVisibility)

        for (typeArgument in type.typeArguments) {
            collectVisibilities(typeArgument, collectedModifiers)
        }
    }
}