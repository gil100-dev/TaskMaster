package com.example.taskmasterfinalproject.main

// ספרייה ליצירת רשימות נגללות יעילות (RecyclerView)
import androidx.recyclerview.widget.RecyclerView
// מחלקת ה-Binding שנוצרה עבור קובץ ה-XML של פריט המשימה (item_task.xml)
import com.example.taskmasterfinalproject.databinding.ItemTaskBinding

// מחלקה המחזיקה את ה-View עבור פריט משימה בודד ברשימה, יורשת מ-RecyclerView.ViewHolder
class TaskViewHolder(val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root)
