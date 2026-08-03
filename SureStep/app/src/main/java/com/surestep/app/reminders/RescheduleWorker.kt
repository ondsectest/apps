package com.surestep.app.reminders

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/** Re-arms every reminder after a reboot or an app update. */
@HiltWorker
class RescheduleWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val reminderScheduler: ReminderScheduler,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        reminderScheduler.rescheduleAll()
        return Result.success()
    }
}
