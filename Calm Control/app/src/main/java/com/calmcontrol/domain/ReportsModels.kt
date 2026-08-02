package com.calmcontrol.domain

import com.calmcontrol.data.local.TriggerCategory
import java.time.LocalDate

/** Today's headline numbers, feeding the circular progress ring. */
data class DailySummary(
    val date: LocalDate,
    val total: Int,
    val controlledCount: Int,
    val angerCount: Int,
) {
    /** 0..100, rounded. Shown as the big number in the middle of the ring. */
    val selfControlRate: Int = percentOf(controlledCount, total)

    /** 0f..1f, what the green arc actually sweeps. */
    val controlledFraction: Float =
        if (total == 0) 0f else controlledCount.toFloat() / total.toFloat()

    val hasData: Boolean = total > 0

    companion object {
        fun empty(date: LocalDate) = DailySummary(date, 0, 0, 0)
    }
}

/** One day column in the weekly chart. */
data class DayBucket(
    val date: LocalDate,
    val label: String,
    val controlledCount: Int,
    val angerCount: Int,
    val isInFuture: Boolean,
) {
    val total: Int = controlledCount + angerCount
}

/**
 * One point on the monthly trend line.
 *
 * Values are 7-day trailing averages rather than raw daily counts. Raw counts on a personal log
 * are spiky enough to read as noise, and noise reads as chaos — the opposite of what this screen
 * is for. Averaging keeps the line calm and lets a genuine upward trend actually show.
 */
data class TrendPoint(
    val date: LocalDate,
    val controlledAvg: Float,
    val angerAvg: Float,
)

enum class TrendRange(val days: Int, val label: String) {
    DAYS_30(30, "30 days"),
    DAYS_90(90, "90 days"),
}

/** One row of the trigger analysis. */
data class TriggerShare(
    val category: TriggerCategory,
    val count: Int,
    val controlledCount: Int,
    /** Share of all triggers in the window, 0..100. */
    val sharePercent: Int,
    /** How often this trigger ended calmly, 0..100. */
    val controlledRate: Int,
) {
    /** 0f..1f, used for the bar width. */
    val shareFraction: Float = sharePercent / 100f
}

/** The "you're strongest here" callout. Always framed as a win. */
data class StrongestArea(
    val category: TriggerCategory,
    val controlledRate: Int,
)

/** 0..100, rounded, safe on a zero denominator. */
internal fun percentOf(part: Int, total: Int): Int =
    if (total <= 0) 0 else Math.round(part * 100f / total)
