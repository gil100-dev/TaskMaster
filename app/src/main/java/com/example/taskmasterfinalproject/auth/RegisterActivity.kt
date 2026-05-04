package com.example.taskmasterfinalproject.auth

// ספרייה להעברת נתונים בין רכיבים (Bundle)
import android.os.Bundle
// אלמנט תצוגה
import android.view.View
// ספרייה ליצירת ViewModels
import androidx.activity.viewModels
// מחלקת בסיס ל-Activities
import androidx.appcompat.app.AppCompatActivity
// משאבי האפליקציה
import com.example.taskmasterfinalproject.R
// מחלקת ה-Binding של מסך ההרשמה
import com.example.taskmasterfinalproject.databinding.ActivityRegisterBinding
// פונקציית עזר לניווט חזור
import com.example.taskmasterfinalproject.util.setupBackNavigation
// רכיב להצגת הודעות קצרות (Snackbar)
import com.google.android.material.snackbar.Snackbar

// מסך הרשמה למשתמש חדש
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels()

    // פונקציית ה-Lifecycle הראשית
    override fun onCreate(savedInstanceState: Bundle?) {
        com.example.taskmasterfinalproject.settings.ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // הגדרת סרגל הכלים
        setupBackNavigation(binding.toolbar, getString(R.string.action_register))

        setupListeners()
        observeViewModel()
    }

    // טיפול בניווט חזור
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    // הגדרת מאזינים לכפתורים
    private fun setupListeners() {
        binding.buttonRegisterAction.setOnClickListener {
            val username = binding.editUsername.editText?.text.toString()
            val password = binding.editPassword.editText?.text.toString()
            val confirm = binding.editConfirmPassword.editText?.text.toString()
            
            if (password != confirm) {
                 binding.editConfirmPassword.error = getString(R.string.error_password_mismatch)
                 return@setOnClickListener
            } else {
                 binding.editConfirmPassword.error = null
            }

            viewModel.register(username, password)
        }

        binding.buttonBackLogin.setOnClickListener {
            finish()
        }
    }

    // האזנה לשינויים ב-ViewModel
    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.buttonRegisterAction.isEnabled = !isLoading
            binding.buttonBackLogin.isEnabled = !isLoading
        }

        viewModel.authResult.observe(this) { result ->
            if (result.isSuccess) {
                // הרשמה הצליחה
                // נווט חזרה למסך ההתחברות
                 android.widget.Toast.makeText(this, getString(R.string.msg_register_success), android.widget.Toast.LENGTH_LONG).show()
                 finish()
            } else {
                Snackbar.make(binding.root, result.exceptionOrNull()?.message ?: "Error", Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}
