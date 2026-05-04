package com.example.taskmasterfinalproject.db

// ספרייה המגדירה כי הממשק הוא DAO (Data Access Object)
import androidx.room.Dao
// ספרייה להגדרת פעולת הוספה
import androidx.room.Insert
// ספרייה להגדרת אסטרטגיה במקרה של התנגשות (כגון ביטול)
import androidx.room.OnConflictStrategy
// ספרייה להגדרת שאילתת SQL מותאמת אישית
import androidx.room.Query
// ספרייה להגדרת פעולת עדכון
import androidx.room.Update
// המודל המייצג משתמש
import com.example.taskmasterfinalproject.model.User

// ממשק לגישה לנתוני המשתמשים
@Dao
interface UserDao {
    // פונקציה לרישום משתמש חדש (מחזירה את ה-ID החדש)
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun registerUser(user: User): Long

    // פונקציה למציאת משתמש לפי שם משתמש
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    // פונקציה למציאת משתמש לפי מזהה (ID)
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Long): User?

    // פונקציה לעדכון פרטי משתמש
    @Update
    suspend fun updateUser(user: User)
}
