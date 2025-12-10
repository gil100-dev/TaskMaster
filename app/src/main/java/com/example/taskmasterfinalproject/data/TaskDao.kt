package com.example.taskmasterfinalproject.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.taskmasterfinalproject.model.Task

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks WHERE isCompleted = 0")
    suspend fun getActiveTasks(): List<Task>

    @Query("SELECT * FROM tasks WHERE isCompleted = 1")
    suspend fun getCompletedTasks(): List<Task>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)
}



