package com.surestep.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
    indices = [Index("sortOrder"), Index("isActive")],
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    /** Key into [com.surestep.app.ui.components.TaskIcons]; falls back to a default if unknown. */
    val iconKey: String,
    /** Accent colour as an ARGB value. Stored as Long because Room has no unsigned types. */
    val colorArgb: Long,
    /** Name of a [com.surestep.app.domain.model.TaskGroup]. */
    val groupName: String,
    val sortOrder: Int,
    @ColumnInfo(defaultValue = "1")
    val isActive: Boolean = true,
    val createdAt: Long,
)
