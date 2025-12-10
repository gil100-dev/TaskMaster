package com.example.taskmasterfinalproject.data

import com.example.taskmasterfinalproject.model.Task

class TaskRepository(private val dao: TaskDao) {

    suspend fun getActiveTasks(): List<Task> = dao.getActiveTasks()

    suspend fun getCompletedTasks(): List<Task> = dao.getCompletedTasks()

    suspend fun insertTask(task: Task) = dao.insertTask(task)

    suspend fun deleteTask(task: Task) = dao.deleteTask(task)

    suspend fun updateTask(task: Task) = dao.updateTask(task)
}



