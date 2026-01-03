package com.example.taskmasterfinalproject.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.taskmasterfinalproject.data.TaskRepository
import com.example.taskmasterfinalproject.model.Subtask
import com.example.taskmasterfinalproject.model.TaskWithDetails
import kotlinx.coroutines.launch

class TaskDetailViewModel(private val repository: TaskRepository, private val taskId: String) : ViewModel() {

    val taskWithDetails: LiveData<TaskWithDetails> = repository.getTaskDetailsStream(taskId).asLiveData()

    fun addSubtask(title: String) {
        viewModelScope.launch {
            val subtask = Subtask(taskId = taskId, title = title)
            repository.insertSubtask(subtask)
        }
    }

    fun restoreSubtask(subtask: Subtask) {
        viewModelScope.launch {
            repository.insertSubtask(subtask)
        }
    }

    fun toggleSubtask(subtask: Subtask) {
        viewModelScope.launch {
            val updatedSubtask = subtask.copy(isDone = !subtask.isDone)
            repository.updateSubtask(updatedSubtask)
        }
    }

    fun deleteSubtask(subtask: Subtask) {
        viewModelScope.launch {
            repository.deleteSubtask(subtask)
        }
    }
}
