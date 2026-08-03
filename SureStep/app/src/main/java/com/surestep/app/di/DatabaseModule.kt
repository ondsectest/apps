package com.surestep.app.di

import android.content.Context
import androidx.room.Room
import com.surestep.app.data.local.SureStepDatabase
import com.surestep.app.data.local.dao.ReminderDao
import com.surestep.app.data.local.dao.TaskDao
import com.surestep.app.data.local.dao.TaskLogDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SureStepDatabase =
        Room.databaseBuilder(context, SureStepDatabase::class.java, SureStepDatabase.NAME)
            // No destructive fallback: these records are the whole point of the
            // app, so a missing migration must fail loudly in development rather
            // than quietly wipe a user's history in production.
            .build()

    @Provides
    fun provideTaskDao(database: SureStepDatabase): TaskDao = database.taskDao()

    @Provides
    fun provideTaskLogDao(database: SureStepDatabase): TaskLogDao = database.taskLogDao()

    @Provides
    fun provideReminderDao(database: SureStepDatabase): ReminderDao = database.reminderDao()
}
