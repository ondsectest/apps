package com.calmcontrol.domain

import com.calmcontrol.data.local.Outcome
import com.calmcontrol.data.local.TriggerCategory
import com.calmcontrol.data.local.TriggerEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.Locale

class ReportsCalculatorTest {

    private val zone: ZoneId = ZoneId.of("UTC")

    /** A Saturday, so the current week has days still ahead of it. */
    private val today: LocalDate = LocalDate.of(2026, 8, 1)

    private fun event(
        date: LocalDate,
        outcome: Outcome,
        category: TriggerCategory = TriggerCategory.WORK,
        hour: Int = 12,
    ) = TriggerEvent(
        epochMillis = date.atTime(LocalTime.of(hour, 0)).atZone(zone).toInstant().toEpochMilli(),
        outcome = outcome,
        category = category,
    )

    private fun events(
        date: LocalDate,
        controlled: Int,
        anger: Int,
        category: TriggerCategory = TriggerCategory.WORK,
    ): List<TriggerEvent> =
        List(controlled) { event(date, Outcome.CONTROLLED, category) } +
            List(anger) { event(date, Outcome.ANGER_EXPRESSED, category) }

    @Test
    fun `daily summary reports the self control rate`() {
        val summary = ReportsCalculator.dailySummary(events(today, 7, 3), today, zone)

        assertEquals(10, summary.total)
        assertEquals(7, summary.controlledCount)
        assertEquals(3, summary.angerCount)
        assertEquals(70, summary.selfControlRate)
    }

    @Test
    fun `daily summary ignores other days`() {
        val all = events(today, 2, 0) + events(today.minusDays(1), 5, 5)
        val summary = ReportsCalculator.dailySummary(all, today, zone)

        assertEquals(2, summary.total)
    }

    @Test
    fun `empty day does not divide by zero`() {
        val summary = ReportsCalculator.dailySummary(emptyList(), today, zone)

        assertEquals(0, summary.selfControlRate)
        assertEquals(0f, summary.controlledFraction, 0.0001f)
        assertTrue(!summary.hasData)
    }

    @Test
    fun `week runs monday to sunday and flags days still ahead`() {
        val buckets = ReportsCalculator.weekBuckets(emptyList(), today, zone, Locale.ENGLISH)

        assertEquals(7, buckets.size)
        assertEquals(LocalDate.of(2026, 7, 27), buckets.first().date)
        assertEquals(LocalDate.of(2026, 8, 2), buckets.last().date)
        // Saturday is today, so only Sunday is still to come.
        assertEquals(1, buckets.count { it.isInFuture })
    }

    @Test
    fun `week buckets split outcomes per day`() {
        val monday = LocalDate.of(2026, 7, 27)
        val buckets = ReportsCalculator.weekBuckets(events(monday, 5, 3), today, zone, Locale.ENGLISH)

        assertEquals(5, buckets[0].controlledCount)
        assertEquals(3, buckets[0].angerCount)
        assertEquals(0, buckets[1].total)
    }

    @Test
    fun `trend returns one point per day when history covers the range`() {
        val history = (0L..120L).flatMap { events(today.minusDays(it), 1, 1) }

        val points = ReportsCalculator.trend(history, today, TrendRange.DAYS_30, zone)

        assertEquals(30, points.size)
        assertEquals(today, points.last().date)
        assertEquals(today.minusDays(29), points.first().date)
    }

    @Test
    fun `trend does not extend back past the history that exists`() {
        // Sixty days of history, asked for ninety. The line should start a smoothing window
        // after the first logged day, not a month before any data.
        val history = (0L..59L).flatMap { events(today.minusDays(it), 1, 1) }

        val points = ReportsCalculator.trend(history, today, TrendRange.DAYS_90, zone)

        assertEquals(today.minusDays(59 - 6), points.first().date)
        assertEquals(today, points.last().date)
        assertTrue(points.none { it.controlledAvg == 0f && it.angerAvg == 0f })
    }

    @Test
    fun `trend still returns a line when history is shorter than the smoothing window`() {
        val points = ReportsCalculator.trend(events(today, 2, 1), today, TrendRange.DAYS_30, zone)

        assertEquals(1, points.size)
        assertEquals(today, points.first().date)
    }

    @Test
    fun `trend is empty with no history at all`() {
        assertTrue(ReportsCalculator.trend(emptyList(), today, TrendRange.DAYS_30, zone).isEmpty())
    }

    @Test
    fun `trend averages over the smoothing window`() {
        // Seven controlled events on a single day should read as an average of one per day.
        val points = ReportsCalculator.trend(events(today, 7, 0), today, TrendRange.DAYS_30, zone)

        assertEquals(1f, points.last().controlledAvg, 0.0001f)
        assertEquals(0f, points.last().angerAvg, 0.0001f)
    }

    @Test
    fun `trigger breakdown is ordered by frequency with shares and calm rates`() {
        val all = events(today, 3, 1, TriggerCategory.FAMILY) +
            events(today, 1, 1, TriggerCategory.WORK)

        val breakdown = ReportsCalculator.triggerBreakdown(all, today, zone)

        assertEquals(TriggerCategory.FAMILY, breakdown.first().category)
        assertEquals(4, breakdown.first().count)
        assertEquals(67, breakdown.first().sharePercent)
        assertEquals(75, breakdown.first().controlledRate)
        assertEquals(50, breakdown[1].controlledRate)
    }

    @Test
    fun `trigger breakdown only looks at the last thirty days`() {
        val old = events(today.minusDays(45), 10, 0, TriggerCategory.MONEY)
        val recent = events(today, 1, 0, TriggerCategory.WORK)

        val breakdown = ReportsCalculator.triggerBreakdown(old + recent, today, zone)

        assertEquals(1, breakdown.size)
        assertEquals(TriggerCategory.WORK, breakdown.first().category)
    }

    @Test
    fun `strongest area prefers categories with enough events to be meaningful`() {
        val all = events(today, 1, 0, TriggerCategory.MONEY) + // 100% but only one event
            events(today, 4, 1, TriggerCategory.TRAFFIC) // 80% over five
        val breakdown = ReportsCalculator.triggerBreakdown(all, today, zone)

        val strongest = ReportsCalculator.strongestArea(breakdown)

        assertNotNull(strongest)
        assertEquals(TriggerCategory.TRAFFIC, strongest!!.category)
        assertEquals(80, strongest.controlledRate)
    }

    @Test
    fun `strongest area ignores a perfect record on a trivial sample`() {
        // A busy month, in which one rare category happens to be three-for-three. That is a
        // rounding error, not an achievement, and it should not beat a well-sampled category.
        val all = events(today.minusDays(1), 3, 0, TriggerCategory.SELF) +
            events(today.minusDays(2), 30, 10, TriggerCategory.WORK) +
            events(today.minusDays(3), 40, 20, TriggerCategory.FAMILY)
        val breakdown = ReportsCalculator.triggerBreakdown(all, today, zone)

        val strongest = ReportsCalculator.strongestArea(breakdown)

        assertNotNull(strongest)
        assertEquals(TriggerCategory.WORK, strongest!!.category)
        assertEquals(75, strongest.controlledRate)
    }

    @Test
    fun `strongest area stays silent on a first logged moment`() {
        // Day one: one calm moment. "You controlled this 100% of the time" off a single event is
        // flattery, and there is no fallback that should let it through.
        val breakdown = ReportsCalculator.triggerBreakdown(events(today, 1, 0), today, zone)

        assertNull(ReportsCalculator.strongestArea(breakdown))
    }

    @Test
    fun `strongest area is absent when nothing was controlled`() {
        val breakdown = ReportsCalculator.triggerBreakdown(events(today, 0, 5), today, zone)

        assertNull(ReportsCalculator.strongestArea(breakdown))
    }

    @Test
    fun `reflection reports growth as a percentage`() {
        val lastMonth = events(today.minusDays(45), 10, 0)
        val thisMonth = events(today.minusDays(5), 12, 0)

        val lines = ReportsCalculator.monthlyReflection(lastMonth + thisMonth, today, zone)

        assertTrue(lines.any { it.contains("increased by 20%") })
    }

    @Test
    fun `reflection never describes a decline`() {
        // A worse month than the one before it.
        val lastMonth = events(today.minusDays(45), 20, 0)
        val thisMonth = events(today.minusDays(5), 2, 15)

        val lines = ReportsCalculator.monthlyReflection(lastMonth + thisMonth, today, zone)

        val forbidden = listOf(
            "decreas", "declin", "dropp", "fell", "worse", "fail", "slipped", "less than",
        )
        val joined = lines.joinToString(" ").lowercase(Locale.ROOT)
        forbidden.forEach { word ->
            assertTrue("Reflection should not contain '$word': $joined", !joined.contains(word))
        }
        assertTrue(lines.isNotEmpty())
    }

    @Test
    fun `reflection is a single line until there is a month worth summarising`() {
        val lines = ReportsCalculator.monthlyReflection(events(today, 1, 0), today, zone)

        assertEquals(1, lines.size)
        assertTrue(lines.first().contains("1 moment"))
    }

    @Test
    fun `reflection is empty until there is something to reflect on`() {
        assertTrue(ReportsCalculator.monthlyReflection(emptyList(), today, zone).isEmpty())
    }

    @Test
    fun `reflection names the most common trigger without blaming it`() {
        val all = events(today.minusDays(2), 6, 2, TriggerCategory.FAMILY) +
            events(today.minusDays(3), 1, 1, TriggerCategory.WORK)

        val lines = ReportsCalculator.monthlyReflection(all, today, zone)

        assertTrue(lines.any { it.startsWith("Family came up most often") })
    }
}
