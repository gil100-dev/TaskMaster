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
        super.onCreate(savedInstanceState)
        binding = ActivityTaskDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        observeViewModel()

        binding.buttonAddSubtask.setOnClickListener {
            val title = binding.editNewSubtask.text.toString()
            if (title.isNotBlank()) {
                viewModel.addSubtask(title)
                binding.editNewSubtask.text.clear()
            }
        }
    }

    private fun setupRecyclerView() {
        subtaskAdapter = SubtaskAdapter { subtask ->
            viewModel.toggleSubtask(subtask)
        }
        binding.recyclerSubtasks.apply {
            layoutManager = LinearLayoutManager(this@TaskDetailActivity)
            adapter = subtaskAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.taskWithDetails.observe(this) { taskWithDetails ->
            taskWithDetails?.let {
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
}
