package com.example.taskmasterfinalproject.completed

// מחלקת הבסיס לתצוגה
import android.view.View
// רכיב טקסט
import android.widget.TextView
// ספרייה לרכיב הרשימה הממוחזרת
import androidx.recyclerview.widget.RecyclerView
// משאבי האפליקציה
import com.example.taskmasterfinalproject.R
// רכיב תיבת סמן (Checkbox) בעיצוב Material
import com.google.android.material.checkbox.MaterialCheckBox

// מחלקה המחזיקה את רכיבי התצוגה עבור פריט משימה שהושלמה
class CompletedTaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val titleText: TextView = itemView.findViewById(R.id.completed_task_title)
    val checkBox: MaterialCheckBox = itemView.findViewById(R.id.completed_task_checkbox)
}
