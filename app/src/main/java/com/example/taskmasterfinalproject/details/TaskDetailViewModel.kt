package com.example.taskmasterfinalproject.details

// ספרייה המייצגת נתונים ברי-צפייה (Observable) המודעים למחזור החיים
import androidx.lifecycle.LiveData
// מחלקת בסיס ל-ViewModel, האחראי על ניהול הנתונים של ה-UI
import androidx.lifecycle.ViewModel
// ספרייה להמרת Flow ל-LiveData
import androidx.lifecycle.asLiveData
// ספרייה המספקת CoroutineScope הקשור למחזור החיים של ה-ViewModel
import androidx.lifecycle.viewModelScope
// שכבת הנתונים (Repository) לניהול משימות
import com.example.taskmasterfinalproject.data.TaskRepository
// המודל המייצג תת-משימה
import com.example.taskmasterfinalproject.model.Subtask
// אובייקט המאגד משימה עם פרטיה (כגון תת-משימות)
import com.example.taskmasterfinalproject.model.TaskWithDetails
// ספרייה להרצת קורוטינות (Coroutines)
import kotlinx.coroutines.launch

// ViewModel עבור מסך פרטי המשימה, מנהל את הלוגיקה העסקית והנתונים
class TaskDetailViewModel(private val repository: TaskRepository, private val taskId: String) : ViewModel() {

    // משתנה LiveData המחזיק את המשימה ופרטיה, ומעדכן את ה-UI אוטומטית בשינויים
    val taskWithDetails: LiveData<TaskWithDetails> = repository.getTaskDetailsStream(taskId).asLiveData()

    // פונקציה להוספת תת-משימה חדשה
    fun addSubtask(title: String) {
        viewModelScope.launch {
            val subtask = Subtask(taskId = taskId, title = title)
            repository.insertSubtask(subtask)
        }
    }

    // פונקציה לשחזור תת-משימה שנמחקה
    fun restoreSubtask(subtask: Subtask) {
        viewModelScope.launch {
            repository.insertSubtask(subtask)
        }
    }

    // פונקציה לשינוי מצב הביצוע של תת-משימה (בוצע/לא בוצע)
    fun toggleSubtask(subtask: Subtask) {
        viewModelScope.launch {
            val updatedSubtask = subtask.copy(isDone = !subtask.isDone)
            repository.updateSubtask(updatedSubtask)
        }
    }

    // פונקציה למחיקת תת-משימה
    fun deleteSubtask(subtask: Subtask) {
        viewModelScope.launch {
            repository.deleteSubtask(subtask)
        }
    }
}
