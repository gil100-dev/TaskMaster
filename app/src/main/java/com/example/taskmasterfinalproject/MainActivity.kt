package com.example.taskmasterfinalproject

import android.app.Activity
import android.content.pm.PackageManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.example.taskmasterfinalproject.addtask.AddTaskActivity
import com.example.taskmasterfinalproject.completed.CompletedTasksActivity
import com.example.taskmasterfinalproject.model.Task
import com.example.taskmasterfinalproject.main.MainViewModel
import com.example.taskmasterfinalproject.main.TaskAdapter
import com.example.taskmasterfinalproject.notifications.NotificationHelper
import com.example.taskmasterfinalproject.notifications.ReminderScheduler
import com.example.taskmasterfinalproject.settings.SettingsActivity

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var taskAdapter: TaskAdapter

    private val addTaskLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data != null) {
                val isEdit = data.getBooleanExtra(AddTaskActivity.EXTRA_IS_EDIT, false)
                val returnedId = data.getStringExtra(AddTaskActivity.EXTRA_TASK_ID)
                val title = data.getStringExtra(AddTaskActivity.EXTRA_TITLE)
                val description = data.getStringExtra(AddTaskActivity.EXTRA_DESCRIPTION)
                val dueDate = data.getStringExtra(AddTaskActivity.EXTRA_DUE_DATE)
                val priority = data.getIntExtra(AddTaskActivity.EXTRA_PRIORITY, 0)
                val dueTimeMillis = data.getLongExtra(AddTaskActivity.EXTRA_DUE_TIME_MILLIS, -1L)
                val millisOrNull = if (dueTimeMillis >= 0L) dueTimeMillis else null

                if (isEdit && returnedId != null) {
                    val updatedTask = Task(
                        id = returnedId,
                        title = title,
                        description = description,
                        dueDate = dueDate,
                        priority = priority,
                        dueTimeMillis = millisOrNull,
                        isCompleted = false
                    )
                    ReminderScheduler.cancelTaskReminder(this, updatedTask)
                    viewModel.updateTask(updatedTask)
                    ReminderScheduler.scheduleTaskReminder(this, updatedTask)
                } else {
                    val newTask = Task(
                        id = System.currentTimeMillis().toString(),
                        title = title,
                        description = description,
                        dueDate = dueDate,
                        priority = priority,
                        dueTimeMillis = millisOrNull
                    )
                    viewModel.addTask(newTask)
                    ReminderScheduler.scheduleTaskReminder(this, newTask)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        NotificationHelper.createTaskReminderChannels(this)
        requestNotificationPermissionIfNeeded()

        val buttonAddTask: Button = findViewById(R.id.button_add_task)
        val buttonOpenSettings: Button = findViewById(R.id.button_open_settings)
        val buttonViewCompleted: Button = findViewById(R.id.button_view_completed)
        val recyclerTasks: RecyclerView = findViewById(R.id.recycler_tasks)

        taskAdapter = TaskAdapter(
            emptyList(),
            onTaskCompleted = { task ->
                viewModel.markTaskCompleted(task)
            },
            onTaskClicked = { task ->
                val intent = Intent(this, AddTaskActivity::class.java).apply {
                    putExtra(AddTaskActivity.EXTRA_MODE, AddTaskActivity.EXTRA_MODE_EDIT)
                    putExtra(AddTaskActivity.EXTRA_TASK_ID, task.id)
                    putExtra(AddTaskActivity.EXTRA_TITLE, task.title)
                    putExtra(AddTaskActivity.EXTRA_DESCRIPTION, task.description)
                    putExtra(AddTaskActivity.EXTRA_DUE_DATE, task.dueDate)
                    putExtra(AddTaskActivity.EXTRA_PRIORITY, task.priority ?: 0)
                    putExtra(AddTaskActivity.EXTRA_DUE_TIME_MILLIS, task.dueTimeMillis ?: -1L)
                }
                addTaskLauncher.launch(intent)
            }
        )
        recyclerTasks.layoutManager = LinearLayoutManager(this)
        recyclerTasks.adapter = taskAdapter

        val swipeCallback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val task = taskAdapter.getTask(position)

                if (direction == ItemTouchHelper.RIGHT) {
                    viewModel.markTaskCompleted(task)
                    taskAdapter.notifyItemRemoved(position)
                }

                if (direction == ItemTouchHelper.LEFT) {
                    viewModel.deleteTask(task)
                    ReminderScheduler.cancelTaskReminder(this@MainActivity, task)

                    Snackbar.make(recyclerTasks, "Task deleted", Snackbar.LENGTH_LONG)
                        .setAction("Undo") {
                            viewModel.addTask(task)
                        }
                        .show()
                }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                if (actionState != ItemTouchHelper.ACTION_STATE_SWIPE) {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    return
                }

                val itemView = viewHolder.itemView
                val paint = Paint()
                val iconMargin = (16 * resources.displayMetrics.density).toInt()
                val iconSize = (24 * resources.displayMetrics.density).toInt()

                if (dX > 0) {
                    // Swiping right - complete
                    paint.color = Color.parseColor("#4CAF50")
                    val background = RectF(
                        itemView.left.toFloat(),
                        itemView.top.toFloat(),
                        itemView.left + dX.coerceAtMost(itemView.width.toFloat()),
                        itemView.bottom.toFloat()
                    )
                    c.drawRect(background, paint)

                    val icon = ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_swipe_complete)
                    icon?.let {
                        val iconTop = itemView.top + (itemView.height - iconSize) / 2
                        val iconLeft = itemView.left + iconMargin
                        val iconRight = iconLeft + iconSize
                        val iconBottom = iconTop + iconSize
                        it.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                        it.draw(c)
                    }
                } else if (dX < 0) {
                    // Swiping left - delete
                    paint.color = Color.parseColor("#F44336")
                    val background = RectF(
                        itemView.right + dX.coerceAtLeast(-itemView.width.toFloat()),
                        itemView.top.toFloat(),
                        itemView.right.toFloat(),
                        itemView.bottom.toFloat()
                    )
                    c.drawRect(background, paint)

                    val icon = ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_swipe_delete)
                    icon?.let {
                        val iconTop = itemView.top + (itemView.height - iconSize) / 2
                        val iconRight = itemView.right - iconMargin
                        val iconLeft = iconRight - iconSize
                        val iconBottom = iconTop + iconSize
                        it.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                        it.draw(c)
                    }
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }
        ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerTasks)

        viewModel.tasks.observe(this) { tasks ->
            taskAdapter.submitList(tasks)
        }

        buttonAddTask.setOnClickListener {
            val intent = Intent(this, AddTaskActivity::class.java)
            addTaskLauncher.launch(intent)
        }

        buttonOpenSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        buttonViewCompleted.setOnClickListener {
            val intent = Intent(this, CompletedTasksActivity::class.java)
            startActivity(intent)
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }
}