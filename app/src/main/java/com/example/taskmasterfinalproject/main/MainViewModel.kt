package com.example.taskmasterfinalproject.main

// ספרייה בסיסית של האפליקציה, עבור ה-Context
import android.app.Application
// מחלקת ViewModel המתחשבת ב-Context של האפליקציה
import androidx.lifecycle.AndroidViewModel
// ספרייה המייצגת נתונים ברי-צפייה (Observable) המודעים למחזור החיים
import androidx.lifecycle.LiveData
// ספרייה להמרת Flow ל-LiveData
import androidx.lifecycle.asLiveData
// אופרטור טרנספורמציה עבור LiveData (אם כי לא בשימוש ישיר כאן, אך מיובא)
import androidx.lifecycle.map
// ספרייה המספקת CoroutineScope הקשור למחזור החיים של ה-ViewModel
import androidx.lifecycle.viewModelScope
// שכבת הנתונים (Repository) לניהול משימות
import com.example.taskmasterfinalproject.data.TaskRepository
// מנהל העדפות המשתמש (SharedPreferences)
import com.example.taskmasterfinalproject.data.PreferencesManager
// מסד הנתונים של האפליקציה
import com.example.taskmasterfinalproject.db.TaskDatabase
// המודל המייצג אפשרויות מיון
import com.example.taskmasterfinalproject.model.SortOption
// המודל המייצג משימה
import com.example.taskmasterfinalproject.model.Task
// ספרייה לניהול state בצורה ריאקטיבית (Flow) הניתן לשינוי
import kotlinx.coroutines.flow.MutableStateFlow
// ספרייה לשילוב מספר מקורות Flow לאחד
import kotlinx.coroutines.flow.combine
// ספרייה לביצוע טרנספורמציה שטוחה (FlatMap) על Flow העדכני ביותר
import kotlinx.coroutines.flow.flatMapLatest
// ספרייה לביצוע טרנספורמציה על נתוני Flow
import kotlinx.coroutines.flow.map
// ספרייה להרצת קורוטינות (Coroutines)
import kotlinx.coroutines.launch

// ViewModel הראשי של האפליקציה, מנהל את רשימת המשימות, המיון והסינון
class MainViewModel(application: Application) : AndroidViewModel(application) {

    // יצירת ה-Repository עם ה-DAO של מסד הנתונים
    private val repository: TaskRepository = TaskRepository(
        TaskDatabase.getInstance(application).taskDao()
    )
    // מנהל העדפות – קורא ושומר הגדרות משתמש ב-SharedPreferences
    private val preferencesManager = PreferencesManager(application)

    // MutableStateFlow – כמו LiveData אבל מעולם ה-Flow/Coroutines. מחזיק את הערך האחרון
    private val _sortOption = MutableStateFlow(preferencesManager.getSortOption())
    private val _showCompleted = MutableStateFlow(preferencesManager.getShowCompleted())

    init {
        // אימוץ נתונים קודמים אם קיימים (שיוך משימות ללא משתמש למשתמש הנוכחי)
        val userId = com.example.taskmasterfinalproject.auth.SessionManager.currentUserId
        if (userId != null) {
            viewModelScope.launch {
                repository.adoptLegacyData(userId)
            }
        }
    }

    // משתנה LiveData החושף את רשימת המשימות המעודכנת בהתאם למיון והסינון
    // שרשרת Flow ריאקטיבית:
    // 1. combine – משלב שני Flows (מיון + הצגת הושלמו) ל-Flow אחד
    // 2. flatMapLatest – כל פעם שאחד הערכים משתנה, מבטל את ה-Flow הקודם ויוצר חדש
    // 3. map – ממפה TaskWithDetails ל-Task בלבד (חילוץ המשימה)
    // 4. asLiveData() – המרת Flow ל-LiveData לשימוש עם observe() ב-UI
    val tasks: LiveData<List<Task>> = combine(_sortOption, _showCompleted) { sort, show ->
        Pair(sort, show)
    }.flatMapLatest { (sort, show) ->
        repository.getTasks(sort, show)
    }.map { list ->
        list.map { it.task }
    }.asLiveData()
    
    // חשיפת אפשרות המיון הנוכחית לסנכרון ממשק המשתמש
    val currentSortOption: SortOption get() = _sortOption.value
    // חשיפת מצב הצגת המשימות שהושלמו לסנכרון ממשק המשתמש
    val currentShowCompleted: Boolean get() = _showCompleted.value

    // פונקציה להגדרת אפשרות המיון ושמירתה בהעדפות
    fun setSortOption(option: SortOption) {
        _sortOption.value = option
        preferencesManager.saveSortOption(option)
    }

    // פונקציה לשינוי מצב הצגת משימות שהושלמו (הצג/הסתר)
    fun toggleShowCompleted() {
        val newValue = !_showCompleted.value
        _showCompleted.value = newValue
        preferencesManager.saveShowCompleted(newValue)
    }

    // פונקציה להוספת משימה חדשה
    fun addTask(task: Task) {
        viewModelScope.launch {
            repository.insertTask(task)
            com.example.taskmasterfinalproject.widget.WidgetUpdateHelper.updateUrgentTasksWidget(getApplication())
        }
    }

    // פונקציה לסימון משימה כהושלמה
    fun markTaskCompleted(task: Task) {
        viewModelScope.launch {
            val updatedTask = task.copy(isCompleted = true)
            repository.updateTask(updatedTask)
            com.example.taskmasterfinalproject.widget.WidgetUpdateHelper.updateUrgentTasksWidget(getApplication())
        }
    }

    // פונקציה למחיקת משימה
    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
            com.example.taskmasterfinalproject.widget.WidgetUpdateHelper.updateUrgentTasksWidget(getApplication())
        }
    }

    // פונקציה לעדכון משימה קיימת
    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task)
            com.example.taskmasterfinalproject.widget.WidgetUpdateHelper.updateUrgentTasksWidget(getApplication())
        }
    }
}
