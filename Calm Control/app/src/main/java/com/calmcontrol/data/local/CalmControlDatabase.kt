package com.calmcontrol.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [TriggerEvent::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class CalmControlDatabase : RoomDatabase() {

    abstract fun triggerEventDao(): TriggerEventDao

    companion object {
        @Volatile
        private var instance: CalmControlDatabase? = null

        fun get(context: Context): CalmControlDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    CalmControlDatabase::class.java,
                    "calm_control.db",
                ).build().also { instance = it }
            }
    }
}
