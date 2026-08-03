package com.surestep.app.data.local

import com.surestep.app.data.local.entity.TaskEntity
import com.surestep.app.domain.model.TaskGroup
import com.surestep.app.ui.theme.TaskPalette

/**
 * The checklist a first-time user sees. Every one of these is editable or
 * removable — it is a starting point, not a prescription.
 */
object DefaultTasks {

    fun build(now: Long): List<TaskEntity> {
        val seed = listOf(
            Triple("Lock Door", "lock", TaskGroup.HOME),
            Triple("Turn Off Gas", "gas", TaskGroup.HOME),
            Triple("Turn Off Lights", "lights", TaskGroup.HOME),
            Triple("Carry Wallet", "wallet", TaskGroup.OFFICE),
            Triple("Carry Keys", "keys", TaskGroup.OFFICE),
            Triple("Carry Phone", "phone", TaskGroup.OFFICE),
            Triple("Carry Laptop", "laptop", TaskGroup.OFFICE),
            Triple("Carry Charger", "charger", TaskGroup.OFFICE),
            Triple("Carry Medicines", "medicine", TaskGroup.HOME),
        )
        return seed.mapIndexed { index, (title, icon, group) ->
            TaskEntity(
                title = title,
                iconKey = icon,
                colorArgb = TaskPalette.forIndex(index),
                groupName = group.name,
                sortOrder = index,
                isActive = true,
                createdAt = now,
            )
        }
    }
}
