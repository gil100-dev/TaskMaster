package com.example.taskmasterfinalproject.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.taskmasterfinalproject.data.TaskRepository
import com.example.taskmasterfinalproject.data.PreferencesManager
import com.example.taskmasterfinalproject.db.TaskDatabase
import com.example.taskmasterfinalproject.model.SortOption
import com.example.taskmasterfinalproject.model.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaskRepository = TaskRepository(
        TaskDatabase.getInstance(application).taskDao()
    )
    private val preferencesManager = PreferencesManager(application)

    private val _sortOption = MutableStateFlow(preferencesManager.getSortOption())
    private val _showCompleted = MutableStateFlow(preferencesManager.getShowCompleted())

    val tasks: LiveData<List<Task>> = combine(_sortOption, _showCompleted) { sort, show ->
        Pair(sort, show)
    }.flatMapLatest { (sort, show) ->
        repository.getTasks(sort, show)
    }.map { list ->
        list.map { it.task }
    }.asLiveData()
    
    // Expose current sort option for UI synchronization
    val currentSortOption: SortOption get() = _sortOption.value
    // Expose current show completed for UI synchronization
    val currentShowCompleted: Boolean get() = _showCompleted.value

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
        preferencesManager.saveSortOption(option)
    }

    fun toggleShowCompleted() {
        val newValue = !_showCompleted.value
        _showCompleted.value = newValue
        preferencesManager.saveShowCompleted(newValue)
    }

    fun addTask(task: Task) {
        viewModelScope.launch {
            repository.insertTask(task)
            com.example.taskmasterfinalproject.widget.WidgetUpdateHelper.updateUrgentTasksWidget(getApplication())
        }
    }

    fun markTaskCompleted(task: Task) {
        viewModelScope.launch {
            val updatedTask = task.copy(isCompleted = true)
            repository.updateTask(updatedTask)
            com.example.taskmasterfinalproject.widget.WidgetUpdateHelper.updateUrgentTasksWidget(getApplication())
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
            com.example.taskmasterfinalproject.widget.WidgetUpdateHelper.updateUrgentTasksWidget(getApplication())
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task)
            com.example.taskmasterfinalproject.widget.WidgetUpdateHelper.updateUrgentTasksWidget(getApplication())
        }
    }
}
