package com.surestep.app.domain

import com.surestep.app.domain.model.DayStatus
import java.time.LocalDate

/**
 * How a day is scored and how a streak is counted.
 *
 * Pure functions, deliberately separate from the repository: these rules decide
 * whether the app tells someone they did well or badly, so they are the part
 * most worth being able to test directly.
 */
object CompletionRules {

    fun statusFor(
        completed: Int,
        total: Int,
        day: LocalDate,
        today: LocalDate,
        checklistStart: LocalDate?,
    ): DayStatus = when {
        total == 0 || day.isAfter(today) -> DayStatus.NONE
        completed >= total -> DayStatus.COMPLETE
        completed > 0 -> DayStatus.PARTIAL
        // Today is still in progress, so it is not yet a miss.
        day == today -> DayStatus.NONE
        // Nothing was missed before the checklist existed. A new install should
        // not open onto a month of red.
        checklistStart == null || day.isBefore(checklistStart) -> DayStatus.NONE
        else -> DayStatus.MISSED
    }

    /**
     * Consecutive fully-completed days ending today.
     *
     * Today being incomplete does not break the streak — the count then runs
     * back from yesterday. Without that, every streak would read as zero each
     * morning, which punishes the user for the time of day.
     */
    fun streak(
        completedByDate: Map<String, Int>,
        today: LocalDate,
        activeTaskCount: Int,
        maxDays: Int,
    ): Int {
        if (activeTaskCount == 0) return 0
        val isComplete = { day: LocalDate ->
            (completedByDate[day.toString()] ?: 0) >= activeTaskCount
        }
        var cursor = if (isComplete(today)) today else today.minusDays(1)
        var streak = 0
        while (streak < maxDays && isComplete(cursor)) {
            streak++
            cursor = cursor.minusDays(1)
        }
        return streak
    }

    /**
     * A day only counts as missed if the checklist already existed then.
     */
    fun missedOn(
        day: LocalDate,
        completed: Int,
        activeTaskCount: Int,
        checklistStart: LocalDate?,
    ): Int {
        if (checklistStart == null || day.isBefore(checklistStart)) return 0
        return (activeTaskCount - completed).coerceAtLeast(0)
    }
}
