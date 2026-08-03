package com.surestep.app.data.repository

import com.surestep.app.data.local.DefaultTasks
import com.surestep.app.data.local.dao.TaskDao
import com.surestep.app.data.local.entity.TaskEntity
import com.surestep.app.domain.model.Task
import com.surestep.app.domain.model.TaskGroup
import com.surestep.app.domain.model.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao,
) {
    val activeTasks: Flow<List<Task>> =
        taskDao.observeActive().map { list -> list.map(TaskEntity::toDomain) }

    val allTasks: Flow<List<Task>> =
        taskDao.observeAll().map { list -> list.map(TaskEntity::toDomain) }

    val activeTaskCount: Flow<Int> = taskDao.observeActiveCount()

    /**
     * The first day the checklist existed. Days before this are not "missed" —
     * there was nothing to miss.
     */
    val checklistStartDate: Flow<LocalDate?> = taskDao.observeEarliestCreatedAt()
        .map { millis ->
            millis?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate() }
        }

    /** Seeds the starter checklist the first time the app is opened. */
    suspend fun seedIfEmpty(now: Long) {
        if (taskDao.count() == 0) {
            taskDao.insertAll(DefaultTasks.build(now))
        }
    }

    suspend fun getById(id: Long): Task? = taskDao.getById(id)?.toDomain()

    suspend fun addTask(
        title: String,
        iconKey: String,
        colorArgb: Long,
        group: TaskGroup,
        now: Long,
    ): Long = taskDao.insert(
        TaskEntity(
            title = title.trim(),
            iconKey = iconKey,
            colorArgb = colorArgb,
            groupName = group.name,
            sortOrder = taskDao.nextSortOrder(),
            isActive = true,
            createdAt = now,
        ),
    )

    suspend fun updateTask(
        id: Long,
        title: String,
        iconKey: String,
        colorArgb: Long,
        group: TaskGroup,
    ) {
        val existing = taskDao.getById(id) ?: return
        taskDao.update(
            existing.copy(
                title = title.trim(),
                iconKey = iconKey,
                colorArgb = colorArgb,
                groupName = group.name,
            ),
        )
    }

    /**
     * Removes the task from the checklist but keeps every record it produced.
     * A record is a statement about the past; deleting the task should not
     * retract it.
     */
    suspend fun archiveTask(id: Long) {
        val existing = taskDao.getById(id) ?: return
        taskDao.update(existing.copy(isActive = false))
    }

    suspend fun restoreTask(id: Long) {
        val existing = taskDao.getById(id) ?: return
        taskDao.update(existing.copy(isActive = true))
    }

    suspend fun deleteTaskPermanently(id: Long) {
        val existing = taskDao.getById(id) ?: return
        taskDao.delete(existing)
    }

    suspend fun reorder(idsInOrder: List<Long>) = taskDao.applyOrder(idsInOrder)
}
