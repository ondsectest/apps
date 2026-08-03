package com.surestep.app.capture

import com.surestep.app.data.local.entity.TaskLogEntity
import com.surestep.app.data.prefs.SettingsRepository
import com.surestep.app.data.repository.LogRepository
import com.surestep.app.di.ApplicationScope
import com.surestep.app.domain.model.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

/** What the UI should do next after a confirmation button is pressed. */
sealed interface CaptureStart {
    /** Photo capture is on and available — show the camera with this countdown. */
    data class NeedsSelfie(val countdownSeconds: Int) : CaptureStart

    /** Everything needed was gathered without the camera; the record already exists. */
    data class Recorded(val logId: Long) : CaptureStart
}

data class CaptureResult(
    val logId: Long,
    val taskTitle: String,
    val hasPhoto: Boolean,
)

/**
 * State carried between the confirmation tap and the camera returning.
 *
 * The timestamp is fixed at the moment of the tap, not the moment the shutter
 * fires — the record should say when the user confirmed the task, and the
 * couple of seconds of countdown are an implementation detail.
 */
private data class PendingCapture(
    val task: Task,
    val at: ZonedDateTime,
    val deviceModel: String,
    val batteryPercent: Int?,
    val networkSummary: String,
    val location: Deferred<ResolvedLocation?>?,
    val photoFile: File,
)

private data class ResolvedLocation(
    val location: CapturedLocation,
    val address: String?,
)

/**
 * Owns the confirmation pipeline end to end: gather metadata, optionally take a
 * selfie, write exactly one record.
 *
 * It is application-scoped on purpose. If the user rotates the device or the
 * camera screen is recreated mid-countdown, the pending capture survives, and a
 * record is written either way — the one thing this app must never do is drop a
 * confirmation the user already made.
 */
@Singleton
class CaptureCoordinator @Inject constructor(
    private val logRepository: LogRepository,
    private val settingsRepository: SettingsRepository,
    private val deviceInfoProvider: DeviceInfoProvider,
    private val locationProvider: LocationProvider,
    private val photoStore: PhotoStore,
    @ApplicationScope private val appScope: CoroutineScope,
) {
    private val _pendingTaskTitle = MutableStateFlow<String?>(null)
    val pendingTaskTitle: StateFlow<String?> = _pendingTaskTitle.asStateFlow()

    private val _results = MutableSharedFlow<CaptureResult>(extraBufferCapacity = 8)
    val results: SharedFlow<CaptureResult> = _results.asSharedFlow()

    private var pending: PendingCapture? = null

    /**
     * Called the instant the user taps the confirmation button. Metadata that is
     * cheap to read is captured synchronously; the location fix runs in the
     * background while the camera countdown plays, so it costs the user nothing.
     */
    suspend fun begin(task: Task, cameraAvailable: Boolean): CaptureStart {
        val settings = settingsRepository.current()
        val at = ZonedDateTime.now()

        val locationJob = if (settings.recordLocation && locationProvider.hasPermission()) {
            appScope.async { resolveLocation(settings.reverseGeocode) }
        } else {
            null
        }

        val capture = PendingCapture(
            task = task,
            at = at,
            deviceModel = deviceInfoProvider.deviceModel(),
            batteryPercent = if (settings.recordBattery) deviceInfoProvider.batteryPercent() else null,
            networkSummary = deviceInfoProvider.networkSummary(),
            location = locationJob,
            photoFile = photoStore.newPhotoFile(task.title, at),
        )

        return if (settings.captureSelfie && cameraAvailable) {
            pending = capture
            _pendingTaskTitle.value = task.title
            CaptureStart.NeedsSelfie(settings.countdownSeconds)
        } else {
            CaptureStart.Recorded(finalise(capture, photoFile = null))
        }
    }

    /** Where the camera should write its image, if a capture is in flight. */
    fun pendingPhotoFile(): File? = pending?.photoFile

    /** The selfie was taken. Save the record with it. */
    suspend fun completeWithPhoto() {
        val capture = pending ?: return
        val photo = capture.photoFile.takeIf { it.exists() && it.length() > 0 }
        finalise(capture, photo)
    }

    /**
     * The camera failed, was denied, or the user backed out. The record is still
     * written — the confirmation itself is what matters, and losing it would be
     * the worst possible outcome for someone who taps the button and walks away.
     */
    suspend fun completeWithoutPhoto() {
        val capture = pending ?: return
        photoStore.delete(capture.photoFile.absolutePath)
        finalise(capture, photoFile = null)
    }

    private suspend fun finalise(capture: PendingCapture, photoFile: File?): Long {
        val resolved = runCatching { capture.location?.await() }.getOrNull()

        val logId = logRepository.insert(
            TaskLogEntity(
                taskId = capture.task.id,
                taskTitle = capture.task.title,
                iconKey = capture.task.iconKey,
                colorArgb = capture.task.colorArgb,
                recordedAtMillis = capture.at.toInstant().toEpochMilli(),
                zoneId = capture.at.zone.id,
                localDate = capture.at.toLocalDate().toString(),
                latitude = resolved?.location?.latitude,
                longitude = resolved?.location?.longitude,
                accuracyMeters = resolved?.location?.accuracyMeters,
                address = resolved?.address,
                deviceModel = capture.deviceModel,
                batteryPercent = capture.batteryPercent,
                networkSummary = capture.networkSummary,
                photoPath = photoFile?.absolutePath,
                notes = null,
            ),
        )

        pending = null
        _pendingTaskTitle.value = null
        _results.emit(
            CaptureResult(
                logId = logId,
                taskTitle = capture.task.title,
                hasPhoto = photoFile != null,
            ),
        )
        return logId
    }

    private suspend fun resolveLocation(reverseGeocode: Boolean): ResolvedLocation? {
        val location = locationProvider.currentLocation() ?: return null
        val address = if (reverseGeocode) {
            locationProvider.reverseGeocode(location.latitude, location.longitude)
        } else {
            null
        }
        return ResolvedLocation(location, address)
    }
}
