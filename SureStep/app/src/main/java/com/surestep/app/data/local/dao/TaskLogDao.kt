package com.surestep.app.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.surestep.app.data.local.entity.TaskLogEntity
import kotlinx.coroutines.flow.Flow

/** Row shape for the calendar and dashboard aggregates. */
data class DayCompletion(
    val localDate: String,
    val completedTasks: Int,
)

/** What today's checklist needs to know about a task that has already been recorded. */
data class TaskDayRecord(
    val taskId: Long,
    val lastRecordedAtMillis: Long,
    val recordCount: Int,
    val lastLogId: Long,
)

@Dao
interface TaskLogDao {

    @Insert
    suspend fun insert(log: TaskLogEntity): Long

    @Update
    suspend fun update(log: TaskLogEntity)

    @Query("SELECT * FROM task_logs WHERE id = :id")
    fun observeById(id: Long): Flow<TaskLogEntity?>

    @Query("SELECT * FROM task_logs WHERE id = :id")
    suspend fun getById(id: Long): TaskLogEntity?

    /**
     * Backing source for the History list. Paged, so the screen stays responsive
     * whether there are 20 records or 200,000.
     *
     * An empty [query] matches everything; otherwise it matches the task title,
     * the notes, the address, or the ISO date.
     */
    @Query(
        """
        SELECT * FROM task_logs
        WHERE (:query = ''
            OR taskTitle LIKE '%' || :query || '%'
            OR notes LIKE '%' || :query || '%'
            OR address LIKE '%' || :query || '%'
            OR localDate LIKE '%' || :query || '%')
        ORDER BY recordedAtMillis DESC
        """,
    )
    fun pagedLogs(query: String): PagingSource<Int, TaskLogEntity>

    @Query("SELECT * FROM task_logs WHERE localDate = :localDate ORDER BY recordedAtMillis DESC")
    fun observeForDate(localDate: String): Flow<List<TaskLogEntity>>

    /** Task ids recorded today, used to tick the checklist. */
    @Query("SELECT DISTINCT taskId FROM task_logs WHERE localDate = :localDate")
    fun observeCompletedTaskIds(localDate: String): Flow<List<Long>>

    /** Per-task record summary for one day, including the id of the latest record. */
    @Query(
        """
        SELECT logs.taskId AS taskId,
               MAX(logs.recordedAtMillis) AS lastRecordedAtMillis,
               COUNT(*) AS recordCount,
               (SELECT latest.id FROM task_logs AS latest
                 WHERE latest.taskId = logs.taskId AND latest.localDate = :localDate
                 ORDER BY latest.recordedAtMillis DESC LIMIT 1) AS lastLogId
        FROM task_logs AS logs
        WHERE logs.localDate = :localDate
        GROUP BY logs.taskId
        """,
    )
    fun observeDayRecords(localDate: String): Flow<List<TaskDayRecord>>

    /**
     * Distinct *currently active* tasks recorded per day. Logs for tasks the user
     * has since deleted are excluded so a day is not scored against a checklist
     * that no longer exists.
     */
    @Query(
        """
        SELECT localDate AS localDate, COUNT(DISTINCT taskId) AS completedTasks
        FROM task_logs
        WHERE localDate BETWEEN :startDate AND :endDate
          AND taskId IN (SELECT id FROM tasks WHERE isActive = 1)
        GROUP BY localDate
        """,
    )
    fun observeCompletionBetween(startDate: String, endDate: String): Flow<List<DayCompletion>>

    @Query("SELECT * FROM task_logs ORDER BY recordedAtMillis DESC")
    suspend fun getAllForExport(): List<TaskLogEntity>

    @Query("SELECT photoPath FROM task_logs WHERE photoPath IS NOT NULL")
    suspend fun getAllPhotoPaths(): List<String>

    @Query("SELECT photoPath FROM task_logs WHERE id = :id AND photoPath IS NOT NULL")
    suspend fun getPhotoPath(id: Long): String?

    @Query("DELETE FROM task_logs WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM task_logs")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM task_logs")
    fun observeTotalCount(): Flow<Int>
}
