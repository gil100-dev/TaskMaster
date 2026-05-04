package com.example.taskmasterfinalproject.auth

// ספרייה לקבלת ה-Context של האפליקציה ב-ViewModel
import android.app.Application
// מחלקת ViewModel בסיסית המקבלת Application
import androidx.lifecycle.AndroidViewModel
// ספרייה לייצוג נתונים ברי-צפייה (לקריאה בלבד)
import androidx.lifecycle.LiveData
// ספרייה לייצוג נתונים ברי-צפייה וניתנים לשינוי
import androidx.lifecycle.MutableLiveData
// ספרייה לניהול Coroutines בתוך ה-ViewModel
import androidx.lifecycle.viewModelScope
// ה-Repository המטפל באימות
import com.example.taskmasterfinalproject.data.AuthRepository
// מנהל ההעדפות
import com.example.taskmasterfinalproject.data.PreferencesManager
// מסד הנתונים
import com.example.taskmasterfinalproject.db.TaskDatabase
// המודל המייצג משתמש
import com.example.taskmasterfinalproject.model.User
// ספרייה להרצת קורוטינות
import kotlinx.coroutines.launch

// ViewModel עבור מסכי האימות (התחברות, הרשמה, פרופיל)
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AuthRepository
    private val prefs: PreferencesManager

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _authResult = MutableLiveData<Result<User>>()
    val authResult: LiveData<Result<User>> = _authResult

    init {
        val userDao = TaskDatabase.getInstance(application).userDao()
        repository = AuthRepository(userDao)
        prefs = PreferencesManager(application)
    }

    // פונקציה לביצוע התחברות
    fun login(username: String, pass: String, rememberMe: Boolean) {
        if (username.isBlank() || pass.isBlank()) {
            _authResult.value = Result.failure(Exception("Please fill all fields"))
            return
        }
        
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.login(username, pass)
            
            if (result.isSuccess) {
                val user = result.getOrNull()
                user?.let { 
                    SessionManager.startSession(it.id)
                    if (rememberMe) {
                        prefs.saveSession(it.id)
                    }
                }
            }
            _authResult.value = result
            _isLoading.value = false
        }
    }

    // פונקציה לביצוע הרשמה
    fun register(username: String, pass: String) {
        if (username.isBlank() || pass.isBlank()) {
            _authResult.value = Result.failure(Exception("Please fill all fields"))
            return
        }
        if (pass.length < 6) {
             _authResult.value = Result.failure(Exception("הסיסמה קצרה מדי (מינימום 6 תווים)"))
             return
        }

        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.register(username, pass)
            // אם ההרשמה הצליחה, מחזירים תוצאה חיובית. המשתמש יידרש להתחבר ידנית או שהמסך יטפל בזה.
            _authResult.value = result
            _isLoading.value = false
        }
    }
    
    // פונקציה לשינוי סיסמה (גרסה פנימית ישנה שנותרה, ראה changePasswordAction)
    fun changePassword(userId: Long, oldPass: String, newPass: String) {
        if (oldPass.isBlank() || newPass.isBlank()) {
            _authResult.value = Result.failure(Exception("Please fill all fields"))
            return
        }
        if (newPass.length < 6) {
             _authResult.value = Result.failure(Exception("הסיסמה החדשה קצרה מדי"))
             return
        }
        
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.changePassword(userId, oldPass, newPass)
            if (result.isSuccess) {
                 // סיסמה שונתה
            }
        }
    }
    
    private val _passwordChangeResult = MutableLiveData<Result<Unit>>()
    val passwordChangeResult: LiveData<Result<Unit>> = _passwordChangeResult
    
    // פונקציה לשינוי סיסמה בפועל, המעדכנת את ה-LiveData הייעודי
    fun changePasswordAction(userId: Long, oldPass: String, newPass: String) {
        if (oldPass.isBlank() || newPass.isBlank()) {
            _passwordChangeResult.value = Result.failure(Exception("נא למלא את כל השדות"))
            return
        }
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.changePassword(userId, oldPass, newPass)
            _passwordChangeResult.value = result
            _isLoading.value = false
        }
    }
    
    // משתנה LiveData להחזקת פרטי המשתמש הנוכחי
    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    // פונקציה לטעינת פרטי משתמש לפי מזהה
    fun loadUser(userId: Long) {
        viewModelScope.launch {
            val u = repository.getUser(userId)
            _user.value = u
        }
    }
}
