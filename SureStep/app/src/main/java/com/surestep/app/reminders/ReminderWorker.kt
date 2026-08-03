package com.surestep.app.reminders

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.surestep.app.data.local.dao.ReminderDao
import com.surestep.app.data.prefs.SettingsRepository
import com.surestep.app.data.repository.LogRepository
import com.surestep.app.data.repository.TaskRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val reminderDao: ReminderDao,
    private val taskRepository: TaskRepository,
    private val logRepository: LogRepository,
    private val settingsRepository: SettingsRepository,
    private val notificationHelper: NotificationHelper,
    private val reminderScheduler: ReminderScheduler,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val reminderId = inputData.getLong(KEY_REMINDER_ID, -1L)
        val reminder = reminderDao.getById(reminderId) ?: return Result.success()

        // Re-arm first, so a failure further down cannot silently end the series.
        reminderScheduler.schedule(reminder)

        if (!reminder.enabled) return Result.success()
        if (!settingsRepository.current().notificationsEnabled) return Result.success()

        val today = LocalDate.now()
        val activeTasks = taskRepository.activeTasks.first()
        val recordedIds = logRepository.observeCompletedTaskIdsToday(today).first()
        val outstanding = activeTasks.filter { it.id !in recordedIds }

        // Everything is already recorded — say nothing. A reminder that fires
        // when there is nothing to do teaches the user to re-check for no
        // reason, which is exactly the habit this app exists to replace.
        if (outstanding.isEmpty()) return Result.success()

        notificationHelper.postReminder(
            reminderId = reminder.id,
            title = reminder.label,
            body = bodyFor(outstanding.map { it.title }),
        )
        return Result.success()
    }

    private fun bodyFor(titles: List<String>): String {
        val listed = when (titles.size) {
            1 -> "\"${titles.first()}\" isn't marked yet today."
            2 -> "\"${titles[0]}\" and \"${titles[1]}\" aren't marked yet today."
            else -> "${titles.size} tasks aren't marked yet today, including \"${titles.first()}\"."
        }
        return "$listed If you've already done it, you can record it now."
    }

    companion object {
        const val KEY_REMINDER_ID = "reminder_id"
    }
}
