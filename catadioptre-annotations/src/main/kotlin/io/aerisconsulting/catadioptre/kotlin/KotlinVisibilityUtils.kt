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
import javax.annotation.processing.Messager
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
import javax.tools.Diagnostic.Kind
import kotlin.metadata.KmClass
import kotlin.metadata.Visibility
import kotlin.metadata.visibility

@DelicateKotlinPoetApi("Awareness of delicate aspect")
internal class KotlinVisibilityUtils(
    private val classInspector: ClassInspector,
    private val elementsUtils: Elements,
    private val messager: Messager
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
     * @param typeSpec the Kotlin poet specification of the type containing the method
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
     * Determines whether all the types in the context of a property of type [typeName] enclosed in [typeSpec]
     * can be accessible from a public method of the module.
     *
     * @param typeSpec the enclosing type of the property to check
     * @param typeName the type of the property to check
     */
    fun detectLowestVisibility(typeSpec: TypeSpec, typeName: TypeName): KModifier {
        val collectedVisibilities = mutableSetOf<KClassVisibility>()
        if (KModifier.INTERNAL in typeSpec.modifiers) {
            collectedVisibilities += KClassVisibility.INTERNAL
        } else if (KModifier.PRIVATE in typeSpec.modifiers) {
            collectedVisibilities += KClassVisibility.PRIVATE
        }
        collectVisibilities(typeName, collectedVisibilities)
        return collectedVisibilities.minByOrNull { it.ordinal }?.klassModifiers?.first() ?: KModifier.PUBLIC
    }

    private fun collectVisibilities(
        typeName: TypeName,
        collectedVisibilities: MutableSet<KClassVisibility>
    ) {
        messager.printMessage(Kind.OTHER, "Collecting visibility of type $typeName")
        if (typeName is ParameterizedTypeName) {
            collectVisibilities(typeName.rawType, collectedVisibilities)
            typeName.typeArguments.forEach {
                collectVisibilities(it, collectedVisibilities)
            }
        } else if (typeName is ClassName) {
            visibilityCache[typeName.canonicalName]?.also { collectedVisibilities += it } ?: run {
                runCatching {
                    elementsUtils.getTypeElement(typeName.canonicalName).asType()
                }.getOrNull()?.let {
                    collectVisibilities(it, collectedVisibilities)
                }
            }
        }
    }

    /**
     * Collects all the private, internal and public visibilities of the [ExecutableElement].
     *
     * @param methodElement the element to inspect
     * @param collectedVisibilities the set containing the collected modifiers
     */
    private fun collectVisibilities(
        methodElement: ExecutableElement,
        collectedVisibilities: MutableSet<KClassVisibility>
    ) {
        methodElement.receiverType?.let { collectVisibilities(it, collectedVisibilities) }
        methodElement.enclosingElement.asType()?.let { collectVisibilities(it, collectedVisibilities) }
        collectVisibilities(methodElement.returnType, collectedVisibilities)
        for (parameter in methodElement.parameters) {
            collectVisibilities(parameter.asType(), collectedVisibilities)
        }
    }

    /**
     * Collects all the private, internal and public visibilities of the [TypeMirror].
     *
     * @param type the type to inspect
     * @param collectedVisibilities the set containing the collected modifiers
     */
    private fun collectVisibilities(
        type: TypeMirror,
        collectedVisibilities: MutableSet<KClassVisibility>,
        visitedTypes: MutableSet<TypeMirror> = mutableSetOf()
    ) {
        if (type !in visitedTypes) {
            // When the type as an argument of its own type, it should not be visited a second type to avoid
            // infinite loops.
            visitedTypes += type

            when (type) {
                is PrimitiveType -> collectedVisibilities.add(KClassVisibility.PUBLIC)
                is ArrayType -> collectVisibilities(type.componentType, collectedVisibilities, visitedTypes)
                is DeclaredType -> collectVisibilities(type, collectedVisibilities, visitedTypes)
                is TypeVariable -> collectVisibilities(type.upperBound, collectedVisibilities, visitedTypes)
                is WildcardType -> type.extendsBound?.let {
                    collectVisibilities(
                        it,
                        collectedVisibilities,
                        visitedTypes
                    )
                }

                is NoType -> {
                    // Nothing to do.
                }

                else -> throw IllegalArgumentException("Not supported type: " + type + " being a " + type.javaClass)
            }
        }
    }

    /**
     * Collects all the private, internal and public visibilities of the [DeclaredType] and its type arguments.
     *
     * @param type the type to inspect
     * @param collectedVisibilities the set containing the collected modifiers
     */
    private fun collectVisibilities(
        type: DeclaredType,
        collectedVisibilities: MutableSet<KClassVisibility>,
        visitedTypes: MutableSet<TypeMirror>
    ) {
        val element = (type.asElement() as TypeElement)
        val typeVisibility = visibilityCache.computeIfAbsent(element.toString()) {
            val kmClass = typesCache.computeIfAbsent(element.toString()) {
                runCatching { element.toTypeSpec(false, classInspector).tag<KmClass>() }.getOrNull()
            }
            when {
                kmClass?.visibility == Visibility.PRIVATE -> KClassVisibility.PRIVATE
                kmClass?.visibility == Visibility.INTERNAL -> KClassVisibility.INTERNAL
                Modifier.PRIVATE in element.modifiers -> KClassVisibility.PRIVATE
                Modifier.PROTECTED in element.modifiers -> KClassVisibility.PRIVATE
                else -> KClassVisibility.PUBLIC
            }
        }
        collectedVisibilities.add(typeVisibility)
        type.typeArguments.forEach { typeArgument ->
            collectVisibilities(typeArgument, collectedVisibilities, visitedTypes)
        }

    }
}