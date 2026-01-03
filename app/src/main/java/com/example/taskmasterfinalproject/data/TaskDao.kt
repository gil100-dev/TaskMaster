package com.example.taskmasterfinalproject.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.taskmasterfinalproject.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    // --- Task Methods ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Query("SELECT * FROM tasks")
    suspend fun getTasksList(): List<Task>

    @Delete
    suspend fun deleteTask(task: Task)

    @Transaction
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    fun getTaskWithDetails(taskId: String): Flow<TaskWithDetails>

    @Transaction
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC") // Default
    fun getAllTasksWithDetails(): Flow<List<TaskWithDetails>>

    @Transaction
    @Query("SELECT * FROM tasks WHERE (isCompleted = 0 OR :includeCompleted = 1) ORDER BY dueDate ASC, createdAt DESC")
    fun getTasksSortedByDate(includeCompleted: Boolean): Flow<List<TaskWithDetails>>

    @Transaction
    @Query("SELECT * FROM tasks WHERE (isCompleted = 0 OR :includeCompleted = 1) ORDER BY priority DESC, createdAt DESC")
    fun getTasksSortedByPriority(includeCompleted: Boolean): Flow<List<TaskWithDetails>>

    // --- Subtask Methods ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtask(subtask: Subtask)

    @Update
    suspend fun updateSubtask(subtask: Subtask)

    @Delete
    suspend fun deleteSubtask(subtask: Subtask)

    // --- Tag & CrossRef Methods ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: Tag)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTaskTagCrossRef(crossRef: TaskTagCrossRef)

    @Delete
    suspend fun deleteTaskTagCrossRef(crossRef: TaskTagCrossRef)

    @Transaction
    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTags(): Flow<List<Tag>>

    // --- Statistics Methods ---
    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 1 AND completedAt >= :since")
    fun getCompletedTaskCount(since: Long): Flow<Int>

    @Query("SELECT * FROM tasks WHERE isCompleted = 1 ORDER BY completedAt DESC")
    fun getCompletedTasksSortedByCompletion(): Flow<List<Task>>
}
