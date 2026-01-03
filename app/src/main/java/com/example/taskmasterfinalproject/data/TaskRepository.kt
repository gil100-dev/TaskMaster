package com.example.taskmasterfinalproject.data

import com.example.taskmasterfinalproject.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Collections // fallback if needed, but likely kotlin stdlib is enough

class TaskRepository(private val taskDao: TaskDao) {

    // Get all tasks with their details
    fun getAllTasksStream(): Flow<List<TaskWithDetails>> = taskDao.getAllTasksWithDetails()

    fun getTasks(sortOption: SortOption, includeCompleted: Boolean): Flow<List<TaskWithDetails>> {
        val flow = when (sortOption) {
            SortOption.BY_DATE -> taskDao.getTasksSortedByDate(includeCompleted)
            SortOption.BY_PRIORITY -> taskDao.getTasksSortedByPriority(includeCompleted)
            else -> taskDao.getAllTasksWithDetails() // Default source
        }

        // Apply Smart Sort transformation if selected
        return if (sortOption == SortOption.SMART_SORT) {
            flow.map { list ->
                val filtered = if (includeCompleted) list else list.filter { !it.task.isCompleted }
                filtered.sortedByDescending { calculateTaskScore(it.task) }
            }
        } else {
            flow
        }
    }

    internal fun calculateTaskScore(task: Task, now: Long = System.currentTimeMillis()): Int {
        if (task.isCompleted) return -1000 // Push completed to bottom

        var score = 0

        // 1. Priority Weight
        // 1=High (100), 2=Medium (50), 3=Low (10)
        score += when (task.priority) {
            1 -> 100
            2 -> 50
            3 -> 10
            else -> 0
        }

        // 2. Deadline Factor
        val dueTime = task.dueTimeMillis
        if (dueTime != null && dueTime > 0) {
            val diff = dueTime - now
            val oneDayMillis = 24 * 60 * 60 * 1000
            
            when {
                diff < 0 -> score += 500 // Overdue: Critical urgency
                diff < oneDayMillis -> score += 200 // Due Today: High urgency
                diff < (7 * oneDayMillis) -> score += 50 // Due this week: Medium urgency
            }
        }

        return score
    }

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
