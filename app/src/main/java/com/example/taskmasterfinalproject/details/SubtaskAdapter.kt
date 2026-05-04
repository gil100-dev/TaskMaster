package com.example.taskmasterfinalproject.details

// ספרייה לעיבוד גרפי וטקסט (כגון קו חוצה)
import android.graphics.Paint
// ספרייה ליצירת View מתוך קובץ ה-XML (Layout Inflation)
import android.view.LayoutInflater
// ספרייה המייצגת קבוצת Views (Container)
import android.view.ViewGroup
// ספרייה לחישוב ההבדלים בין רשימות לצורך עדכון יעיל
import androidx.recyclerview.widget.DiffUtil
// ספרייה מתקדמת לניהול רשימות המבוססת על DiffUtil
import androidx.recyclerview.widget.ListAdapter
// ספרייה ליצירת רשימות נגללות
import androidx.recyclerview.widget.RecyclerView
// מחלקת ה-Binding עבור פריט תת-משימה
import com.example.taskmasterfinalproject.databinding.ItemSubtaskBinding
// המודל המייצג תת-משימה
import com.example.taskmasterfinalproject.model.Subtask

// מתאם (Adapter) לרשימת תת-משימות, אחראי על קישור הנתונים לתצוגה
class SubtaskAdapter(
    // פונקציית callback לטיפול בלחיצה על תת-משימה (שינוי סטטוס)
    private val onSubtaskToggled: (Subtask) -> Unit,
    // פונקציית callback לטיפול במחיקת תת-משימה
    private val onSubtaskDeleted: (Subtask) -> Unit
) :
    ListAdapter<Subtask, SubtaskAdapter.SubtaskViewHolder>(SubtaskDiffCallback()) {

    // פונקציה ליצירת ViewHolder חדש עבור פריט ברשימה
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubtaskViewHolder {
        val binding = ItemSubtaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SubtaskViewHolder(binding)
    }

    // פונקציה לקישור נתונים ל-ViewHolder במיקום מסוים
    override fun onBindViewHolder(holder: SubtaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // מחלקה פנימית המייצגת את התצוגה של פריט תת-משימה בודד
    inner class SubtaskViewHolder(private val binding: ItemSubtaskBinding) : RecyclerView.ViewHolder(binding.root) {
        // פונקציה המקשרת את נתוני המשימה לרכיבי התצוגה
        fun bind(subtask: Subtask) {
            binding.checkboxSubtask.text = subtask.title
            binding.checkboxSubtask.isChecked = subtask.isDone

            if (subtask.isDone) {
                binding.checkboxSubtask.paintFlags = binding.checkboxSubtask.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                binding.checkboxSubtask.paintFlags = binding.checkboxSubtask.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

            binding.checkboxSubtask.setOnClickListener {
                onSubtaskToggled(subtask)
            }
            
            binding.buttonDeleteSubtask.setOnClickListener {
                onSubtaskDeleted(subtask)
            }
        }
    }

    // מחלקה פנימית לחישוב יעיל של שינויים ברשימה
    private class SubtaskDiffCallback : DiffUtil.ItemCallback<Subtask>() {
        // בדיקה האם שני פריטים הם אותו פריט (לפי מזהה)
        override fun areItemsTheSame(oldItem: Subtask, newItem: Subtask): Boolean {
            return oldItem.id == newItem.id
        }

        // בדיקה האם תוכן הפריטים זהה
        override fun areContentsTheSame(oldItem: Subtask, newItem: Subtask): Boolean {
            return oldItem == newItem
        }
    }
}
