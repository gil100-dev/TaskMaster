package com.example.taskmasterfinalproject.data

import android.util.Base64
import com.example.taskmasterfinalproject.db.UserDao
import com.example.taskmasterfinalproject.model.User
import java.security.MessageDigest
import java.security.SecureRandom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(private val userDao: UserDao) {

    suspend fun register(username: String, password: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                if (userDao.getUserByUsername(username) != null) {
                    return@withContext Result.failure(Exception("Username already exists"))
                }

                val salt = generateSalt()
                val hash = hashPassword(password, salt)
                val user = User(username = username, passwordHash = hash, salt = salt)
                
                val id = userDao.registerUser(user)
                // Return User with ID populated
                Result.success(user.copy(id = id))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun login(username: String, password: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val user = userDao.getUserByUsername(username)
                if (user == null) {
                    return@withContext Result.failure(Exception("User not found"))
                }

                val computedHash = hashPassword(password, user.salt)
                if (computedHash == user.passwordHash) {
                    Result.success(user)
                } else {
                    Result.failure(Exception("Invalid password"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
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
                
                val newSalt = generateSalt()
                val newHash = hashPassword(newPass, newSalt)
                userDao.updateUser(user.copy(passwordHash = newHash, salt = newSalt))
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }



    suspend fun getUser(userId: Long): User? {
        return withContext(Dispatchers.IO) {
            userDao.getUserById(userId)
        }
    }

    private fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return Base64.encodeToString(salt, Base64.NO_WRAP)
    }

    private fun hashPassword(password: String, salt: String): String {
        val combined = password + salt
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(combined.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }
}
