package com.example.taskmasterfinalproject.main

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmasterfinalproject.R

class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val titleText: TextView = itemView.findViewById(R.id.text_task_title)
    val descriptionText: TextView = itemView.findViewById(R.id.text_task_description)
    val priorityText: TextView = itemView.findViewById(R.id.text_task_priority)
    val dueDateText: TextView = itemView.findViewById(R.id.text_task_due_date)
    val completeButton: Button = itemView.findViewById(R.id.button_complete_task)
}


