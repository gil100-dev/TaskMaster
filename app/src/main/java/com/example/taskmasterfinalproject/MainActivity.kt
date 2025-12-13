package com.example.taskmasterfinalproject

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmasterfinalproject.addtask.AddTaskActivity
import com.example.taskmasterfinalproject.details.TaskDetailActivity
import com.example.taskmasterfinalproject.main.MainViewModel
import com.example.taskmasterfinalproject.main.TaskAdapter
import com.example.taskmasterfinalproject.model.Task
import com.example.taskmasterfinalproject.notifications.NotificationHelper
import com.example.taskmasterfinalproject.notifications.ReminderScheduler
// import com.example.taskmasterfinalproject.settings.SettingsActivity
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var taskAdapter: TaskAdapter

    private val addTaskLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data != null) {
                val title = data.getStringExtra(AddTaskActivity.EXTRA_TITLE)
                val description = data.getStringExtra(AddTaskActivity.EXTRA_DESCRIPTION)
                val dueDate = data.getStringExtra(AddTaskActivity.EXTRA_DUE_DATE)
                val priority = data.getIntExtra(AddTaskActivity.EXTRA_PRIORITY, 0)
                // Correctly get the time in millis using the new key
                val dueTimeMillis = data.getLongExtra(AddTaskActivity.EXTRA_DUE_TIME_MILLIS, -1L)
                val millisOrNull = if (dueTimeMillis > 0) dueTimeMillis else null

                val newTask = Task(
                    id = System.currentTimeMillis().toString(),
                    title = title,
                    description = description,
                    dueDate = dueDate,
                    priority = priority,
                    dueTimeMillis = millisOrNull
                )
                viewModel.addTask(newTask)
                if (millisOrNull != null) {
                    ReminderScheduler.scheduleTaskReminder(this, newTask)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        NotificationHelper.createTaskReminderChannels(this)
        requestNotificationPermissionIfNeeded()
        ReminderScheduler.requestExactAlarmPermissionIfNeeded(this)

        val buttonAddTask: Button = findViewById(R.id.button_add_task)
        val buttonOpenSettings: Button = findViewById(R.id.button_open_settings)
        val recyclerTasks: RecyclerView = findViewById(R.id.recycler_tasks)

        taskAdapter = TaskAdapter(
            emptyList(),
            onTaskClick = { task ->
                val intent = Intent(this, TaskDetailActivity::class.java)
                intent.putExtra("TASK_ID", task.id)
                startActivity(intent)
            },
            onTaskComplete = { task ->
                viewModel.markTaskCompleted(task)
                Snackbar.make(recyclerTasks, "Task completed", Snackbar.LENGTH_LONG)
                    .setAction("UNDO") { viewModel.addTask(task) }
                    .show()
            },
            onTaskDelete = { task ->
                viewModel.deleteTask(task)
                Snackbar.make(recyclerTasks, "Task deleted", Snackbar.LENGTH_LONG)
                    .setAction("UNDO") { viewModel.addTask(task) }
                    .show()
            }
        )
        recyclerTasks.layoutManager = LinearLayoutManager(this)
        recyclerTasks.adapter = taskAdapter

        setupSwipeToDelete(recyclerTasks)

        viewModel.tasks.observe(this) { tasks ->
            taskAdapter.submitList(tasks)
        }

        buttonAddTask.setOnClickListener {
            val intent = Intent(this, AddTaskActivity::class.java)
            addTaskLauncher.launch(intent)
        }

        buttonOpenSettings.setOnClickListener {
            // val intent = Intent(this, SettingsActivity::class.java)
            // startActivity(intent)
        }
    }

    private fun setupSwipeToDelete(recyclerView: RecyclerView) {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val task = taskAdapter.getTask(position)

                if (direction == ItemTouchHelper.LEFT) {
                    viewModel.deleteTask(task)
                    Snackbar.make(recyclerView, "Task deleted", Snackbar.LENGTH_LONG)
                        .setAction("UNDO") { viewModel.addTask(task) }
                        .show()
                } else if (direction == ItemTouchHelper.RIGHT) {
                    viewModel.markTaskCompleted(task)
                    Snackbar.make(recyclerView, "Task completed", Snackbar.LENGTH_LONG)
                        .setAction("UNDO") { viewModel.addTask(task) }
                        .show()
                }
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
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
}
