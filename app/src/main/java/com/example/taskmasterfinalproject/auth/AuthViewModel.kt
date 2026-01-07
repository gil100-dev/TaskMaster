package com.example.taskmasterfinalproject.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.taskmasterfinalproject.data.AuthRepository
import com.example.taskmasterfinalproject.data.PreferencesManager
import com.example.taskmasterfinalproject.db.TaskDatabase
import com.example.taskmasterfinalproject.model.User
import kotlinx.coroutines.launch

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

    fun register(username: String, pass: String) {
        if (username.isBlank() || pass.isBlank()) {
            _authResult.value = Result.failure(Exception("Please fill all fields"))
            return
        }
        if (pass.length < 4) {
             _authResult.value = Result.failure(Exception("Password too short (min 4 chars)"))
             return
        }

        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.register(username, pass)
            // Auto login on register? No, maybe just success message or auto-login.
            // Requirement says "On Login:... create session".
            // Usually Register -> Auto Login is good UX.
            // I'll auto-login (no remember me by default unless we add checkbox to register, but simpler to just return success and let user login, OR auto login without remember me)
            // Let's just return success. User can Login. 
            // Better: If register success, emit success. Activity can decide to auto-login or ask user to login.
            // I'll leave it as Result<User>. The activity can handle navigation.
            _authResult.value = result
            _isLoading.value = false
        }
    }
    
    fun changePassword(userId: Long, oldPass: String, newPass: String) {
        if (oldPass.isBlank() || newPass.isBlank()) {
            _authResult.value = Result.failure(Exception("Please fill all fields"))
            return
        }
        if (newPass.length < 4) {
             _authResult.value = Result.failure(Exception("New password too short"))
             return
        }
        
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.changePassword(userId, oldPass, newPass)
            if (result.isSuccess) {
                 // Password changed
            }
            // We use same authResult for simplicity or specialized LiveData?
            // Reuse authResult (contains User? ChangePassword returns Unit).
            // I'll make authResult more generic or add _passwordChangeResult.
            // Using _authResult with null user or just boolean? Result<User> implies User.
            // I'll add `passwordChangeResult`.
        }
    }
    
    private val _passwordChangeResult = MutableLiveData<Result<Unit>>()
    val passwordChangeResult: LiveData<Result<Unit>> = _passwordChangeResult
    
    // Updated implementation for changePassword to use new LiveData
    fun changePasswordAction(userId: Long, oldPass: String, newPass: String) {
        if (oldPass.isBlank() || newPass.isBlank()) {
            _passwordChangeResult.value = Result.failure(Exception("Please fill all fields"))
            return
        }
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.changePassword(userId, oldPass, newPass)
            _passwordChangeResult.value = result
            _isLoading.value = false
        }
    }
    
    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    fun loadUser(userId: Long) {
        viewModelScope.launch {
            val u = repository.getUser(userId)
            _user.value = u
        }
    }
}
