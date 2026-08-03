package com.surestep.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val label: String,
    val hour: Int,
    val minute: Int,
    /** Bit i (0 = Monday … 6 = Sunday) set means the reminder fires that day. */
    val daysOfWeekMask: Int,
    val enabled: Boolean = true,
)
