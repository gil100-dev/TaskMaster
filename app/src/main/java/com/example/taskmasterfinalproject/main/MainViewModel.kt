package com.example.taskmasterfinalproject.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.taskmasterfinalproject.data.TaskRepository
import com.example.taskmasterfinalproject.db.TaskDatabase
import com.example.taskmasterfinalproject.model.Task
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaskRepository

    val tasks: MutableLiveData<List<Task>> = MutableLiveData(emptyList())

    init {
        val taskDao = TaskDatabase.getInstance(application).taskDao()
        repository = TaskRepository(taskDao)
        refreshTasks()
    }

    fun refreshTasks() {
        viewModelScope.launch {
            tasks.value = repository.getActiveTasks()
        }
    }

    fun addTask(task: Task) {
        viewModelScope.launch {
            repository.insertTask(task)
            refreshTasks()
        }
    }

    fun markTaskCompleted(task: Task) {
        viewModelScope.launch {
            val updatedTask = task.copy(isCompleted = true)
            repository.updateTask(updatedTask)
            refreshTasks()
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
            refreshTasks()
        }
    }
}
