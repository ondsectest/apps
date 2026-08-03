package com.surestep.app.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.surestep.app.data.repository.TaskRepository
import com.surestep.app.domain.model.Task
import com.surestep.app.domain.model.TaskGroup
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ManageTasksUiState(
    val active: List<Task> = emptyList(),
    val archived: List<Task> = emptyList(),
)

@HiltViewModel
class ManageTasksViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
) : ViewModel() {

    val uiState: StateFlow<ManageTasksUiState> = taskRepository.allTasks
        .map { tasks ->
            ManageTasksUiState(
                active = tasks.filter { it.isActive },
                archived = tasks.filterNot { it.isActive },
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ManageTasksUiState())

    fun add(title: String, iconKey: String, colorArgb: Long, group: TaskGroup) {
        viewModelScope.launch {
            taskRepository.addTask(title, iconKey, colorArgb, group, System.currentTimeMillis())
        }
    }

    fun update(task: Task, title: String, iconKey: String, colorArgb: Long, group: TaskGroup) {
        viewModelScope.launch {
            taskRepository.updateTask(task.id, title, iconKey, colorArgb, group)
        }
    }

    /**
     * Removing a task takes it off the checklist but leaves every record it
     * produced in History — the record is a statement about the past.
     */
    fun archive(task: Task) {
        viewModelScope.launch { taskRepository.archiveTask(task.id) }
    }

    fun restore(task: Task) {
        viewModelScope.launch { taskRepository.restoreTask(task.id) }
    }

    fun move(task: Task, direction: Int) {
        val ordered = uiState.value.active.toMutableList()
        val index = ordered.indexOfFirst { it.id == task.id }
        val target = index + direction
        if (index < 0 || target !in ordered.indices) return
        ordered.add(target, ordered.removeAt(index))
        viewModelScope.launch { taskRepository.reorder(ordered.map { it.id }) }
    }
}
