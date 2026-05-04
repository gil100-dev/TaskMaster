package com.example.taskmasterfinalproject.model

// ספרייה להגדרת ישות במסד הנתונים (טבלה)
import androidx.room.Entity
// ספרייה להגדרת אינדקסים לשיפור ביצועי שליפה
import androidx.room.Index
// ספרייה להגדרת מפתח ראשי
import androidx.room.PrimaryKey

// מודל המייצג משתמש במערכת.
// @Entity עם indices מגדיר אינדקס ייחודי על שם המשתמש – מונע כפילויות ומאיץ חיפוש
@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true)]  // אינדקס ייחודי – מונע שני משתמשים עם אותו שם
)
data class User(
    @PrimaryKey(autoGenerate = true)    // מפתח ראשי שנוצר אוטומטית (Auto Increment)
    val id: Long = 0,                   // מזהה ייחודי של המשתמש
    val username: String,                // שם המשתמש (ייחודי)
    val passwordHash: String,            // גיבוב (Hash) הסיסמה – לא שומרים את הסיסמה עצמה מטעמי אבטחה!
    val salt: String                     // ערך אקראי (Salt) שמצורף לסיסמה לפני הגיבוב – מגן מפני התקפות Rainbow Table
)
