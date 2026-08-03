package com.surestep.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import java.time.LocalDate

object Routes {
    const val HOME = "home"
    const val HISTORY = "history"
    const val CALENDAR = "calendar"
    const val SETTINGS = "settings"

    const val CAPTURE = "capture"
    const val MANAGE_TASKS = "manage_tasks"
    const val REMINDERS = "reminders"

    private const val LOG_DETAIL_BASE = "log"
    const val LOG_DETAIL = "$LOG_DETAIL_BASE/{logId}"
    fun logDetail(logId: Long) = "$LOG_DETAIL_BASE/$logId"

    private const val DAY_BASE = "day"
    const val DAY_DETAIL = "$DAY_BASE/{date}"
    fun dayDetail(date: LocalDate) = "$DAY_BASE/$date"

    const val ARG_LOG_ID = "logId"
    const val ARG_DATE = "date"
}

enum class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    HOME(Routes.HOME, "Home", Icons.Filled.Checklist),
    HISTORY(Routes.HISTORY, "History", Icons.Filled.History),
    CALENDAR(Routes.CALENDAR, "Calendar", Icons.Filled.CalendarMonth),
    SETTINGS(Routes.SETTINGS, "Settings", Icons.Filled.Settings),
}
