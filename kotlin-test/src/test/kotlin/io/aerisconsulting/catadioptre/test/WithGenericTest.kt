package io.aerisconsulting.catadioptre.test

import io.aerisconsulting.catadioptre.test.catadioptre.filterMap
import io.aerisconsulting.catadioptre.test.catadioptre.processGenericReferencingItself
import io.aerisconsulting.catadioptre.test.catadioptre.processGenericStar
import io.mockk.mockk
import org.junit.jupiter.api.Test

internal class WithGenericTest {

    @Test
    internal fun `should compile call of a function with generics as argument and returned value`() {
        val instance = WithGeneric()
        instance.filterMap(mapOf("1" to mockk<Converted>()))
    }

    @Test
    internal fun `should compile call of a function with generic star as argument`() {
        val instance = WithGeneric()
        instance.processGenericStar(mockk<Specification<Int, Any?, *>>())
    }

    @Test
    internal fun `should compile call of a function with generic referencing itself as argument`() {
        val instance = WithGeneric()
        instance.processGenericReferencingItself(mockk<Specification<Any?, Any?, out Specification<Any?, Any?, *>>>())
    }
}