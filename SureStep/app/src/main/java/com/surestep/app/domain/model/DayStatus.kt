package com.surestep.app.domain.model

/** Calendar colour coding for a single day. */
enum class DayStatus {
    /** No checklist existed / day is in the future — render neutral. */
    NONE,

    /** Nothing recorded on a day that had tasks. */
    MISSED,

    /** Some, but not all, tasks recorded. */
    PARTIAL,

    /** Every active task has a record. */
    COMPLETE,
}

data class DaySummary(
    val completed: Int,
    val total: Int,
    val status: DayStatus,
) {
    val percent: Int get() = if (total == 0) 0 else (completed * 100) / total
}
