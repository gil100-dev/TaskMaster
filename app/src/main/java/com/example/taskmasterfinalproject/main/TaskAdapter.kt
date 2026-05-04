package com.example.taskmasterfinalproject.main

// ספרייה לניהול יצירת Views
import android.view.LayoutInflater
// ספרייה המייצגת קבוצת Views
import android.view.ViewGroup
// ספרייה לניהול רשימות נגללות
import androidx.recyclerview.widget.RecyclerView
// מחלקת ה-Binding עבור פריט משימה
import com.example.taskmasterfinalproject.databinding.ItemTaskBinding
// המודל המייצג משימה
import com.example.taskmasterfinalproject.model.Task

// מתאם (Adapter) לרשימת המשימות הראשיים
class TaskAdapter(
    private var items: List<Task>,
    private val onTaskClick: (Task) -> Unit, // Callback ללחיצה על משימה
    private val onTaskComplete: (Task) -> Unit,
    private val onTaskDelete: (Task) -> Unit,
    private val onTaskSpeak: (Task) -> Unit // Callback להקראת משימה בקול
) : RecyclerView.Adapter<TaskViewHolder>() {

    // פונקציה ליצירת ViewHolder חדש
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    // פונקציה לקישור נתונים לתצוגה
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = items[position]
        with(holder.binding) {
            textTaskTitle.text = task.title ?: "Task"
            textTaskDescription.text = task.description ?: ""
            textTaskPriority.text = when (task.priority) {
                1 -> "נמוכה"
                2 -> "בינונית"
                3 -> "גבוהה"
                else -> "רגילה"
            }
            
            // קביעת צבע בהתאם לעדיפות
            val colorRes = when (task.priority) {
                3 -> android.R.color.holo_red_light
                2 -> android.R.color.holo_orange_light
                else -> android.R.color.holo_green_light
            }
            
            // החלת הצבע על רקע התגית (Chip)
            textTaskPriority.setChipBackgroundColorResource(colorRes)
            
            textTaskDueDate.text = if (task.dueDate.isNullOrBlank()) "" else "Due: ${task.dueDate}"

            buttonCompleteTask.setOnClickListener {
                onTaskComplete(task)
            }

            // הגדרת מאזין לכפתור ההקראה
            buttonSpeakTask.setOnClickListener {
                onTaskSpeak(task)
            }

            // הגדרת מאזין ללחיצה על כל הפריט
            root.setOnClickListener {
                onTaskClick(task)
            }
        }
    }

    // פונקציה להחזרת כמות הפריטים ברשימה
    override fun getItemCount(): Int = items.size

    // פונקציה לקבלת פריט במיקום ספציפי
    fun getTask(position: Int): Task {
        return items[position]
    }

    // פונקציה לעדכון הרשימה כולה
    fun submitList(newItems: List<Task>) {
        items = newItems
        notifyDataSetChanged()
    }
}
