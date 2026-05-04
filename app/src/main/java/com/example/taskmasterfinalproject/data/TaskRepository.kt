package com.example.taskmasterfinalproject.data

// ייבוא המודלים של האפליקציה
import com.example.taskmasterfinalproject.model.*
// ספרייה לשימוש ב-Flow (זרם נתונים א-סינכרוני)
import kotlinx.coroutines.flow.Flow
// ספרייה לביצוע טרנספורמציה על נתוני Flow
import kotlinx.coroutines.flow.map
// ספרייה למבני נתונים בסיסיים של Java (אם נדרש)
import java.util.Collections

// שכבת הנתונים (Repository) המתווכת בין ה-ViewModel למסד הנתונים (DAO)
class TaskRepository(private val taskDao: TaskDao) {

    // פונקציה לקבלת כל המשימות עם הפרטים שלהן כ-Flow
    fun getAllTasksStream(): Flow<List<TaskWithDetails>> {
        val userId = com.example.taskmasterfinalproject.auth.SessionManager.currentUserId ?: return kotlinx.coroutines.flow.emptyFlow()
        return taskDao.getAllTasksWithDetails(userId)
    }

    // פונקציה לקבלת משימות בהתאם לאפשרויות המיון והסינון
    fun getTasks(sortOption: SortOption, includeCompleted: Boolean): Flow<List<TaskWithDetails>> {
        val userId = com.example.taskmasterfinalproject.auth.SessionManager.currentUserId ?: return kotlinx.coroutines.flow.emptyFlow()
        
        val flow = when (sortOption) {
            SortOption.BY_DATE -> taskDao.getTasksSortedByDate(includeCompleted, userId)
            SortOption.BY_PRIORITY -> taskDao.getTasksSortedByPriority(includeCompleted, userId)
            else -> taskDao.getAllTasksWithDetails(userId) // ברירת מחדל
        }

        // החלת "מיון חכם" (Smart Sort) אם נבחר
        return if (sortOption == SortOption.SMART_SORT) {
            flow.map { list ->
                val filtered = if (includeCompleted) list else list.filter { !it.task.isCompleted }
                filtered.sortedByDescending { calculateTaskScore(it.task) }
            }
        } else {
            flow
        }
    }

    // פונקציה פנימית לחישוב "ציון" למשימה עבור המיון החכם
    internal fun calculateTaskScore(task: Task, now: Long = System.currentTimeMillis()): Int {
        if (task.isCompleted) return -1000 // דחיקת משימות שהושלמו למטה

        var score = 0

        // 1. משקל עדיפות
        // 3=גבוה (100), 2=בינוני (50), 1=נמוך (10)
        score += when (task.priority) {
            3 -> 100
            2 -> 50
            1 -> 10
            else -> 0
        }

        // 2. פקטור מועד יעד
        val dueTime = task.dueTimeMillis
        if (dueTime != null && dueTime > 0) {
            val diff = dueTime - now
            val oneDayMillis = 24 * 60 * 60 * 1000
            
            when {
                diff < 0 -> score += 500 // באיחור: דחיפות קריטית
                diff < oneDayMillis -> score += 200 // היום: דחיפות גבוהה
                diff < (7 * oneDayMillis) -> score += 50 // השבוע: דחיפות בינונית
            }
        }

        return score
    }

    // פונקציה לקבלת פרטי משימה ספציפית
    fun getTaskDetailsStream(taskId: String): Flow<TaskWithDetails> {
        val userId = com.example.taskmasterfinalproject.auth.SessionManager.currentUserId ?: return kotlinx.coroutines.flow.emptyFlow()
        return taskDao.getTaskWithDetails(taskId, userId)
    }

    // פונקציה להוספת משימה
    suspend fun insertTask(task: Task) {
         val userId = com.example.taskmasterfinalproject.auth.SessionManager.currentUserId ?: -1L
        taskDao.insertTask(task.copy(ownerUserId = userId))
    }

    // פונקציה לעדכון משימה
    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }

    // פונקציה למחיקת משימה
    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
    }

    // --- Subtask --- 
    // פונקציה להוספת תת-משימה
    suspend fun insertSubtask(subtask: Subtask) {
        taskDao.insertSubtask(subtask)
    }

    // פונקציה לעדכון תת-משימה
    suspend fun updateSubtask(subtask: Subtask) {
        taskDao.updateSubtask(subtask)
    }

    // פונקציה למחיקת תת-משימה
    suspend fun deleteSubtask(subtask: Subtask) {
        taskDao.deleteSubtask(subtask)
    }

    // --- Tag & CrossRef --- 
    // פונקציה לקבלת כל התגיות כ-Flow
    fun getAllTagsStream(): Flow<List<Tag>> {
         val userId = com.example.taskmasterfinalproject.auth.SessionManager.currentUserId ?: return kotlinx.coroutines.flow.emptyFlow()
         return taskDao.getAllTags(userId)
    }

    // פונקציה להוספת תגית
    suspend fun insertTag(tag: Tag) {
         val userId = com.example.taskmasterfinalproject.auth.SessionManager.currentUserId ?: -1L
        taskDao.insertTag(tag.copy(ownerUserId = userId))
    }

    // פונקציה להוספת קישור בין משימה לתגית
    suspend fun addTaskTagCrossRef(taskId: String, tagId: String) {
        taskDao.insertTaskTagCrossRef(TaskTagCrossRef(taskId, tagId))
    }

    // פונקציה להסרת קישור בין משימה לתגית
    suspend fun removeTaskTagCrossRef(taskId: String, tagId: String) {
        taskDao.deleteTaskTagCrossRef(TaskTagCrossRef(taskId, tagId))
    }

    // --- Statistics --- 
    // פונקציה לקבלת זרם נתונים של כמות המשימות שהושלמו
    fun getCompletedTaskCountStream(since: Long): Flow<Int> {
         val userId = com.example.taskmasterfinalproject.auth.SessionManager.currentUserId ?: return kotlinx.coroutines.flow.emptyFlow()
         return taskDao.getCompletedTaskCount(since, userId)
    }

    // פונקציה לקבלת משימות שהושלמו עבור חישובי רצף
    fun getCompletedTasksForStreak(): Flow<List<Task>> {
         val userId = com.example.taskmasterfinalproject.auth.SessionManager.currentUserId ?: return kotlinx.coroutines.flow.emptyFlow()
         return taskDao.getCompletedTasksSortedByCompletion(userId)
    }
    
    // פונקציה לאימוץ נתונים ליגאסיים למשתמש הנוכחי
    suspend fun adoptLegacyData(userId: Long) {
        taskDao.adoptLegacyTasks(userId)
        taskDao.adoptLegacyTags(userId)
    }
}
