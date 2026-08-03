package com.surestep.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One intentional record of a completed task.
 *
 * There is deliberately no foreign key onto [TaskEntity]. A log is evidence that
 * something happened; deleting or renaming the task later must not rewrite or
 * erase that evidence, so the task's title is snapshotted here at record time.
 */
@Entity(
    tableName = "task_logs",
    indices = [
        Index("localDate"),
        Index("taskId"),
        Index("recordedAtMillis"),
        Index(value = ["localDate", "taskId"]),
    ],
)
data class TaskLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: Long,
    /** Title as it read when the record was made. */
    val taskTitle: String,
    val iconKey: String,
    val colorArgb: Long,

    /** Epoch millis, including the millisecond component the spec asks for. */
    val recordedAtMillis: Long,
    /** IANA zone id active on the device at record time, e.g. "Asia/Kolkata". */
    val zoneId: String,
    /** Local calendar day as ISO "yyyy-MM-dd". Indexed: every day/calendar query keys off it. */
    val localDate: String,

    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracyMeters: Float? = null,
    val address: String? = null,

    val deviceModel: String? = null,
    val batteryPercent: Int? = null,
    val networkSummary: String? = null,

    /** Absolute path inside app-private storage. Null when photo capture is off or failed. */
    val photoPath: String? = null,
    val notes: String? = null,
)
