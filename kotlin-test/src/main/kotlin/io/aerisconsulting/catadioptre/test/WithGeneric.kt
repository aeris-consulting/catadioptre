package io.aerisconsulting.catadioptre.test

import io.aerisconsulting.catadioptre.KTestable

@Suppress("kotlin:S1144", "kotlin:S1172")
class WithGeneric {

    @KTestable
    private fun filterMap(values: Map<String, Converted>): Map<String, Converted> {
        return values
    }

    @KTestable
    private fun processGenericStar(spec: Specification<*, Any?, *>): String = "any"

    @KTestable
    private fun processGenericReferencingItself(spec: Specification<Any?, Any?, out Specification<Any?, Any?, *>>) =
        Unit
}

interface Converted

interface Specification<INPUT, OUTPUT, SELF : Specification<INPUT, OUTPUT, SELF>>