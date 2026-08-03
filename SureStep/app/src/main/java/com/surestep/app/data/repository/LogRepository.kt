package com.surestep.app.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.surestep.app.data.local.dao.DayCompletion
import com.surestep.app.data.local.dao.TaskLogDao
import com.surestep.app.data.local.entity.TaskLogEntity
import com.surestep.app.domain.CompletionRules
import com.surestep.app.domain.model.DashboardStats
import com.surestep.app.domain.model.DaySummary
import com.surestep.app.domain.model.TaskLog
import com.surestep.app.domain.model.toDomain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogRepository @Inject constructor(
    private val taskLogDao: TaskLogDao,
    private val taskRepository: TaskRepository,
) {
    val totalRecordCount: Flow<Int> = taskLogDao.observeTotalCount()

    fun pagedLogs(query: String): Flow<PagingData<TaskLog>> = Pager(
        config = PagingConfig(
            pageSize = 30,
            initialLoadSize = 60,
            prefetchDistance = 15,
            enablePlaceholders = false,
        ),
        pagingSourceFactory = { taskLogDao.pagedLogs(query.trim()) },
    ).flow.map { paging -> paging.map(TaskLogEntity::toDomain) }

    fun observeById(id: Long): Flow<TaskLog?> =
        taskLogDao.observeById(id).map { it?.toDomain() }

    fun observeForDate(date: LocalDate): Flow<List<TaskLog>> =
        taskLogDao.observeForDate(date.toString())
            .map { list -> list.map(TaskLogEntity::toDomain) }

    fun observeCompletedTaskIdsToday(today: LocalDate): Flow<Set<Long>> =
        taskLogDao.observeCompletedTaskIds(today.toString()).map { it.toSet() }

    /** Per-day summaries for a calendar month (or any range). */
    fun observeDaySummaries(
        start: LocalDate,
        end: LocalDate,
        today: LocalDate,
    ): Flow<Map<LocalDate, DaySummary>> = combine(
        taskLogDao.observeCompletionBetween(start.toString(), end.toString()),
        taskRepository.activeTaskCount,
        taskRepository.checklistStartDate,
    ) { completions, activeCount, checklistStart ->
        val byDate = completions.associateBy(DayCompletion::localDate)
        generateSequence(start) { day -> day.plusDays(1).takeIf { !it.isAfter(end) } }
            .associateWith { day ->
                val completed = byDate[day.toString()]?.completedTasks ?: 0
                DaySummary(
                    completed = completed,
                    total = activeCount,
                    status = CompletionRules.statusFor(
                        completed = completed,
                        total = activeCount,
                        day = day,
                        today = today,
                        checklistStart = checklistStart,
                    ),
                )
            }
    }

    val dashboardStats: Flow<DashboardStats> = flow {
        // The window start is fixed per collection; the end is open so a session
        // left running past midnight still picks up the new day's records.
        val windowStart = LocalDate.now().minusDays(STREAK_WINDOW_DAYS.toLong()).toString()
        emitAll(
            combine(
                taskLogDao.observeCompletionBetween(windowStart, OPEN_ENDED_DATE),
                taskRepository.activeTaskCount,
                taskRepository.checklistStartDate,
            ) { completions, activeCount, checklistStart ->
                val now = LocalDate.now()
                val yesterday = now.minusDays(1)
                val byDate = completions.associate { it.localDate to it.completedTasks }
                val yesterdayCompleted = byDate[yesterday.toString()] ?: 0

                // Nothing counts as missed before the checklist existed. A fresh
                // install should not open on a tally of failures for days its
                // owner never saw.
                DashboardStats(
                    completedToday = byDate[now.toString()] ?: 0,
                    totalToday = activeCount,
                    streakDays = CompletionRules.streak(
                        completedByDate = byDate,
                        today = now,
                        activeTaskCount = activeCount,
                        maxDays = STREAK_WINDOW_DAYS,
                    ),
                    missedYesterday = CompletionRules.missedOn(
                        day = yesterday,
                        completed = yesterdayCompleted,
                        activeTaskCount = activeCount,
                        checklistStart = checklistStart,
                    ),
                )
            },
        )
    }

    suspend fun insert(entity: TaskLogEntity): Long = taskLogDao.insert(entity)

    suspend fun updateNotes(id: Long, notes: String?) {
        val existing = taskLogDao.getById(id) ?: return
        taskLogDao.update(existing.copy(notes = notes?.takeIf { it.isNotBlank() }))
    }

    suspend fun getAllForExport(): List<TaskLog> =
        taskLogDao.getAllForExport().map(TaskLogEntity::toDomain)

    /** Deletes the record and the photo file it owns. */
    suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        taskLogDao.getPhotoPath(id)?.let { path -> File(path).delete() }
        taskLogDao.deleteById(id)
    }

    suspend fun deleteAll() = withContext(Dispatchers.IO) {
        taskLogDao.getAllPhotoPaths().forEach { path -> File(path).delete() }
        taskLogDao.deleteAll()
    }

    private companion object {
        const val STREAK_WINDOW_DAYS = 365

        /** Upper bound that no real ISO date can exceed — keeps the range query open-ended. */
        const val OPEN_ENDED_DATE = "9999-12-31"
    }
}
