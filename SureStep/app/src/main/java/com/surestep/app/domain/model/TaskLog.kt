package com.surestep.app.domain.model

import com.surestep.app.data.local.entity.TaskLogEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

data class TaskLog(
    val id: Long,
    val taskId: Long,
    val taskTitle: String,
    val iconKey: String,
    val colorArgb: Long,
    val recordedAtMillis: Long,
    val zoneId: String,
    val localDate: LocalDate,
    val latitude: Double?,
    val longitude: Double?,
    val accuracyMeters: Float?,
    val address: String?,
    val deviceModel: String?,
    val batteryPercent: Int?,
    val networkSummary: String?,
    val photoPath: String?,
    val notes: String?,
) {
    val hasLocation: Boolean get() = latitude != null && longitude != null

    /**
     * The moment as it was experienced, in the zone the device was in at the
     * time — not the reader's current zone. A record made in IST still reads as
     * IST after the user flies somewhere else.
     */
    val recordedAt: ZonedDateTime
        get() = Instant.ofEpochMilli(recordedAtMillis)
            .atZone(runCatching { ZoneId.of(zoneId) }.getOrElse { ZoneId.systemDefault() })
}

fun TaskLogEntity.toDomain(): TaskLog = TaskLog(
    id = id,
    taskId = taskId,
    taskTitle = taskTitle,
    iconKey = iconKey,
    colorArgb = colorArgb,
    recordedAtMillis = recordedAtMillis,
    zoneId = zoneId,
    localDate = runCatching { LocalDate.parse(localDate) }.getOrElse { LocalDate.now() },
    latitude = latitude,
    longitude = longitude,
    accuracyMeters = accuracyMeters,
    address = address,
    deviceModel = deviceModel,
    batteryPercent = batteryPercent,
    networkSummary = networkSummary,
    photoPath = photoPath,
    notes = notes,
)
