package com.surestep.app.camera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.surestep.app.capture.CaptureCoordinator
import com.surestep.app.data.prefs.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class CaptureUiState(
    val taskTitle: String? = null,
    val countdownSeconds: Int = 3,
    val ready: Boolean = false,
    /** True when there is nothing in flight — the screen should just close. */
    val nothingToCapture: Boolean = false,
)

@HiltViewModel
class CaptureViewModel @Inject constructor(
    private val captureCoordinator: CaptureCoordinator,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CaptureUiState())
    val uiState: StateFlow<CaptureUiState> = _uiState.asStateFlow()

    /** Set once the record is written, so teardown cannot write it a second time. */
    private var finalised = false

    init {
        viewModelScope.launch {
            val settings = settingsRepository.current()
            val title = captureCoordinator.pendingTaskTitle.value
            _uiState.value = CaptureUiState(
                taskTitle = title,
                countdownSeconds = settings.countdownSeconds,
                ready = title != null,
                nothingToCapture = title == null,
            )
        }
    }

    fun targetFile(): File? = captureCoordinator.pendingPhotoFile()

    fun onPhotoCaptured(onDone: () -> Unit) {
        if (finalised) return
        finalised = true
        viewModelScope.launch {
            captureCoordinator.completeWithPhoto()
            onDone()
        }
    }

    /**
     * The camera could not deliver a photo, or the user left. The record is
     * saved anyway — the tap was the intention, and losing it would be worse
     * than losing the selfie.
     */
    fun onPhotoSkipped(onDone: () -> Unit) {
        if (finalised) return
        finalised = true
        viewModelScope.launch {
            captureCoordinator.completeWithoutPhoto()
            onDone()
        }
    }
}
