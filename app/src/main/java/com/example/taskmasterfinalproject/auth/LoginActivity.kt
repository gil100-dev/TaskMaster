package com.example.taskmasterfinalproject.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.taskmasterfinalproject.MainActivity
import com.example.taskmasterfinalproject.databinding.ActivityLoginBinding
import com.google.android.material.snackbar.Snackbar

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        com.example.taskmasterfinalproject.settings.ThemeHelper.applyTheme(this) // Apply theme
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.buttonLogin.setOnClickListener {
            val username = binding.editUsername.editText?.text.toString()
            val password = binding.editPassword.editText?.text.toString()
            val rememberMe = binding.checkRememberMe.isChecked
            viewModel.login(username, password, rememberMe)
        }

        binding.buttonRegister.setOnClickListener {
            // Open Register Screen
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

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

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        // Clear back stack so user can't go back to Login
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
