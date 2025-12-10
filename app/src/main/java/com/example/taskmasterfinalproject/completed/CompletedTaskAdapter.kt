package com.example.taskmasterfinalproject.completed

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmasterfinalproject.R
import com.example.taskmasterfinalproject.model.Task

class CompletedTaskAdapter(
    private var items: List<Task>,
    private val onTaskRestore: (Task) -> Unit
) : RecyclerView.Adapter<CompletedTaskViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompletedTaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_completed_task, parent, false)
        return CompletedTaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: CompletedTaskViewHolder, position: Int) {
        val task = items[position]
        holder.titleText.text = task.title ?: "Task"
        holder.descriptionText.text = task.description ?: ""

        val priorityText = when (task.priority) {
            1 -> "Priority: Low"
            2 -> "Priority: Medium"
            3 -> "Priority: High"
            else -> "Priority: None"
        }
        holder.priorityText.text = priorityText

        val dueDateLabel = if (!task.dueDate.isNullOrBlank()) {
            "Due: ${task.dueDate}"
        } else {
            ""
        }
        holder.dueDateText.text = dueDateLabel

        holder.restoreButton.setOnClickListener {
            onTaskRestore(task)
        }
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<Task>) {
        items = newItems
        notifyDataSetChanged()
    }
}


