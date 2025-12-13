package com.example.taskmasterfinalproject.completed

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmasterfinalproject.R
import com.google.android.material.checkbox.MaterialCheckBox

class CompletedTaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val titleText: TextView = itemView.findViewById(R.id.completed_task_title)
    val checkBox: MaterialCheckBox = itemView.findViewById(R.id.completed_task_checkbox)
}
