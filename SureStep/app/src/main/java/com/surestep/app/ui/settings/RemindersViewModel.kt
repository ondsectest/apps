package com.surestep.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.surestep.app.data.local.entity.ReminderEntity
import com.surestep.app.data.repository.ReminderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RemindersViewModel @Inject constructor(
    private val reminderRepository: ReminderRepository,
) : ViewModel() {

    val reminders: StateFlow<List<ReminderEntity>> = reminderRepository.reminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun add(label: String, hour: Int, minute: Int, daysMask: Int) {
        viewModelScope.launch { reminderRepository.add(label, hour, minute, daysMask) }
    }

    fun setEnabled(reminder: ReminderEntity, enabled: Boolean) {
        viewModelScope.launch { reminderRepository.setEnabled(reminder, enabled) }
    }

    fun delete(reminder: ReminderEntity) {
        viewModelScope.launch { reminderRepository.delete(reminder) }
    }

    companion object {
        const val EVERY_DAY_MASK = 0b1111111
        const val WEEKDAYS_MASK = 0b0011111
    }
}
