package com.calmcontrol.data.repository

import com.calmcontrol.data.local.Outcome
import com.calmcontrol.data.local.TriggerCategory
import com.calmcontrol.data.local.TriggerEvent
import com.calmcontrol.data.local.TriggerEventDao
import kotlinx.coroutines.flow.Flow
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

/**
 * The whole Reports screen reads from one rolling window rather than one query per chart.
 *
 * Personal journalling volumes are tiny — tens of events a day at most — so loading ninety days
 * and aggregating in memory is cheaper than six round trips, sidesteps SQLite date functions and
 * their timezone traps entirely, and leaves every aggregation as a pure, testable function.
 */
class ReportsRepository(
    private val dao: TriggerEventDao,
    private val clock: Clock = Clock.systemDefaultZone(),
) {

    val zone: ZoneId get() = clock.zone

    fun today(): LocalDate = LocalDate.now(clock)

    /**
     * Events from [WINDOW_DAYS] before [today] up to the end of [today].
     *
     * The window is wider than the longest chart (90 days) by [SMOOTHING_WARMUP_DAYS] so the
     * trend line's moving average has real history to average over at its left edge instead of
     * ramping up from zero.
     */
    fun observeRecentWindow(today: LocalDate): Flow<List<TriggerEvent>> {
        val from = today.minusDays((WINDOW_DAYS - 1).toLong())
            .minusDays(SMOOTHING_WARMUP_DAYS.toLong())
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()
        val to = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return dao.observeBetween(from, to)
    }

    /**
     * Just [day]'s events. For screens that only need today's tally — the Log screen's count —
     * rather than the full rolling window every Reports chart is derived from.
     */
    fun observeDay(day: LocalDate): Flow<List<TriggerEvent>> {
        val from = day.atStartOfDay(zone).toInstant().toEpochMilli()
        val to = day.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return dao.observeBetween(from, to)
    }

    /**
     * The seam the logging UI plugs into. Every chart updates off the back of this call, with no
     * refresh plumbing, because Room re-emits the window flow on write.
     */
    suspend fun logEvent(
        outcome: Outcome,
        category: TriggerCategory,
        intensity: Int? = null,
        note: String? = null,
        epochMillis: Long = clock.millis(),
    ): Long = dao.insert(
        TriggerEvent(
            epochMillis = epochMillis,
            outcome = outcome,
            category = category,
            intensity = intensity,
            note = note,
        ),
    )

    suspend fun isEmpty(): Boolean = dao.count() == 0

    suspend fun insertAll(events: List<TriggerEvent>) = dao.insertAll(events)

    companion object {
        const val WINDOW_DAYS = 90
        const val SMOOTHING_WARMUP_DAYS = 6
    }
}
