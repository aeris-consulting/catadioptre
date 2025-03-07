package io.aerisconsulting.catadioptre.kotlin

import com.squareup.kotlinpoet.FunSpec
import javax.lang.model.element.ExecutableElement

/**
 * Represents a function for which a proxy is to generate.
 *
 * @property element the function as a Java model [ExecutableElement]
 * @property spec the function as a KotlinPoet [FunSpec]
 */
data class AnnotatedFunction(
    val element: ExecutableElement,
    val spec: FunSpec
)
