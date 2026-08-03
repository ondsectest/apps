package com.surestep.app.reminders

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

class ReminderScheduleTest {

    private val zone: ZoneId = ZoneId.of("Asia/Kolkata")

    /** Monday 3 August 2026, 07:00 local. */
    private val mondayMorning: ZonedDateTime =
        LocalDateTime.of(2026, 8, 3, 7, 0).atZone(zone)

    private val everyDay = 0b1111111
    private val weekdays = 0b0011111
    private val mondayOnly = 0b0000001
    private val sundayOnly = 0b1000000

    @Test
    fun `fires later the same day when the time has not passed`() {
        val next = ReminderSchedule.nextOccurrence(everyDay, 8, 30, mondayMorning)
        assertEquals(LocalDateTime.of(2026, 8, 3, 8, 30).atZone(zone), next)
    }

    @Test
    fun `rolls to tomorrow when today's time has already passed`() {
        val next = ReminderSchedule.nextOccurrence(everyDay, 6, 0, mondayMorning)
        assertEquals(LocalDateTime.of(2026, 8, 4, 6, 0).atZone(zone), next)
    }

    @Test
    fun `skips days that are not selected`() {
        // Friday evening, weekdays-only: the next firing is Monday, not Saturday.
        val fridayEvening = LocalDateTime.of(2026, 8, 7, 21, 0).atZone(zone)
        val next = ReminderSchedule.nextOccurrence(weekdays, 8, 30, fridayEvening)
        assertEquals(LocalDateTime.of(2026, 8, 10, 8, 30).atZone(zone), next)
    }

    @Test
    fun `a single selected day wraps a full week when its time has passed`() {
        val next = ReminderSchedule.nextOccurrence(mondayOnly, 6, 0, mondayMorning)
        assertEquals(LocalDateTime.of(2026, 8, 10, 6, 0).atZone(zone), next)
    }

    @Test
    fun `sunday is bit six, not bit zero`() {
        // Off-by-one here would silently fire reminders on the wrong day.
        val next = ReminderSchedule.nextOccurrence(sundayOnly, 22, 0, mondayMorning)
        assertEquals(LocalDateTime.of(2026, 8, 9, 22, 0).atZone(zone), next)
    }

    @Test
    fun `an empty day mask never fires`() {
        assertNull(ReminderSchedule.nextOccurrence(0, 8, 30, mondayMorning))
    }

    @Test
    fun `exactly now counts as passed rather than firing twice`() {
        val next = ReminderSchedule.nextOccurrence(everyDay, 7, 0, mondayMorning)
        assertEquals(LocalDateTime.of(2026, 8, 4, 7, 0).atZone(zone), next)
    }
}
