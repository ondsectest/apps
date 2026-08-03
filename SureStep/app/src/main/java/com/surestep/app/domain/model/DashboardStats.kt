package com.surestep.app.domain.model

data class DashboardStats(
    val completedToday: Int = 0,
    val totalToday: Int = 0,
    val streakDays: Int = 0,
    val missedYesterday: Int = 0,
) {
    val remainingToday: Int get() = (totalToday - completedToday).coerceAtLeast(0)
    val completionPercent: Int
        get() = if (totalToday == 0) 0 else (completedToday * 100) / totalToday
}
