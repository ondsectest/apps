package com.surestep.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.surestep.app.capture.CaptureCoordinator
import com.surestep.app.capture.CaptureStart
import com.surestep.app.data.local.dao.TaskDayRecord
import com.surestep.app.data.local.dao.TaskLogDao
import com.surestep.app.data.prefs.AppSettings
import com.surestep.app.data.prefs.SettingsRepository
import com.surestep.app.data.repository.LogRepository
import com.surestep.app.data.repository.TaskRepository
import com.surestep.app.domain.model.DashboardStats
import com.surestep.app.domain.model.Task
import com.surestep.app.domain.model.TaskGroup
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/** A checklist row: the task plus whatever has already been recorded for it today. */
data class ChecklistItem(
    val task: Task,
    val recordedAtMillis: Long? = null,
    val recordCount: Int = 0,
    val lastLogId: Long? = null,
) {
    val isRecordedToday: Boolean get() = recordedAtMillis != null
}

data class HomeUiState(
    val items: List<ChecklistItem> = emptyList(),
    val stats: DashboardStats = DashboardStats(),
    val settings: AppSettings = AppSettings(),
    val loading: Boolean = true,
) {
    val grouped: List<Pair<TaskGroup, List<ChecklistItem>>>
        get() = items
            .groupBy { it.task.group }
            .toList()
            .sortedBy { (group, _) -> group.ordinal }
}

sealed interface HomeEvent {
    /** Photo capture is on — hand off to the camera route. */
    data object OpenCamera : HomeEvent

    data class Recorded(val taskTitle: String, val hasPhoto: Boolean) : HomeEvent

    data class Message(val text: String) : HomeEvent
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val logRepository: LogRepository,
    private val settingsRepository: SettingsRepository,
    private val captureCoordinator: CaptureCoordinator,
    taskLogDao: TaskLogDao,
) : ViewModel() {

    private val events = Channel<HomeEvent>(Channel.BUFFERED)
    val eventFlow: Flow<HomeEvent> = events.receiveAsFlow()

    /** Guards against a double-tap producing two records for one intention. */
    private var confirmationInFlight = false

    /**
     * Today's date, re-emitted when the day actually changes. Without this the
     * checklist would keep showing yesterday for anyone who leaves the app open
     * overnight — and would let them tick a task against the wrong day.
     */
    private val today: Flow<LocalDate> = flow {
        while (true) {
            emit(LocalDate.now())
            delay(DAY_CHECK_INTERVAL_MS)
        }
    }.distinctUntilChanged()

    private val dayRecords = today.flatMapLatest { date ->
        taskLogDao.observeDayRecords(date.toString())
    }

    val uiState: StateFlow<HomeUiState> = combine(
        taskRepository.activeTasks,
        dayRecords,
        logRepository.dashboardStats,
        settingsRepository.settings,
    ) { tasks, dayRecords, stats, settings ->
        val recordsByTask: Map<Long, TaskDayRecord> = dayRecords.associateBy { it.taskId }
        HomeUiState(
            items = tasks.map { task ->
                val record = recordsByTask[task.id]
                ChecklistItem(
                    task = task,
                    recordedAtMillis = record?.lastRecordedAtMillis,
                    recordCount = record?.recordCount ?: 0,
                    lastLogId = record?.lastLogId,
                )
            },
            stats = stats,
            settings = settings,
            loading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    init {
        viewModelScope.launch {
            captureCoordinator.results.collect { result ->
                confirmationInFlight = false
                events.send(HomeEvent.Recorded(result.taskTitle, result.hasPhoto))
            }
        }
    }

    /**
     * The confirmation tap. [cameraAvailable] reflects what the UI could
     * actually offer — permission granted and a front camera present — so a
     * denied permission degrades to a timestamp-only record instead of failing.
     */
    fun confirm(task: Task, cameraAvailable: Boolean) {
        if (confirmationInFlight) return
        confirmationInFlight = true
        viewModelScope.launch {
            when (captureCoordinator.begin(task, cameraAvailable)) {
                is CaptureStart.NeedsSelfie -> events.send(HomeEvent.OpenCamera)
                is CaptureStart.Recorded -> Unit // The results collector reports it.
            }
        }
    }

    /** Called if the UI could not hand off to the camera after all. */
    fun releaseConfirmationLock() {
        confirmationInFlight = false
    }

    fun addQuickTask(title: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            taskRepository.addTask(
                title = title,
                iconKey = com.surestep.app.ui.components.TaskIcons.defaultKey,
                colorArgb = com.surestep.app.ui.theme.TaskPalette.forIndex(
                    uiState.value.items.size,
                ),
                group = TaskGroup.CUSTOM,
                now = System.currentTimeMillis(),
            )
            events.send(HomeEvent.Message("Added \"${title.trim()}\""))
        }
    }

    private companion object {
        const val DAY_CHECK_INTERVAL_MS = 60_000L
    }
}
