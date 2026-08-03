package com.surestep.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.surestep.app.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks WHERE isActive = 1 ORDER BY sortOrder ASC, id ASC")
    fun observeActive(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks ORDER BY sortOrder ASC, id ASC")
    fun observeAll(): Flow<List<TaskEntity>>

    @Query("SELECT COUNT(*) FROM tasks WHERE isActive = 1")
    fun observeActiveCount(): Flow<Int>

    /** When the checklist first existed. Null until the first task is created. */
    @Query("SELECT MIN(createdAt) FROM tasks WHERE isActive = 1")
    fun observeEarliestCreatedAt(): Flow<Long?>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: Long): TaskEntity?

    @Query("SELECT COALESCE(MAX(sortOrder), -1) + 1 FROM tasks")
    suspend fun nextSortOrder(): Int

    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun count(): Int

    @Insert
    suspend fun insert(task: TaskEntity): Long

    @Insert
    suspend fun insertAll(tasks: List<TaskEntity>)

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("UPDATE tasks SET sortOrder = :sortOrder WHERE id = :id")
    suspend fun setSortOrder(id: Long, sortOrder: Int)

    /** Rewrites the whole ordering in one transaction so the list never renders half-sorted. */
    @Transaction
    suspend fun applyOrder(idsInOrder: List<Long>) {
        idsInOrder.forEachIndexed { index, id -> setSortOrder(id, index) }
    }
}
