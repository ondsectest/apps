package com.surestep.app.ui.history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.surestep.app.data.repository.LogRepository
import com.surestep.app.domain.model.TaskLog
import com.surestep.app.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val logRepository: LogRepository,
) : ViewModel() {

    private val logId: Long = savedStateHandle.get<Long>(Routes.ARG_LOG_ID) ?: -1L

    val log: StateFlow<TaskLog?> = logRepository.observeById(logId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun saveNotes(notes: String) {
        viewModelScope.launch { logRepository.updateNotes(logId, notes) }
    }

    fun deleteRecord(onDeleted: () -> Unit) {
        viewModelScope.launch {
            logRepository.delete(logId)
            onDeleted()
        }
    }
}
