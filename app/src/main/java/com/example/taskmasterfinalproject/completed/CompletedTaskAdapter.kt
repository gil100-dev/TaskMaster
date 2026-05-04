package com.example.taskmasterfinalproject.completed

// ספרייה לעיצוב טקסט (למשל קו חוצה)
import android.graphics.Paint
// ספרייה לניפוח תצוגה
import android.view.LayoutInflater
// ספרייה לקבוצות תצוגה
import android.view.ViewGroup
// ספרייה לרכיב הרשימה הממוחזרת
import androidx.recyclerview.widget.RecyclerView
// משאבי האפליקציה
import com.example.taskmasterfinalproject.R
// מודל המשימה
import com.example.taskmasterfinalproject.model.Task

// מתאם (Adapter) להצגת רשימת המשימות שהושלמו
class CompletedTaskAdapter(
    private var items: List<Task>
) : RecyclerView.Adapter<CompletedTaskViewHolder>() {

    // יצירת ViewHolder חדש עבור פריט ברשימה
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompletedTaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_completed_task, parent, false)
        return CompletedTaskViewHolder(view)
    }

    // קישור נתוני המשימה לתצוגה
    override fun onBindViewHolder(holder: CompletedTaskViewHolder, position: Int) {
        val task = items[position]
        holder.titleText.text = task.title ?: "Task"
        holder.titleText.paintFlags = holder.titleText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        holder.checkBox.isChecked = true
    }

    // החזרת כמות הפריטים ברשימה
    override fun getItemCount(): Int = items.size

    // עדכון הרשימה ורענון התצוגה
    fun submitList(newItems: List<Task>) {
        items = newItems
        notifyDataSetChanged()
    }
}
