package com.surestep.app.reminders

import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime

/**
 * When a reminder should next fire. Pure, so the awkward cases — a time already
 * past today, a mask that skips several days, a week wrapping into the next —
 * can be tested without WorkManager.
 */
object ReminderSchedule {

    /**
     * Next date-time matching [daysOfWeekMask] strictly after [from], or null if
     * no day is selected. Bit 0 is Monday, bit 6 is Sunday.
     */
    fun nextOccurrence(
        daysOfWeekMask: Int,
        hour: Int,
        minute: Int,
        from: ZonedDateTime,
    ): ZonedDateTime? {
        if (daysOfWeekMask == 0) return null
        // Eight days, not seven: if today is selected but the time has already
        // passed, the answer is today-next-week, which is the eighth candidate.
        for (dayOffset in 0..7) {
            val candidateDate = from.toLocalDate().plusDays(dayOffset.toLong())
            val bit = 1 shl (candidateDate.dayOfWeek.value - 1)
            if (daysOfWeekMask and bit == 0) continue

            val candidate = LocalDateTime.of(candidateDate, LocalTime.of(hour, minute))
                .atZone(from.zone)
            if (candidate.isAfter(from)) return candidate
        }
        return null
    }
}
