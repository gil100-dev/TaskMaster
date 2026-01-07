package com.example.taskmasterfinalproject.auth

object SessionManager {
    var currentUserId: Long? = null
        private set
        
    fun startSession(userId: Long) {
        currentUserId = userId
    }
    
    fun clearSession() {
        currentUserId = null
    }
    
    fun isLoggedIn(): Boolean = currentUserId != null
}
