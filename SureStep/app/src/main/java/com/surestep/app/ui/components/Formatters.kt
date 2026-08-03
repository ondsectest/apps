package com.surestep.app.ui.components

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

/**
 * Display formats used across History, Calendar and exports.
 *
 * Formatters are cached per locale rather than built once at class load: a
 * `DateTimeFormatter` bakes in the locale it was created with, so a static one
 * would keep formatting in the old language after the user changes their
 * system locale.
 */
object Formatters {

    private val cache = ConcurrentHashMap<Pair<String, String>, DateTimeFormatter>()

    private const val DAY_MONTH_YEAR = "dd MMM yyyy"
    private const val DAY_MONTH = "dd MMM"
    private const val CLOCK = "hh:mm a"
    private const val CLOCK_SECONDS = "hh:mm:ss a"
    private const val PRECISE = "hh:mm:ss.SSS a z"
    private const val MONTH_YEAR = "MMMM yyyy"

    /** Sortable and locale-independent; used for CSV columns. */
    private val machine: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS", Locale.ROOT)

    private fun formatter(pattern: String): DateTimeFormatter {
        val locale = Locale.getDefault()
        return cache.getOrPut(pattern to locale.toLanguageTag()) {
            DateTimeFormatter.ofPattern(pattern, locale)
        }
    }

    fun date(value: ZonedDateTime): String = value.format(formatter(DAY_MONTH_YEAR))
    fun date(value: LocalDate): String = value.format(formatter(DAY_MONTH_YEAR))
    fun shortDate(value: ZonedDateTime): String = value.format(formatter(DAY_MONTH))
    fun time(value: ZonedDateTime): String = value.format(formatter(CLOCK))
    fun time(value: LocalTime): String = value.format(formatter(CLOCK))
    fun timeWithSeconds(value: ZonedDateTime): String = value.format(formatter(CLOCK_SECONDS))

    /** Full precision, including milliseconds and zone, for the detail screen. */
    fun preciseTime(value: ZonedDateTime): String = value.format(formatter(PRECISE))

    fun monthTitle(value: LocalDate): String = value.format(formatter(MONTH_YEAR))

    fun machineTimestamp(value: ZonedDateTime): String = value.format(machine)

    fun coordinates(latitude: Double, longitude: Double): String =
        String.format(Locale.ROOT, "%.6f, %.6f", latitude, longitude)

    fun relativeDay(date: LocalDate, today: LocalDate = LocalDate.now()): String = when (date) {
        today -> "Today"
        today.minusDays(1) -> "Yesterday"
        else -> date.format(formatter(DAY_MONTH_YEAR))
    }
}
