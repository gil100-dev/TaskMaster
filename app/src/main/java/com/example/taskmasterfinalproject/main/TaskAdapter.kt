package com.example.taskmasterfinalproject.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmasterfinalproject.databinding.ItemTaskBinding
import com.example.taskmasterfinalproject.model.Task

class TaskAdapter(
    private var items: List<Task>,
    private val onTaskClick: (Task) -> Unit, // New callback
    private val onTaskComplete: (Task) -> Unit,
    private val onTaskDelete: (Task) -> Unit
) : RecyclerView.Adapter<TaskViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = items[position]
        with(holder.binding) {
            textTaskTitle.text = task.title ?: "Task"
            textTaskDescription.text = task.description ?: ""
            textTaskPriority.text = when (task.priority) {
                1 -> "High"
                2 -> "Medium"
                3 -> "Low"
                else -> "Normal"
            }
            
            // Set Color based on Priority
            // Set Color based on Priority
            val colorRes = when (task.priority) {
                1 -> android.R.color.holo_red_light
                2 -> android.R.color.holo_orange_light
                else -> android.R.color.holo_green_light
            }
            
            // Apply to Chip Background
            textTaskPriority.setChipBackgroundColorResource(colorRes)
            
            // Tint icon white for better contrast if needed, or leave default
            // textTaskPriority.chipIconTint = android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE)
            textTaskDueDate.text = if (task.dueDate.isNullOrBlank()) "" else "Due: ${task.dueDate}"

            buttonCompleteTask.setOnClickListener {
                onTaskComplete(task)
            }

            // Set click listener on the whole item view
            root.setOnClickListener {
                onTaskClick(task)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun getTask(position: Int): Task {
        return items[position]
    }

    fun submitList(newItems: List<Task>) {
        items = newItems
        notifyDataSetChanged()
    }
}
