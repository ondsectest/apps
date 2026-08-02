package com.calmcontrol.data

import android.content.Context
import android.content.SharedPreferences
import com.calmcontrol.domain.Quote
import com.calmcontrol.domain.Quotes
import kotlin.random.Random

/**
 * Hands out the next quote, in order, wrapping at the end.
 *
 * Sequential rather than random on purpose: random selection would repeat the same line two logs
 * running often enough to notice, and a repeat reads as the app not paying attention. Walking the
 * list guarantees you see every one before you see any twice.
 *
 * The position survives restarts, so the rotation is not quietly reset to the same handful of
 * quotes every time the app is reopened. The starting point is random per install so two people
 * comparing phones do not both begin at Marcus Aurelius.
 */
class QuoteRotation(
    context: Context,
    private val quotes: List<Quote> = Quotes.all,
) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun next(): Quote {
        if (quotes.isEmpty()) error("Quotes.all must not be empty")
        val index = currentIndex()
        prefs.edit().putInt(KEY_INDEX, (index + 1) % quotes.size).apply()
        return quotes[index]
    }

    private fun currentIndex(): Int {
        val stored = prefs.getInt(KEY_INDEX, NO_INDEX)
        if (stored in quotes.indices) return stored
        val start = Random.nextInt(quotes.size)
        prefs.edit().putInt(KEY_INDEX, start).apply()
        return start
    }

    private companion object {
        const val PREFS_NAME = "calm_control_quotes"
        const val KEY_INDEX = "next_index"
        const val NO_INDEX = -1
    }
}
