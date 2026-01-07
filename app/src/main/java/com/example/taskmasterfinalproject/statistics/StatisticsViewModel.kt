package com.example.taskmasterfinalproject.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.taskmasterfinalproject.data.TaskRepository
import kotlinx.coroutines.flow.map

data class StatsResult(
    val total: Int = 0,
    val active: Int = 0,
    val completed: Int = 0,
    val completionRate: Int = 0,
    val highPriorityPending: Int = 0
)

class StatisticsViewModel(repository: TaskRepository) : ViewModel() {

    val stats = repository.getAllTasksStream().map { tasks ->
        val total = tasks.size
        val completed = tasks.count { it.task.isCompleted }
        val active = total - completed
        val rate = if (total > 0) (completed * 100 / total) else 0
        
        // Priority 1 is High
        val highPriorityPending = tasks.count { !it.task.isCompleted && it.task.priority == 1 }

        StatsResult(total, active, completed, rate, highPriorityPending)
    }.asLiveData()
}
