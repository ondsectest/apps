package com.surestep.app.ui.settings

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.surestep.app.capture.PhotoStore
import com.surestep.app.data.prefs.AppSettings
import com.surestep.app.data.prefs.SettingsRepository
import com.surestep.app.data.prefs.ThemeMode
import com.surestep.app.data.repository.LogRepository
import com.surestep.app.data.repository.ReminderRepository
import com.surestep.app.export.ExportFormat
import com.surestep.app.export.ExportManager
import com.surestep.app.export.ExportResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

sealed interface SettingsEvent {
    data class Exported(val result: ExportResult, val format: ExportFormat) : SettingsEvent
    data class Message(val text: String) : SettingsEvent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val logRepository: LogRepository,
    private val reminderRepository: ReminderRepository,
    private val exportManager: ExportManager,
    private val photoStore: PhotoStore,
) : ViewModel() {

    private val events = Channel<SettingsEvent>(Channel.BUFFERED)
    val eventFlow: Flow<SettingsEvent> = events.receiveAsFlow()

    val settings: StateFlow<AppSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    val recordCount: StateFlow<Int> = logRepository.totalRecordCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun setCaptureSelfie(enabled: Boolean) = update { setCaptureSelfie(enabled) }
    fun setRecordLocation(enabled: Boolean) = update { setRecordLocation(enabled) }
    fun setRecordBattery(enabled: Boolean) = update { setRecordBattery(enabled) }
    fun setReverseGeocode(enabled: Boolean) = update { setReverseGeocode(enabled) }
    fun setHighContrast(enabled: Boolean) = update { setHighContrast(enabled) }
    fun setThemeMode(mode: ThemeMode) = update { setThemeMode(mode) }
    fun setCountdownSeconds(seconds: Int) = update { setCountdownSeconds(seconds) }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotificationsEnabled(enabled)
            // Turning notifications off should actually stop them, not just hide
            // a switch, so the scheduled work is cancelled too.
            if (enabled) reminderRepository.rescheduleAll() else reminderRepository.cancelAll()
        }
    }

    fun setPin(pin: String) {
        viewModelScope.launch {
            settingsRepository.setPin(pin)
            events.send(SettingsEvent.Message("PIN set"))
        }
    }

    fun clearPin() {
        viewModelScope.launch {
            settingsRepository.clearPin()
            events.send(SettingsEvent.Message("PIN removed"))
        }
    }

    fun setBiometricEnabled(enabled: Boolean) = update { setBiometricEnabled(enabled) }

    fun export(format: ExportFormat) {
        viewModelScope.launch {
            events.send(SettingsEvent.Exported(exportManager.export(format), format))
        }
    }

    /** Share sheet for a finished export. Sharing is always the user's move. */
    fun shareIntent(file: File, format: ExportFormat): Intent =
        exportManager.shareIntent(file, format)

    fun deleteAllHistory() {
        viewModelScope.launch {
            logRepository.deleteAll()
            withContext(Dispatchers.IO) { photoStore.deleteOrphans(emptySet()) }
            exportManager.clearExports()
            events.send(SettingsEvent.Message("All records deleted"))
        }
    }

    suspend fun photoStorageBytes(): Long = withContext(Dispatchers.IO) { photoStore.usedBytes() }

    private fun update(block: suspend SettingsRepository.() -> Unit) {
        viewModelScope.launch { settingsRepository.block() }
    }
}
