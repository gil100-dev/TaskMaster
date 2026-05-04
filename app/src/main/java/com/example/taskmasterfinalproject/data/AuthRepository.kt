package com.example.taskmasterfinalproject.data

// ספרייה לקידוד ופענוח נתונים בפורמט Base64 (משמש להמרת ה-Hash למחרוזת)
import android.util.Base64
// ממשק הגישה לנתוני המשתמש במסד הנתונים
import com.example.taskmasterfinalproject.db.UserDao
// המודל המייצג את המשתמש
import com.example.taskmasterfinalproject.model.User
// ספרייה ליצירת פונקציות גיבוב (Hash Functions) כגון SHA-256
import java.security.MessageDigest
// ספרייה ליצירת מספרים רנדומליים מאובטחים (Salt)
import java.security.SecureRandom
// ספרייה להגדרת המבצעים (Dispatchers) של הקורוטינות
import kotlinx.coroutines.Dispatchers
// ספרייה להחלפת ה-Context של הקורוטינה (למשל ל-IO)
import kotlinx.coroutines.withContext

// מחלקה המנהלת את הלוגיקה העסקית של אימות משתמשים (הרשמה, התחברות)
class AuthRepository(private val userDao: UserDao) {

    // פונקציה לביצוע הרשמה של משתמש חדש
    suspend fun register(username: String, password: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                if (userDao.getUserByUsername(username) != null) {
                    return@withContext Result.failure(Exception("Username already exists"))
                }

                // יצירת Salt (ערך אקראי) – מובטח שגם אם שני משתמשים בוחרים אותה סיסמה, ה-Hash יהיה שונה
                val salt = generateSalt()
                // גיבוב הסיסמה עם ה-Salt – לעולם לא שומרים סיסמה כטקסט גלוי!
                val hash = hashPassword(password, salt)
                // יצירת אובייקט User עם ה-Hash וה-Salt (לא הסיסמה המקורית)
                val user = User(username = username, passwordHash = hash, salt = salt)
                
                val id = userDao.registerUser(user)
                // החזרת המשתמש עם ה-ID שנוצר
                Result.success(user.copy(id = id))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // פונקציה לביצוע התחברות של משתמש קיים
    suspend fun login(username: String, password: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val user = userDao.getUserByUsername(username)
                if (user == null) {
                    return@withContext Result.failure(Exception("User not found"))
                }

                // חישוב Hash מהסיסמה שהוזנה + Salt השמור – ובהשוואה ל-Hash השמור
                val computedHash = hashPassword(password, user.salt)
                if (computedHash == user.passwordHash) {
                    Result.success(user)  // הסיסמאות תואמות
                } else {
                    Result.failure(Exception("Invalid password"))  // ה-Hash לא תואם
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    // פונקציה לשינוי סיסמה של משתמש
    suspend fun changePassword(userId: Long, oldPass: String, newPass: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val user = userDao.getUserById(userId) ?: return@withContext Result.failure(Exception("User not found"))
                val oldHash = hashPassword(oldPass, user.salt)
                if (oldHash != user.passwordHash) {
                    return@withContext Result.failure(Exception("Old password incorrect"))
                }
                
                val checkNewHash = hashPassword(newPass, user.salt)
                if (checkNewHash == user.passwordHash) {
                    return@withContext Result.failure(Exception("הסיסמה החדשה זהה לסיסמה הנוכחית"))
                }
                
                // יצירת Salt חדש וגיבוב חדש – חשוב להחליף גם את ה-Salt בשינוי סיסמה
                val newSalt = generateSalt()
                val newHash = hashPassword(newPass, newSalt)
                // עדכון המשתמש במסד הנתונים עם ה-Hash וה-Salt החדשים
                userDao.updateUser(user.copy(passwordHash = newHash, salt = newSalt))
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // פונקציה לקבלת פרטי משתמש לפי מזהה (ID)
    suspend fun getUser(userId: Long): User? {
        return withContext(Dispatchers.IO) {
            userDao.getUserById(userId)
        }
    }

    // פונקציה פרטית ליצירת Salt אקראי
    // פונקציה פרטית ליצירת Salt אקראי.
    // SecureRandom – מחולל מספרים אקראיים מאובטח קריפטוגרפית (לא רגיל Random!)
    // Base64 – קידוד בינארי למחרוזת קריאה שניתן לשמור במסד הנתונים
    private fun generateSalt(): String {
        val random = SecureRandom()    // מחולל מספרים קריפטוגרפי
        val salt = ByteArray(16)       // 16 בתים = 128 ביט של אקראיות
        random.nextBytes(salt)         // מילוי המערך בערכים אקראיים
        return Base64.encodeToString(salt, Base64.NO_WRAP)  // המרה למחרוזת Base64
    }

    // פונקציה פרטית לביצוע גיבוב (Hashing) של הסיסמה עם ה-Salt
    // פונקציה פרטית לביצוע גיבוב (Hashing) של הסיסמה עם ה-Salt.
    // SHA-256 – אלגוריתם גיבוב חד-כיווני (לא ניתן לחזור מ-Hash לסיסמה המקורית)
    private fun hashPassword(password: String, salt: String): String {
        val combined = password + salt         // שרשור הסיסמה עם ה-Salt
        val digest = MessageDigest.getInstance("SHA-256")  // קבלת מנגנון הגיבוב
        val hash = digest.digest(combined.toByteArray(Charsets.UTF_8))  // ביצוע הגיבוב
        return Base64.encodeToString(hash, Base64.NO_WRAP)  // המרה למחרוזת שניתן לשמור
    }
}
