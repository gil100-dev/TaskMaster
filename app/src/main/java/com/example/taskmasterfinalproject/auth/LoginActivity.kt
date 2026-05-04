package com.example.taskmasterfinalproject.auth

// ספרייה ליצירת Intents
import android.content.Intent
// ספרייה להעברת נתונים בין רכיבים (Bundle)
import android.os.Bundle
// אלמנט תצוגה
import android.view.View
// ספרייה ליצירת ViewModels
import androidx.activity.viewModels
// מחלקת בסיס ל-Activities
import androidx.appcompat.app.AppCompatActivity
// ה-Activity הראשי
import com.example.taskmasterfinalproject.MainActivity
// מחלקת ה-Binding של מסך ההתחברות
import com.example.taskmasterfinalproject.databinding.ActivityLoginBinding
// רכיב להצגת הודעות קצרות (Snackbar)
import com.google.android.material.snackbar.Snackbar

// מסך התחברות
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()

    // פונקציית ה-Lifecycle הראשית
    override fun onCreate(savedInstanceState: Bundle?) {
        com.example.taskmasterfinalproject.settings.ThemeHelper.applyTheme(this) // החלת ערכת הנושא
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        observeViewModel()
    }

    // הגדרת מאזינים לכפתורים
    private fun setupListeners() {
        binding.buttonLogin.setOnClickListener {
            val username = binding.editUsername.editText?.text.toString()
            val password = binding.editPassword.editText?.text.toString()
            val rememberMe = binding.checkRememberMe.isChecked
            viewModel.login(username, password, rememberMe)
        }

        binding.buttonRegister.setOnClickListener {
            // מעבר למסך הרשמה
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    // האזנה לשינויים ב-ViewModel
    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.buttonLogin.isEnabled = !isLoading
            binding.buttonRegister.isEnabled = !isLoading
        }

        viewModel.authResult.observe(this) { result ->
            if (result.isSuccess) {
                startMainActivity()
            } else {
                Snackbar.make(binding.root, result.exceptionOrNull()?.message ?: "Error", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    // מעבר למסך הראשי
    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        // ניקוי ערימת המשימות כך שהמשתמש לא יוכל לחזור ב-Back למסך ההתחברות
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
