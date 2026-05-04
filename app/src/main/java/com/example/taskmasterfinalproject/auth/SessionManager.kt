package com.example.taskmasterfinalproject.auth

// אובייקט עזר ("סינגלטון") לניהול מושב (Session) המשתמש בזיכרון
object SessionManager {
    // מזהה המשתמש הנוכחי, נגיש לקריאה ופרטי לכתיבה
    var currentUserId: Long? = null
        private set
        
    // התחלת מושב חדש עם מזהה משתמש
    fun startSession(userId: Long) {
        currentUserId = userId
    }
    
    // ניקוי המושב (התנתקות)
    fun clearSession() {
        currentUserId = null
    }
    
    // בדיקה האם המשתמש מחובר
    fun isLoggedIn(): Boolean = currentUserId != null
}
