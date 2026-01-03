package com.example.taskmasterfinalproject.details

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taskmasterfinalproject.data.TaskRepository
import com.example.taskmasterfinalproject.db.TaskDatabase
import com.example.taskmasterfinalproject.databinding.ActivityTaskDetailBinding
import com.example.taskmasterfinalproject.util.setupBackNavigation

class TaskDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTaskDetailBinding
    private lateinit var subtaskAdapter: SubtaskAdapter

    private val viewModel: TaskDetailViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val taskDao = TaskDatabase.getInstance(applicationContext).taskDao()
                val repository = TaskRepository(taskDao)
                val taskId = intent.getStringExtra("TASK_ID") ?: ""
                @Suppress("UNCHECKED_CAST")
                return TaskDetailViewModel(repository, taskId) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        com.example.taskmasterfinalproject.settings.ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityTaskDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Toolbar
        setupBackNavigation(binding.toolbar, "Task Details")

        val taskId = intent.getStringExtra(EXTRA_TASK_ID)
        if (taskId == null) {
            android.widget.Toast.makeText(this, "Error: Task not found", android.widget.Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupRecyclerView()
        observeViewModel()

        binding.buttonAddSubtask.setOnClickListener {
            val title = binding.editNewSubtask.text.toString()
            if (title.isNotBlank()) {
                viewModel.addSubtask(title)
                binding.editNewSubtask.text.clear()
                hideKeyboard()
            }
        }
        
        binding.fabAiDetail.setOnClickListener {
            val subtasks = viewModel.taskWithDetails.value?.subtasks?.map { it.title } ?: emptyList()
            val fragment = com.example.taskmasterfinalproject.ai.AiCoachBottomSheet.newInstance(currentTask, subtasks)
            fragment.onApplySubtasks = { generatedSubtasks ->
                generatedSubtasks.forEach { sub ->
                     // Appending duration to title for visibility
                    viewModel.addSubtask("${sub.title} (~${sub.minutes}m)")
                }
            }
            fragment.show(supportFragmentManager, com.example.taskmasterfinalproject.ai.AiCoachBottomSheet.TAG)
        }
    }
    
    private var currentTask: com.example.taskmasterfinalproject.model.Task? = null

    private fun setupRecyclerView() {
        subtaskAdapter = SubtaskAdapter(
            onSubtaskToggled = { subtask ->
                viewModel.toggleSubtask(subtask)
            },
            onSubtaskDeleted = { subtask ->
                viewModel.deleteSubtask(subtask)
                com.google.android.material.snackbar.Snackbar.make(binding.root, "Subtask deleted", com.google.android.material.snackbar.Snackbar.LENGTH_LONG)
                    .setAction("UNDO") {
                        viewModel.restoreSubtask(subtask)
                    }
                    .show()
            }
        )
        binding.recyclerSubtasks.apply {
            layoutManager = LinearLayoutManager(this@TaskDetailActivity)
            adapter = subtaskAdapter
        }
    }
    
    private fun hideKeyboard() {
        val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as? android.view.inputmethod.InputMethodManager
        currentFocus?.let {
            imm?.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    private fun observeViewModel() {
        viewModel.taskWithDetails.observe(this) { taskWithDetails ->
            taskWithDetails?.let {
                currentTask = it.task
                binding.textTaskTitle.text = it.task.title
                subtaskAdapter.submitList(it.subtasks)

                val doneCount = it.subtasks.count { s -> s.isDone }
                val totalCount = it.subtasks.size
                binding.textProgress.text = "$doneCount / $totalCount"
                binding.progressSubtasks.max = totalCount
                binding.progressSubtasks.progress = doneCount

                binding.chipGroupTags.removeAllViews()
                it.tags.forEach { tag ->
                    val chip = com.google.android.material.chip.Chip(this)
                    chip.text = tag.name
                    binding.chipGroupTags.addView(chip)
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    companion object {
        const val EXTRA_TASK_ID = "TASK_ID"
    }
}
