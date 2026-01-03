package com.example.taskmasterfinalproject

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmasterfinalproject.addtask.AddTaskActivity
import com.example.taskmasterfinalproject.databinding.ActivityMainBinding
import com.example.taskmasterfinalproject.details.TaskDetailActivity
import com.example.taskmasterfinalproject.main.MainViewModel
import com.example.taskmasterfinalproject.main.TaskAdapter
import com.example.taskmasterfinalproject.model.Task
import com.example.taskmasterfinalproject.notifications.NotificationHelper
import com.example.taskmasterfinalproject.notifications.ReminderScheduler
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        com.example.taskmasterfinalproject.settings.ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
        
        // Session Check
        // Priority: In-Memory (just logged in) -> Persistent (Remember Me)
        // If In-Memory is set, we are good.
        if (!com.example.taskmasterfinalproject.auth.SessionManager.isLoggedIn()) {
             // Not in memory, check prefs
             val prefs = com.example.taskmasterfinalproject.data.PreferencesManager(this)
             val savedId = prefs.getSessionUserId()
             if (savedId != null) {
                 // Found valid saved session, load into memory
                 com.example.taskmasterfinalproject.auth.SessionManager.startSession(savedId)
             } else {
                 // No session anywhere
                 startActivity(Intent(this, com.example.taskmasterfinalproject.auth.LoginActivity::class.java))
                 finish()
                 return
             }
        }
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar) // Ensure Toolbar is set

        ViewCompat.setOnApplyWindowInsetsListener(binding.mainRoot) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0) // Don't pad bottom, let BottomNav handle it
            insets
        }

        NotificationHelper.createTaskReminderChannels(this)
        requestNotificationPermissionIfNeeded()
        ReminderScheduler.requestExactAlarmPermissionIfNeeded(this)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, com.example.taskmasterfinalproject.main.TaskListFragment())
                .commit()
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_tasks -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, com.example.taskmasterfinalproject.main.TaskListFragment())
                        .commit()
                    true
                }
                R.id.nav_statistics -> {
                    // Navigate to Statistics Fragment
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, com.example.taskmasterfinalproject.statistics.StatisticsFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }
        

    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                com.example.taskmasterfinalproject.settings.SettingsBottomSheet.newInstance()
                    .show(supportFragmentManager, com.example.taskmasterfinalproject.settings.SettingsBottomSheet.TAG)
                true
            }
            R.id.action_profile -> {
                startActivity(Intent(this, com.example.taskmasterfinalproject.profile.ProfileActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
