package com.example.taskmasterfinalproject.data

import com.example.taskmasterfinalproject.model.*
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {

    // Get all tasks with their details, to be filtered downstream
    fun getAllTasksStream(): Flow<List<TaskWithDetails>> = taskDao.getAllTasksWithDetails()

    fun getTaskDetailsStream(taskId: String): Flow<TaskWithDetails> = taskDao.getTaskWithDetails(taskId)

    suspend fun insertTask(task: Task) {
        taskDao.insertTask(task)
    }

    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }

    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
    }

    // --- Subtask --- 
    suspend fun insertSubtask(subtask: Subtask) {
        taskDao.insertSubtask(subtask)
    }

    suspend fun updateSubtask(subtask: Subtask) {
        taskDao.updateSubtask(subtask)
    }

    suspend fun deleteSubtask(subtask: Subtask) {
        taskDao.deleteSubtask(subtask)
    }

    // --- Tag & CrossRef --- 
    fun getAllTagsStream(): Flow<List<Tag>> = taskDao.getAllTags()

    suspend fun insertTag(tag: Tag) {
        taskDao.insertTag(tag)
    }

    suspend fun addTaskTagCrossRef(taskId: String, tagId: String) {
        taskDao.insertTaskTagCrossRef(TaskTagCrossRef(taskId, tagId))
    }

    suspend fun removeTaskTagCrossRef(taskId: String, tagId: String) {
        taskDao.deleteTaskTagCrossRef(TaskTagCrossRef(taskId, tagId))
    }

    // --- Statistics --- 
    fun getCompletedTaskCountStream(since: Long): Flow<Int> = taskDao.getCompletedTaskCount(since)

    fun getCompletedTasksForStreak(): Flow<List<Task>> = taskDao.getCompletedTasksSortedByCompletion()
}
