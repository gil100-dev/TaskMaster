package com.example.taskmasterfinalproject.completed

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmasterfinalproject.R

class CompletedTaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val titleText: TextView = itemView.findViewById(R.id.text_completed_task_title)
    val descriptionText: TextView = itemView.findViewById(R.id.text_completed_task_description)
    val priorityText: TextView = itemView.findViewById(R.id.text_completed_task_priority)
    val dueDateText: TextView = itemView.findViewById(R.id.text_completed_task_due_date)
    val restoreButton: Button = itemView.findViewById(R.id.button_restore_task)
}


