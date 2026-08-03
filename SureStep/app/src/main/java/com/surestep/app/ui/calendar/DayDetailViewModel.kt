package com.surestep.app.ui.calendar

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.surestep.app.data.repository.LogRepository
import com.surestep.app.data.repository.TaskRepository
import com.surestep.app.domain.model.Task
import com.surestep.app.domain.model.TaskLog
import com.surestep.app.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class DayDetailUiState(
    val date: LocalDate = LocalDate.now(),
    val logs: List<TaskLog> = emptyList(),
    /** Tasks on the current checklist with no record on this day. */
    val unrecorded: List<Task> = emptyList(),
    val loading: Boolean = true,
)

@HiltViewModel
class DayDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    logRepository: LogRepository,
    taskRepository: TaskRepository,
) : ViewModel() {

    private val date: LocalDate = savedStateHandle.get<String>(Routes.ARG_DATE)
        ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        ?: LocalDate.now()

    val uiState: StateFlow<DayDetailUiState> = combine(
        logRepository.observeForDate(date),
        taskRepository.activeTasks,
    ) { logs, tasks ->
        val recordedTaskIds = logs.map { it.taskId }.toSet()
        DayDetailUiState(
            date = date,
            logs = logs,
            // Only meaningful for past and present days; a task created after
            // this date is not counted as missed on it.
            unrecorded = tasks.filter { task ->
                task.id !in recordedTaskIds && !task.createdOn().isAfter(date)
            },
            loading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DayDetailUiState(date = date),
    )

    private fun Task.createdOn(): LocalDate =
        Instant.ofEpochMilli(createdAt).atZone(ZoneId.systemDefault()).toLocalDate()
}
