package com.example.taskmasterfinalproject.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmasterfinalproject.R
import com.example.taskmasterfinalproject.model.Task

class TaskAdapter(
    private var items: List<Task>,
    private val onTaskClick: (Task) -> Unit, // New callback
    private val onTaskComplete: (Task) -> Unit,
    private val onTaskDelete: (Task) -> Unit
) : RecyclerView.Adapter<TaskViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = items[position]
        holder.titleText.text = task.title ?: "Task"
        holder.descriptionText.text = task.description ?: ""
        holder.priorityText.text = when (task.priority) {
            1 -> "Low"
            2 -> "Medium"
            3 -> "High"
            else -> "None"
        }
        holder.dueDateText.text = if (task.dueDate.isNullOrBlank()) "" else "Due: ${task.dueDate}"

        holder.completeButton.setOnClickListener {
            onTaskComplete(task)
        }

        // Set click listener on the whole item view
        holder.itemView.setOnClickListener {
            onTaskClick(task)
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
