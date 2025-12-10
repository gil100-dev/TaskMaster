package com.example.taskmasterfinalproject.completed

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.taskmasterfinalproject.data.TaskDatabase
import com.example.taskmasterfinalproject.data.TaskRepository
import com.example.taskmasterfinalproject.model.Task
import kotlinx.coroutines.launch

class CompletedTasksViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaskRepository =
        TaskRepository(TaskDatabase.getInstance(application).taskDao())

    val completedTasks: MutableLiveData<List<Task>> = MutableLiveData(emptyList())

    init {
        viewModelScope.launch {
            completedTasks.value = repository.getCompletedTasks()
        }
    }

    fun refreshCompletedTasks() {
        viewModelScope.launch {
            completedTasks.value = repository.getCompletedTasks()
        }
    }

    fun restoreTask(task: Task) {
        viewModelScope.launch {
            val restored = task.copy(isCompleted = false)
            repository.updateTask(restored)
            completedTasks.value = repository.getCompletedTasks()
        }
    }
}


