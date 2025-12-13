package com.example.taskmasterfinalproject.completed

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmasterfinalproject.R
import com.example.taskmasterfinalproject.model.Task

class CompletedTaskAdapter(
    private var items: List<Task>
) : RecyclerView.Adapter<CompletedTaskViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompletedTaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_completed_task, parent, false)
        return CompletedTaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: CompletedTaskViewHolder, position: Int) {
        val task = items[position]
        holder.titleText.text = task.title ?: "Task"
        holder.titleText.paintFlags = holder.titleText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        holder.checkBox.isChecked = true
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<Task>) {
        items = newItems
        notifyDataSetChanged()
    }
}
