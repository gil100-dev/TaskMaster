package com.example.taskmasterfinalproject.profile

// ספרייה ליצירת Intents
import android.content.Intent
// ספרייה להעברת נתונים (Bundle)
import android.os.Bundle
// אלמנט תצוגה
import android.view.View
// ספרייה ליצירת ViewModels
import androidx.activity.viewModels
// מחלקת בסיס ל-Activities
import androidx.appcompat.app.AppCompatActivity
// ה-ViewModel של האימות
import com.example.taskmasterfinalproject.auth.AuthViewModel
// מסך ההתחברות
import com.example.taskmasterfinalproject.auth.LoginActivity
// מחלקת ה-Binding של מסך הפרופיל
import com.example.taskmasterfinalproject.databinding.ActivityProfileBinding
// פונקציית עזר לניווט חזור
import com.example.taskmasterfinalproject.util.setupBackNavigation
// משאבי האפליקציה
import com.example.taskmasterfinalproject.R
// רכיב להצגת הודעות קצרות (Snackbar)
import com.google.android.material.snackbar.Snackbar

// מסך הפרופיל, מאפשר שינוי סיסמה והתנתקות
class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val viewModel: AuthViewModel by viewModels()
    private var userId: Long = -1

    // פונקציית ה-Lifecycle הראשית
    override fun onCreate(savedInstanceState: Bundle?) {
        com.example.taskmasterfinalproject.settings.ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // הגדרת סרגל הכלים
        setupBackNavigation(binding.toolbar, "Profile")

        val sessionUserId = com.example.taskmasterfinalproject.auth.SessionManager.currentUserId
        if (sessionUserId == null) {
            // לא אמור לקרות אם הניווט תקין, אך ליתר ביטחון
            startLoginActivity()
            return
        }
        userId = sessionUserId

        setupListeners()
        observeViewModel()
        
        viewModel.loadUser(userId)
    }

    // טיפול בניווט חזור
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    // הגדרת מאזינים לכפתורים
    private fun setupListeners() {
        binding.buttonLogout.setOnClickListener {
            com.example.taskmasterfinalproject.auth.SessionManager.clearSession()
            val prefs = com.example.taskmasterfinalproject.data.PreferencesManager(this)
            prefs.clearSession()
            startLoginActivity()
        }

        binding.buttonChangePassword.setOnClickListener {
            val oldPass = binding.editOldPassword.editText?.text.toString()
            val newPass = binding.editNewPassword.editText?.text.toString()
            viewModel.changePasswordAction(userId, oldPass, newPass)
        }
    }

    // האזנה לשינויים ב-ViewModel
    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { loading ->
             binding.progressProfile.visibility = if (loading) View.VISIBLE else View.GONE
             binding.buttonChangePassword.isEnabled = !loading
             binding.buttonLogout.isEnabled = !loading
        }

        viewModel.user.observe(this) { user ->
            binding.textUsernameValue.text = user?.username ?: "Unknown"
        }
        
        viewModel.passwordChangeResult.observe(this) { result ->
            if (result.isSuccess) {
                Snackbar.make(binding.root, getString(R.string.msg_password_changed), Snackbar.LENGTH_SHORT).show()
                binding.editOldPassword.editText?.text?.clear()
                binding.editNewPassword.editText?.text?.clear()
            } else {
                Snackbar.make(binding.root, result.exceptionOrNull()?.message ?: "Error", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    // מעבר למסך התחברות
    private fun startLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
