package com.surestep.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.surestep.app.data.local.dao.ReminderDao
import com.surestep.app.data.local.dao.TaskDao
import com.surestep.app.data.local.dao.TaskLogDao
import com.surestep.app.data.local.entity.ReminderEntity
import com.surestep.app.data.local.entity.TaskEntity
import com.surestep.app.data.local.entity.TaskLogEntity

@Database(
    entities = [TaskEntity::class, TaskLogEntity::class, ReminderEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class SureStepDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun taskLogDao(): TaskLogDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        const val NAME = "surestep.db"
    }
}
