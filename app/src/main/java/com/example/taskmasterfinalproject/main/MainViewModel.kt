package com.example.taskmasterfinalproject.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.taskmasterfinalproject.data.TaskDatabase
import com.example.taskmasterfinalproject.data.TaskRepository
import com.example.taskmasterfinalproject.model.Task
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaskRepository =
        TaskRepository(TaskDatabase.getInstance(application).taskDao())

    val tasks: MutableLiveData<List<Task>> = MutableLiveData(emptyList())

    init {
        viewModelScope.launch {
            tasks.value = repository.getActiveTasks()
        }
    }

    fun addTask(task: Task) {
        viewModelScope.launch {
            repository.insertTask(task)
            tasks.value = repository.getActiveTasks()
        }
    }

    fun markTaskCompleted(task: Task) {
        viewModelScope.launch {
            val completedTask = task.copy(isCompleted = true)
            repository.updateTask(completedTask)
            tasks.value = repository.getActiveTasks()
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
            tasks.value = repository.getActiveTasks()
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task)
            tasks.value = repository.getActiveTasks()
        }
    }
}

