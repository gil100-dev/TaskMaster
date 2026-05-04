package com.example.taskmasterfinalproject.data

// ספרייה המגדירה כי הממשק הוא DAO (Data Access Object)
import androidx.room.Dao
// ספרייה להגדרת פעולת מחיקה
import androidx.room.Delete
// ספרייה להגדרת פעולת הוספה
import androidx.room.Insert
// ספרייה להגדרת אסטרטגיה במקרה של התנגשות (כגון החלפה)
import androidx.room.OnConflictStrategy
// ספרייה להגדרת שאילתת SQL מותאמת אישית
import androidx.room.Query
// ספרייה להגדרת טרנזקציה (ביצוע מספר פעולות כאחת)
import androidx.room.Transaction
// ספרייה להגדרת פעולת עדכון
import androidx.room.Update
// ייבוא כל המודלים (Task, Subtask וכו')
import com.example.taskmasterfinalproject.model.*
// ספרייה לשימוש ב-Flow עבור עדכונים בזמן אמת
import kotlinx.coroutines.flow.Flow

// ממשק לגישה לנתונים (DAO), מגדיר את פעולות מסד הנתונים
@Dao
interface TaskDao {

    // --- Task Methods ---
    // פונקציה להוספת משימה חדשה (מחליפה אם קיים)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    // פונקציה לעדכון משימה קיימת
    @Update
    suspend fun updateTask(task: Task)

    // פונקציה לקבלת רשימת משימות כשגרת סינכרונית (suspend)
    @Query("SELECT * FROM tasks WHERE ownerUserId = :userId")
    suspend fun getTasksList(userId: Long): List<Task>

    // פונקציה למחיקת משימה
    @Delete
    suspend fun deleteTask(task: Task)

    // פונקציה לקבלת משימה ספציפית עם פרטיה (תת-משימות וכו') כ-Flow
    @Transaction
    @Query("SELECT * FROM tasks WHERE id = :taskId AND ownerUserId = :userId")
    fun getTaskWithDetails(taskId: String, userId: Long): Flow<TaskWithDetails>

    // פונקציה לקבלת כל המשימות עם פרטיהן כ-Flow
    @Transaction
    @Query("SELECT * FROM tasks WHERE ownerUserId = :userId ORDER BY createdAt DESC") // ברירת מחדל
    fun getAllTasksWithDetails(userId: Long): Flow<List<TaskWithDetails>>

    // פונקציה לקבלת משימות ממוינות לפי תאריך יעד
    @Transaction
    @Query("SELECT * FROM tasks WHERE ownerUserId = :userId AND (isCompleted = 0 OR :includeCompleted = 1) ORDER BY dueDate ASC, createdAt DESC")
    fun getTasksSortedByDate(includeCompleted: Boolean, userId: Long): Flow<List<TaskWithDetails>>

    // פונקציה לקבלת משימות ממוינות לפי עדיפות
    @Transaction
    @Query("SELECT * FROM tasks WHERE ownerUserId = :userId AND (isCompleted = 0 OR :includeCompleted = 1) ORDER BY priority DESC, createdAt DESC")
    fun getTasksSortedByPriority(includeCompleted: Boolean, userId: Long): Flow<List<TaskWithDetails>>

    // --- Subtask Methods ---
    // פונקציה להוספת תת-משימה
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtask(subtask: Subtask)

    // פונקציה לעדכון תת-משימה
    @Update
    suspend fun updateSubtask(subtask: Subtask)

    // פונקציה למחיקת תת-משימה
    @Delete
    suspend fun deleteSubtask(subtask: Subtask)

    // --- Tag & CrossRef Methods ---
    // פונקציה להוספת תגית
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: Tag)

    // פונקציה לקישור משימה לתגית (טבלת קשר)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTaskTagCrossRef(crossRef: TaskTagCrossRef)

    // פונקציה להסרת קישור בין משימה לתגית
    @Delete
    suspend fun deleteTaskTagCrossRef(crossRef: TaskTagCrossRef)

    // פונקציה לקבלת כל התגיות
    @Transaction
    @Query("SELECT * FROM tags WHERE ownerUserId = :userId ORDER BY name ASC")
    fun getAllTags(userId: Long): Flow<List<Tag>>

    // --- Statistics Methods ---
    // פונקציה לקבלת כמות המשימות שהושלמו מאז זמן מסוים
    @Query("SELECT COUNT(*) FROM tasks WHERE ownerUserId = :userId AND isCompleted = 1 AND completedAt >= :since")
    fun getCompletedTaskCount(since: Long, userId: Long): Flow<Int>

    // פונקציה לקבלת משימות שהושלמו, ממוינות לפי זמן סיום
    @Query("SELECT * FROM tasks WHERE ownerUserId = :userId AND isCompleted = 1 ORDER BY completedAt DESC")
    fun getCompletedTasksSortedByCompletion(userId: Long): Flow<List<Task>>
    
    // --- Migration Helpers ---
    // עדכון משימות ישנות לשיוך למשתמש
    @Query("UPDATE tasks SET ownerUserId = :userId WHERE ownerUserId = -1")
    suspend fun adoptLegacyTasks(userId: Long)

    // עדכון תגיות ישנות לשיוך למשתמש
    @Query("UPDATE tags SET ownerUserId = :userId WHERE ownerUserId = -1")
    suspend fun adoptLegacyTags(userId: Long)
}
