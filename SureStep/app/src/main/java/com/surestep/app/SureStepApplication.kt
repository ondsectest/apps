package com.surestep.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.surestep.app.data.repository.ReminderRepository
import com.surestep.app.data.repository.TaskRepository
import com.surestep.app.di.ApplicationScope
import com.surestep.app.reminders.NotificationHelper
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class SureStepApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var taskRepository: TaskRepository
    @Inject lateinit var reminderRepository: ReminderRepository
    @Inject lateinit var notificationHelper: NotificationHelper

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        notificationHelper.ensureChannel()

        // Startup work is off the main thread so cold start stays fast; the
        // checklist renders from Room the moment the seed commits.
        applicationScope.launch {
            taskRepository.seedIfEmpty(System.currentTimeMillis())
            reminderRepository.rescheduleAll()
        }
    }
}
