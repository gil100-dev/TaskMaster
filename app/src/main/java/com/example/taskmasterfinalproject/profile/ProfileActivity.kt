package com.example.taskmasterfinalproject.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.taskmasterfinalproject.auth.AuthViewModel
import com.example.taskmasterfinalproject.auth.LoginActivity
import com.example.taskmasterfinalproject.databinding.ActivityProfileBinding
import com.example.taskmasterfinalproject.util.setupBackNavigation
import com.google.android.material.snackbar.Snackbar

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val viewModel: AuthViewModel by viewModels()
    private var userId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        com.example.taskmasterfinalproject.settings.ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Toolbar
        setupBackNavigation(binding.toolbar, "Profile")

        val sessionUserId = com.example.taskmasterfinalproject.auth.SessionManager.currentUserId
        if (sessionUserId == null) {
            // Should not happen if MainActivity protects flow, but just in case
            startLoginActivity()
            return
        }
        userId = sessionUserId

        setupListeners()
        observeViewModel()
        
        viewModel.loadUser(userId)
        viewModel.loadUser(userId)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

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
                Snackbar.make(binding.root, "Password changed successfully", Snackbar.LENGTH_SHORT).show()
                binding.editOldPassword.editText?.text?.clear()
                binding.editNewPassword.editText?.text?.clear()
            } else {
                Snackbar.make(binding.root, result.exceptionOrNull()?.message ?: "Error", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun startLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
