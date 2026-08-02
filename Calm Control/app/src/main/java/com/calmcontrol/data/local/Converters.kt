package com.calmcontrol.data.local

import androidx.room.TypeConverter

/**
 * Enums are stored by name rather than ordinal so that reordering or inserting a constant
 * can never silently rewrite the meaning of existing rows.
 */
class Converters {

    @TypeConverter
    fun outcomeToString(value: Outcome): String = value.name

    @TypeConverter
    fun stringToOutcome(value: String): Outcome =
        runCatching { Outcome.valueOf(value) }.getOrDefault(Outcome.CONTROLLED)

    @TypeConverter
    fun categoryToString(value: TriggerCategory): String = value.name

    @TypeConverter
    fun stringToCategory(value: String): TriggerCategory =
        runCatching { TriggerCategory.valueOf(value) }.getOrDefault(TriggerCategory.OTHER)
}
