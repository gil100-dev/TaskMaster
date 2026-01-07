package com.example.taskmasterfinalproject.auth

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.taskmasterfinalproject.R
import com.example.taskmasterfinalproject.databinding.ActivityRegisterBinding
import com.example.taskmasterfinalproject.util.setupBackNavigation
import com.google.android.material.snackbar.Snackbar

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        com.example.taskmasterfinalproject.settings.ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Toolbar
        setupBackNavigation(binding.toolbar, getString(R.string.action_register))

        setupListeners()
        observeViewModel()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun setupListeners() {
        binding.buttonRegisterAction.setOnClickListener {
            val username = binding.editUsername.editText?.text.toString()
            val password = binding.editPassword.editText?.text.toString()
            val confirm = binding.editConfirmPassword.editText?.text.toString()
            
            if (password != confirm) {
                 binding.editConfirmPassword.error = "Passwords do not match"
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

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.buttonRegisterAction.isEnabled = !isLoading
            binding.buttonBackLogin.isEnabled = !isLoading
        }

        viewModel.authResult.observe(this) { result ->
            if (result.isSuccess) {
                // Registration successful.
                // We can navigate back to login.
                 android.widget.Toast.makeText(this, "Account created! Please login.", android.widget.Toast.LENGTH_LONG).show()
                 finish()
            } else {
                Snackbar.make(binding.root, result.exceptionOrNull()?.message ?: "Error", Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}
