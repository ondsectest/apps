package com.calmcontrol.domain

import com.calmcontrol.data.local.Outcome
import com.calmcontrol.data.local.TriggerEvent
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.Locale

/**
 * Every number and sentence on the Reports screen is derived here, from one list of events.
 *
 * Pure functions on purpose: no Android types, no coroutines, no clock of its own. That keeps the
 * product logic — including the wording rules — unit-testable without an emulator.
 *
 * The wording rules matter as much as the arithmetic. This screen must never read as a failure
 * report, so no generated sentence describes a decline, and red is never the subject of a claim.
 */
object ReportsCalculator {

    /** Days averaged over for the trend line. A week smooths out weekday/weekend rhythm. */
    const val SMOOTHING_DAYS = 7

    /** Window for trigger analysis and the monthly reflection. */
    const val MONTH_DAYS = 30

    /** Absolute floor before a category's success rate is worth celebrating. */
    private const val MIN_EVENTS_FOR_STRONGEST_AREA = 5

    /** A category also has to be at least this share of the month to qualify. */
    private const val MIN_SHARE_FOR_STRONGEST_AREA = 0.05f

    /** Below this, the monthly reflection collapses to a single line. */
    private const val MIN_EVENTS_FOR_FULL_REFLECTION = 5

    // ---------------------------------------------------------------- daily

    fun dailySummary(events: List<TriggerEvent>, day: LocalDate, zone: ZoneId): DailySummary {
        var controlled = 0
        var anger = 0
        events.forEach { event ->
            if (event.localDate(zone) == day) {
                if (event.outcome == Outcome.CONTROLLED) controlled++ else anger++
            }
        }
        return DailySummary(
            date = day,
            total = controlled + anger,
            controlledCount = controlled,
            angerCount = anger,
        )
    }

    // --------------------------------------------------------------- weekly

    /**
     * The ISO week containing [today], Monday through Sunday, so the axis labels line up with the
     * Mon..Sun the design calls for.
     *
     * Days later in the week are returned empty rather than omitted. A week that visibly still has
     * room in it is encouraging; a week that stops abruptly at today looks like data is missing.
     */
    fun weekBuckets(
        events: List<TriggerEvent>,
        today: LocalDate,
        zone: ZoneId,
        locale: Locale = Locale.getDefault(),
    ): List<DayBucket> {
        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val counts = countsByDate(events, zone)
        return (0L until 7L).map { offset ->
            val date = monday.plusDays(offset)
            val count = counts[date] ?: DayCount()
            DayBucket(
                date = date,
                label = date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale),
                controlledCount = count.controlled,
                angerCount = count.anger,
                isInFuture = date.isAfter(today),
            )
        }
    }

    // ---------------------------------------------------------------- trend

    /**
     * One point per day for the last [range] days, each a trailing [SMOOTHING_DAYS] average.
     *
     * The line never extends back past the history that exists. Someone two months in who opens
     * the 90-day view would otherwise meet a month of flat zero before their data begins — which
     * is accurate, and reads exactly like the failure report this screen must never be. It starts
     * a smoothing window after the first logged day instead, so the first point is a real average
     * rather than one ramping up out of implicit zeros. As history grows, the line fills out.
     */
    fun trend(
        events: List<TriggerEvent>,
        today: LocalDate,
        range: TrendRange,
        zone: ZoneId,
    ): List<TrendPoint> {
        val earliest = events.minOfOrNull { it.localDate(zone) } ?: return emptyList()
        val rangeStart = today.minusDays((range.days - 1).toLong())
        val warmedUp = earliest.plusDays((SMOOTHING_DAYS - 1).toLong())
        // Never start after today — a user whose whole history is shorter than the smoothing
        // window still gets a line, just a short one.
        val start = maxOf(rangeStart, minOf(warmedUp, today))
        val days = ChronoUnit.DAYS.between(start, today) + 1

        val counts = countsByDate(events, zone)
        return (0L until days).map { offset ->
            val date = start.plusDays(offset)
            var controlled = 0
            var anger = 0
            for (back in 0L until SMOOTHING_DAYS.toLong()) {
                val day = counts[date.minusDays(back)] ?: continue
                controlled += day.controlled
                anger += day.anger
            }
            TrendPoint(
                date = date,
                controlledAvg = controlled.toFloat() / SMOOTHING_DAYS,
                angerAvg = anger.toFloat() / SMOOTHING_DAYS,
            )
        }
    }

    // ------------------------------------------------------- trigger analysis

    /** Categories over the last [windowDays], most frequent first. */
    fun triggerBreakdown(
        events: List<TriggerEvent>,
        today: LocalDate,
        zone: ZoneId,
        windowDays: Int = MONTH_DAYS,
    ): List<TriggerShare> {
        val window = eventsInLastDays(events, today, zone, windowDays)
        if (window.isEmpty()) return emptyList()

        return window
            .groupBy { it.category }
            .map { (category, categoryEvents) ->
                val controlled = categoryEvents.count { it.outcome == Outcome.CONTROLLED }
                TriggerShare(
                    category = category,
                    count = categoryEvents.size,
                    controlledCount = controlled,
                    sharePercent = percentOf(categoryEvents.size, window.size),
                    controlledRate = percentOf(controlled, categoryEvents.size),
                )
            }
            .sortedWith(compareByDescending<TriggerShare> { it.count }.thenBy { it.category.label })
    }

    /**
     * The category handled best.
     *
     * The sample threshold scales with how much was logged, because a fixed floor stops meaning
     * anything once the month is busy: three-for-three is a real achievement in a quiet week and
     * a rounding error in a month of 140 moments. Celebrating "100%" off the latter is flattery,
     * and the user would know it.
     */
    fun strongestArea(breakdown: List<TriggerShare>): StrongestArea? {
        if (breakdown.isEmpty()) return null
        val total = breakdown.sumOf { it.count }
        val minimum = maxOf(
            MIN_EVENTS_FOR_STRONGEST_AREA,
            Math.round(total * MIN_SHARE_FOR_STRONGEST_AREA),
        )
        // No fallback to the full list. An earlier version fell back when nothing met the
        // threshold, which defeated the guard in exactly the case it existed for: on day one,
        // a single controlled moment became "you controlled this 100% of the time". Better to
        // say nothing until there is something to say.
        val candidates = breakdown.filter { it.count >= minimum }
        val best = candidates
            .filter { it.controlledCount > 0 }
            .maxWithOrNull(compareBy<TriggerShare> { it.controlledRate }.thenBy { it.count })
            ?: return null
        return StrongestArea(best.category, best.controlledRate)
    }

    // ----------------------------------------------------------- reflection

    /**
     * Two to four plain sentences summarising the month.
     *
     * Deliberately asymmetric: an improvement is stated as a number, a decline never is. When the
     * count is down we report the absolute work done and the awareness behind it. That is not
     * spin — the honest reading of a month with more logged anger is usually that the user was
     * paying closer attention, and that is the behaviour worth reinforcing.
     */
    fun monthlyReflection(
        events: List<TriggerEvent>,
        today: LocalDate,
        zone: ZoneId,
    ): List<String> {
        val current = eventsInLastDays(events, today, zone, MONTH_DAYS)
        if (current.isEmpty()) return emptyList()

        // Below a handful of moments the four sentences below all restate the same fact, and
        // "you handled 1 trigger calmly / across 1 moment you stayed in control 100% of the time"
        // reads like the app padding. One honest line is better until there is a month to talk
        // about.
        if (current.size < MIN_EVENTS_FOR_FULL_REFLECTION) {
            return listOf(
                "You have logged ${plural(current.size, "moment")} so far. " +
                    "The picture builds from here.",
            )
        }

        val previous = eventsInRange(
            events = events,
            zone = zone,
            fromInclusive = today.minusDays((MONTH_DAYS * 2 - 1).toLong()),
            toInclusive = today.minusDays(MONTH_DAYS.toLong()),
        )

        val currentControlled = current.count { it.outcome == Outcome.CONTROLLED }
        val previousControlled = previous.count { it.outcome == Outcome.CONTROLLED }

        val lines = mutableListOf<String>()

        when {
            previousControlled > 0 && currentControlled > previousControlled -> {
                val change = percentOf(currentControlled - previousControlled, previousControlled)
                lines += "Your controlled responses increased by $change% this month."
            }

            previousControlled > 0 && currentControlled == previousControlled -> {
                lines += "You held steady this month, matching last month's calm responses."
            }

            else -> {
                lines += "You logged ${plural(currentControlled, "calm response")} this month. " +
                    "Noticing the moment is where control begins."
            }
        }

        lines += "You handled ${plural(currentControlled, "trigger")} calmly."

        // The honest rate is worth stating when it is something to stand on. When it is low,
        // repeating it here adds nothing the user does not already feel — so that line becomes
        // about the logging itself, which is the behaviour that actually moves the rate.
        val rate = percentOf(currentControlled, current.size)
        lines += if (rate >= 50) {
            "Across ${plural(current.size, "moment")}, you stayed in control $rate% of the time."
        } else {
            "You noticed and recorded ${plural(current.size, "moment")}. " +
                "Catching them is what shifts the pattern."
        }

        current.groupBy { it.category }
            .maxByOrNull { it.value.size }
            ?.let { (category, categoryEvents) ->
                val controlled = categoryEvents.count { it.outcome == Outcome.CONTROLLED }
                lines += "${category.label} came up most often, and you met it calmly " +
                    "${plural(controlled, "time")}."
            }

        return lines
    }

    // -------------------------------------------------------------- helpers

    private class DayCount(var controlled: Int = 0, var anger: Int = 0)

    private fun countsByDate(
        events: List<TriggerEvent>,
        zone: ZoneId,
    ): Map<LocalDate, DayCount> {
        val counts = HashMap<LocalDate, DayCount>()
        events.forEach { event ->
            val bucket = counts.getOrPut(event.localDate(zone)) { DayCount() }
            if (event.outcome == Outcome.CONTROLLED) bucket.controlled++ else bucket.anger++
        }
        return counts
    }

    private fun eventsInLastDays(
        events: List<TriggerEvent>,
        today: LocalDate,
        zone: ZoneId,
        days: Int,
    ): List<TriggerEvent> = eventsInRange(
        events = events,
        zone = zone,
        fromInclusive = today.minusDays((days - 1).toLong()),
        toInclusive = today,
    )

    private fun eventsInRange(
        events: List<TriggerEvent>,
        zone: ZoneId,
        fromInclusive: LocalDate,
        toInclusive: LocalDate,
    ): List<TriggerEvent> = events.filter { event ->
        val date = event.localDate(zone)
        !date.isBefore(fromInclusive) && !date.isAfter(toInclusive)
    }

    private fun plural(count: Int, noun: String): String =
        if (count == 1) "$count $noun" else "$count ${noun}s"
}
