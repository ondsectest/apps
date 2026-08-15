package com.calmcontrol.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * Targets the specific behaviour change made when the silent-default fallback was removed: an
 * unrecognized stored value must throw, not quietly become [Outcome.CONTROLLED] or
 * [TriggerCategory.OTHER]. A regression back to the old default would pass every other test in
 * this project without this one.
 */
class ConvertersTest {

    private val converters = Converters()

    @Test
    fun `every outcome round-trips through its stored name`() {
        Outcome.entries.forEach { outcome ->
            val stored = converters.outcomeToString(outcome)
            assertEquals(outcome, converters.stringToOutcome(stored))
        }
    }

    @Test
    fun `every category round-trips through its stored name`() {
        TriggerCategory.entries.forEach { category ->
            val stored = converters.categoryToString(category)
            assertEquals(category, converters.stringToCategory(stored))
        }
    }

    @Test
    fun `an unrecognized outcome throws instead of silently becoming CONTROLLED`() {
        assertThrows(IllegalArgumentException::class.java) {
            converters.stringToOutcome("NOT_A_REAL_OUTCOME")
        }
    }

    @Test
    fun `an unrecognized category throws instead of silently becoming OTHER`() {
        assertThrows(IllegalArgumentException::class.java) {
            converters.stringToCategory("NOT_A_REAL_CATEGORY")
        }
    }
}
