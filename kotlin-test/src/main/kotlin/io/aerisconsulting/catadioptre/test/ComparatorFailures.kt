package io.aerisconsulting.catadioptre.test

import io.aerisconsulting.catadioptre.KTestable

class ComparatorFailures {

    @KTestable
    private fun <T : Comparator<*>> checkString(
        argument1: Comparator<String>?,
        argument2: Comparator<Map<Int, List<Comparator<Int>>>>
    ): List<Comparator<String>> {
        TODO()
    }

    @KTestable
    private fun <T> checkUndefined(comparable: Comparator<T?>?) = Unit

    @KTestable
    private fun returnComparator(): Map<Int, List<Comparator<Int>>> {
        TODO()
    }

}