package com.surestep.app.domain

import com.surestep.app.domain.model.DayStatus
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class CompletionRulesTest {

    private val today = LocalDate.of(2026, 8, 3)
    private val checklistStart = LocalDate.of(2026, 7, 1)

    @Test
    fun `a day with every task recorded is complete`() {
        assertEquals(
            DayStatus.COMPLETE,
            CompletionRules.statusFor(4, 4, today.minusDays(1), today, checklistStart),
        )
    }

    @Test
    fun `a day with some tasks recorded is partial`() {
        assertEquals(
            DayStatus.PARTIAL,
            CompletionRules.statusFor(2, 4, today.minusDays(1), today, checklistStart),
        )
    }

    @Test
    fun `an empty past day after the checklist started is missed`() {
        assertEquals(
            DayStatus.MISSED,
            CompletionRules.statusFor(0, 4, today.minusDays(1), today, checklistStart),
        )
    }

    @Test
    fun `today is never missed while it is still in progress`() {
        assertEquals(
            DayStatus.NONE,
            CompletionRules.statusFor(0, 4, today, today, checklistStart),
        )
    }

    @Test
    fun `days before the checklist existed are not missed`() {
        assertEquals(
            DayStatus.NONE,
            CompletionRules.statusFor(0, 4, checklistStart.minusDays(1), today, checklistStart),
        )
    }

    @Test
    fun `a fresh install shows no missed days at all`() {
        assertEquals(
            DayStatus.NONE,
            CompletionRules.statusFor(0, 4, today.minusDays(5), today, checklistStart = null),
        )
    }

    @Test
    fun `future days are blank`() {
        assertEquals(
            DayStatus.NONE,
            CompletionRules.statusFor(0, 4, today.plusDays(1), today, checklistStart),
        )
    }

    @Test
    fun `streak counts consecutive complete days ending today`() {
        val completions = mapOf(
            today.toString() to 4,
            today.minusDays(1).toString() to 4,
            today.minusDays(2).toString() to 4,
            today.minusDays(3).toString() to 1,
        )
        assertEquals(3, CompletionRules.streak(completions, today, 4, maxDays = 365))
    }

    @Test
    fun `an incomplete today does not break the streak`() {
        // The morning case: nothing recorded yet today, but yesterday and the
        // day before were complete. The streak should still read 2.
        val completions = mapOf(
            today.minusDays(1).toString() to 4,
            today.minusDays(2).toString() to 4,
        )
        assertEquals(2, CompletionRules.streak(completions, today, 4, maxDays = 365))
    }

    @Test
    fun `a gap yesterday ends the streak`() {
        val completions = mapOf(
            today.minusDays(2).toString() to 4,
            today.minusDays(3).toString() to 4,
        )
        assertEquals(0, CompletionRules.streak(completions, today, 4, maxDays = 365))
    }

    @Test
    fun `streak is zero when there are no tasks`() {
        assertEquals(
            0,
            CompletionRules.streak(mapOf(today.toString() to 0), today, 0, maxDays = 365),
        )
    }

    @Test
    fun `streak stops at the window limit instead of running forever`() {
        val completions = (0..40L).associate { today.minusDays(it).toString() to 4 }
        assertEquals(10, CompletionRules.streak(completions, today, 4, maxDays = 10))
    }

    @Test
    fun `missed count ignores days before the checklist existed`() {
        assertEquals(
            0,
            CompletionRules.missedOn(checklistStart.minusDays(1), 0, 4, checklistStart),
        )
    }

    @Test
    fun `missed count reports outstanding tasks for a day the checklist covered`() {
        assertEquals(3, CompletionRules.missedOn(today.minusDays(1), 1, 4, checklistStart))
    }

    @Test
    fun `missed count never goes negative when tasks were removed`() {
        // Six records exist from a day when the checklist was longer than it is now.
        assertEquals(0, CompletionRules.missedOn(today.minusDays(1), 6, 4, checklistStart))
    }
}
