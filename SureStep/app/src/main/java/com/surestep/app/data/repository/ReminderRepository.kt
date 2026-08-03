package com.surestep.app.data.repository

import com.surestep.app.data.local.dao.ReminderDao
import com.surestep.app.data.local.entity.ReminderEntity
import com.surestep.app.reminders.ReminderScheduler
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepository @Inject constructor(
    private val reminderDao: ReminderDao,
    private val reminderScheduler: ReminderScheduler,
) {
    val reminders: Flow<List<ReminderEntity>> = reminderDao.observeAll()

    suspend fun add(label: String, hour: Int, minute: Int, daysOfWeekMask: Int): Long {
        val id = reminderDao.insert(
            ReminderEntity(
                label = label.trim().ifBlank { "Checklist reminder" },
                hour = hour,
                minute = minute,
                daysOfWeekMask = daysOfWeekMask,
                enabled = true,
            ),
        )
        reminderDao.getById(id)?.let(reminderScheduler::schedule)
        return id
    }

    suspend fun update(reminder: ReminderEntity) {
        reminderDao.update(reminder)
        reminderScheduler.schedule(reminder)
    }

    suspend fun setEnabled(reminder: ReminderEntity, enabled: Boolean) =
        update(reminder.copy(enabled = enabled))

    suspend fun delete(reminder: ReminderEntity) {
        reminderScheduler.cancel(reminder.id)
        reminderDao.delete(reminder)
    }

    /** Cancels every scheduled firing without deleting the user's reminders. */
    suspend fun cancelAll() {
        reminderDao.getEnabled().forEach { reminderScheduler.cancel(it.id) }
    }

    suspend fun rescheduleAll() = reminderScheduler.rescheduleAll()
}
