package com.surestep.app.domain.model

import com.surestep.app.data.local.entity.TaskEntity

data class Task(
    val id: Long,
    val title: String,
    val iconKey: String,
    val colorArgb: Long,
    val group: TaskGroup,
    val sortOrder: Int,
    val isActive: Boolean,
    val createdAt: Long,
)

fun TaskEntity.toDomain(): Task = Task(
    id = id,
    title = title,
    iconKey = iconKey,
    colorArgb = colorArgb,
    group = TaskGroup.fromName(groupName),
    sortOrder = sortOrder,
    isActive = isActive,
    createdAt = createdAt,
)

fun Task.toEntity(): TaskEntity = TaskEntity(
    id = id,
    title = title,
    iconKey = iconKey,
    colorArgb = colorArgb,
    groupName = group.name,
    sortOrder = sortOrder,
    isActive = isActive,
    createdAt = createdAt,
)
