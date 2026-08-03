package com.surestep.app.reminders

import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.surestep.app.data.local.dao.ReminderDao
import com.surestep.app.data.local.entity.ReminderEntity
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(
    private val workManager: WorkManager,
    private val reminderDao: ReminderDao,
) {
    /**
     * Reminders are scheduled one firing at a time rather than as a periodic
     * request. WorkManager's periodic minimum is 15 minutes and its window
     * drifts; a chain of one-shots re-armed after each firing keeps "8:30 AM"
     * meaning 8:30 AM, and correctly skips days the user did not select.
     */
    fun schedule(reminder: ReminderEntity, from: ZonedDateTime = ZonedDateTime.now()) {
        cancel(reminder.id)
        if (!reminder.enabled || reminder.daysOfWeekMask == 0) return

        val next = nextOccurrence(reminder, from) ?: return
        val delay = Duration.between(from, next).toMillis().coerceAtLeast(0)

        workManager.enqueueUniqueWork(
            workName(reminder.id),
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(Data.Builder().putLong(ReminderWorker.KEY_REMINDER_ID, reminder.id).build())
                .addTag(TAG)
                .build(),
        )
    }

    fun cancel(reminderId: Long) {
        workManager.cancelUniqueWork(workName(reminderId))
    }

    /** Re-arms every enabled reminder. Called on boot, app update, and app start. */
    suspend fun rescheduleAll() {
        val now = ZonedDateTime.now()
        reminderDao.getEnabled().forEach { reminder -> schedule(reminder, now) }
    }

    private fun nextOccurrence(reminder: ReminderEntity, from: ZonedDateTime): ZonedDateTime? =
        ReminderSchedule.nextOccurrence(
            daysOfWeekMask = reminder.daysOfWeekMask,
            hour = reminder.hour,
            minute = reminder.minute,
            from = from,
        )

    private fun workName(reminderId: Long) = "$WORK_PREFIX$reminderId"

    companion object {
        const val TAG = "surestep_reminder"
        private const val WORK_PREFIX = "surestep_reminder_"
    }
}
