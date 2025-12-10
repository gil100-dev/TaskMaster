package com.example.taskmasterfinalproject.completed

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmasterfinalproject.R

class CompletedTasksActivity : AppCompatActivity() {

    private val viewModel: CompletedTasksViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_completed_tasks)

        val recycler: RecyclerView = findViewById(R.id.recycler_completed_tasks)
        val adapter = CompletedTaskAdapter(
            emptyList(),
            onTaskRestore = { task ->
                viewModel.restoreTask(task)
            }
        )
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        viewModel.completedTasks.observe(this) { tasks ->
            adapter.submitList(tasks)
        }

        viewModel.refreshCompletedTasks()
    }
}


