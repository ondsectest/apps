package com.calmcontrol.data.local

import androidx.room.TypeConverter

/**
 * Enums are stored by name rather than ordinal so that reordering or inserting a constant
 * can never silently rewrite the meaning of existing rows.
 *
 * A name that fails to match throws rather than falling back to a default. A silent fallback
 * here would mean a corrupted [Outcome] column quietly reads back as CONTROLLED — the one thing
 * this app's charts must never get wrong about a moment the user actually logged.
 */
class Converters {

    @TypeConverter
    fun outcomeToString(value: Outcome): String = value.name

    @TypeConverter
    fun stringToOutcome(value: String): Outcome = Outcome.valueOf(value)

    @TypeConverter
    fun categoryToString(value: TriggerCategory): String = value.name

    @TypeConverter
    fun stringToCategory(value: String): TriggerCategory = TriggerCategory.valueOf(value)
}
