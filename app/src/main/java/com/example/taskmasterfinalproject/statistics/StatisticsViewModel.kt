package com.example.taskmasterfinalproject.statistics

// מחלקת הבסיס ל-ViewModel
import androidx.lifecycle.ViewModel
// המרת Flow ל-LiveData
import androidx.lifecycle.asLiveData
// המאגר לגישה לנתוני משימות
import com.example.taskmasterfinalproject.data.TaskRepository
// אופרטור טרנספורמציה ב-Flow
import kotlinx.coroutines.flow.map

// מודל נתונים המכיל את תוצאות החישוב הסטטיסטי
// מודל נתונים המכיל את תוצאות החישוב הסטטיסטי.
// data class מספקת אוטומטית: equals(), hashCode(), toString(), copy()
data class StatsResult(
    val total: Int = 0,               // סך הכל המשימות
    val active: Int = 0,              // משימות פעילות (לא הושלמו)
    val completed: Int = 0,           // משימות שהושלמו
    val completionRate: Int = 0,      // אחוז השלמה (0-100)
    val highPriorityPending: Int = 0  // משימות בעדיפות גבוהה שעדיין פתוחות
)

// ViewModel המחשב ומספק נתונים סטטיסטיים על המשימות
class StatisticsViewModel(repository: TaskRepository) : ViewModel() {

    // זרם נתונים (LiveData) המכיל את הסטטיסטיקות המחושבות בזמן אמת.
    // ה-Flow מתעדכן אוטומטית כל פעם שמשימה נוספת/נמחקת/משתנה במסד הנתונים
    val stats = repository.getAllTasksStream().map { tasks ->
        val total = tasks.size
        val completed = tasks.count { it.task.isCompleted }
        val active = total - completed
        // חישוב אחוז השלמה (מניעת חלוקה ב-0)
        val rate = if (total > 0) (completed * 100 / total) else 0
        
        // ספירת משימות בעדיפות גבוהה שעדיין לא הושלמו
        val highPriorityPending = tasks.count { !it.task.isCompleted && it.task.priority == 1 }

        StatsResult(total, active, completed, rate, highPriorityPending)
    }.asLiveData()  // asLiveData() – המרה ל-LiveData לצורך observe() מה-UI
}
