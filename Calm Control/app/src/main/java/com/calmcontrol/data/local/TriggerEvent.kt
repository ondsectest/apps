package com.calmcontrol.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * One logged emotional moment.
 *
 * There is deliberately no separate domain model. The entity is small, stable and carries no
 * persistence-only fields, so a mapping layer would be pure ceremony at this size. If the schema
 * ever grows storage-specific concerns, that is the moment to split it.
 */
@Entity(
    tableName = "trigger_events",
    indices = [Index("epoch_millis")],
)
data class TriggerEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    /** Every report query is a range scan over this column, hence the index. */
    @ColumnInfo(name = "epoch_millis")
    val epochMillis: Long,

    val outcome: Outcome,

    val category: TriggerCategory,

    /** Optional 1..5 self-rated intensity. Not charted yet; captured for later. */
    val intensity: Int? = null,

    val note: String? = null,
) {
    fun localDate(zone: ZoneId): LocalDate =
        Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()
}

/** The two things that can happen in a triggered moment. */
enum class Outcome {
    /** GREEN. The user felt the trigger and chose a calm response. */
    CONTROLLED,

    /** RED. Anger was expressed. Recorded as information, never as a verdict. */
    ANGER_EXPRESSED,
}

/**
 * What set the moment off. [label] is what the user reads; [phrase] slots into generated
 * sentences like "You controlled traffic-related frustration 80% of the time."
 */
enum class TriggerCategory(val label: String, val phrase: String) {
    FAMILY("Family", "family-related"),
    WORK("Work", "work-related"),
    TRAFFIC("Traffic", "traffic-related"),
    MONEY("Money", "money-related"),
    HEALTH("Health", "health-related"),
    SELF("Self", "self-directed"),
    OTHER("Other", "other"),
}
