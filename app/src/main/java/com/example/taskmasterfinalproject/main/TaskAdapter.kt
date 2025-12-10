package com.example.taskmasterfinalproject.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmasterfinalproject.R
import com.example.taskmasterfinalproject.model.Task
import com.google.android.material.card.MaterialCardView

class TaskAdapter(
    private var items: List<Task>,
    private val onTaskCompleted: (Task) -> Unit,
    private val onTaskClicked: (Task) -> Unit
) : RecyclerView.Adapter<TaskViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = items[position]
        holder.titleText.text = task.title ?: "Task Title Placeholder"
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

        holder.completeButton.setOnClickListener {
            onTaskCompleted(task)
        }

        holder.itemView.setOnClickListener {
            onTaskClicked(task)
        }

        val cardView = holder.itemView as? MaterialCardView
        val colorRes = when (task.priority) {
            3 -> R.color.task_card_high_bg
            2 -> R.color.task_card_medium_bg
            1 -> R.color.task_card_low_bg
            else -> android.R.color.white
        }
        cardView?.setCardBackgroundColor(
            ContextCompat.getColor(holder.itemView.context, colorRes)
        )
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<Task>) {
        items = newItems
        notifyDataSetChanged()
    }

    fun getTask(position: Int): Task = items[position]
}


