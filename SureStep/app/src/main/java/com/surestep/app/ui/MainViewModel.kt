package com.surestep.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.surestep.app.data.prefs.AppSettings
import com.surestep.app.data.prefs.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class MainUiState(
    val settings: AppSettings = AppSettings(),
    val settingsLoaded: Boolean = false,
    val unlocked: Boolean = false,
) {
    val showLockScreen: Boolean get() = settingsLoaded && settings.pinEnabled && !unlocked
    val ready: Boolean get() = settingsLoaded
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val unlocked = MutableStateFlow(false)

    val uiState: StateFlow<MainUiState> =
        combine(settingsRepository.settings, unlocked.asStateFlow()) { settings, isUnlocked ->
            MainUiState(settings = settings, settingsLoaded = true, unlocked = isUnlocked)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MainUiState(),
        )

    fun unlock() {
        unlocked.value = true
    }

    /**
     * Called when the app leaves the foreground. Re-locking on background is the
     * only behaviour that makes a PIN meaningful — records include selfies and
     * locations, and the phone gets handed around.
     */
    fun lock() {
        unlocked.value = false
    }

    suspend fun verifyPin(pin: String): Boolean = settingsRepository.verifyPin(pin)
}
