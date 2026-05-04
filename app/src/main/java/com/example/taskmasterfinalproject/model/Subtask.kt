package com.example.taskmasterfinalproject.model

// ספרייה להגדרת ישות במסד הנתונים
import androidx.room.Entity
// ספרייה להגדרת מפתח זר לקשר בין טבלאות – מבטיח שלמות נתונים (Referential Integrity)
import androidx.room.ForeignKey
// ספרייה להגדרת מפתח ראשי
import androidx.room.PrimaryKey
// ספרייה ליצירת מזהים ייחודיים (UUID) – מבטיח מזהה ייחודי ללא צורך בשרת
import java.util.UUID

// מודל המייצג תת-משימה, מקושר למשימה אב ביחס One-to-Many.
// ForeignKey מגדיר קשר בין taskId בטבלה זו ל-id בטבלת tasks.
// onDelete = CASCADE – כשמוחקים משימה אב, כל תתי-המשימות שלה נמחקים אוטומטית
@Entity(
    tableName = "subtasks",
    foreignKeys = [
        ForeignKey(
            entity = Task::class,               // הישות ההורה (טבלת המשימות)
            parentColumns = ["id"],              // העמודה בטבלת האב
            childColumns = ["taskId"],           // העמודה בטבלה הנוכחית שמצביעה על האב
            onDelete = ForeignKey.CASCADE        // מחיקת האב תמחק את הבנים אוטומטית
        )
    ],
    indices = [androidx.room.Index("taskId")]     // אינדקס על taskId לשיפור ביצועי JOIN
)
data class Subtask(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),  // מזהה ייחודי שנוצר אוטומטית באמצעות UUID
    val taskId: String,                                          // מזהה המשימה ההורה (Foreign Key)
    val title: String,                                           // כותרת תת-המשימה
    val isDone: Boolean = false,                                 // האם בוצעה (ברירת מחדל: לא)
    val createdAt: Long = System.currentTimeMillis()             // חותמת זמן יצירה
)
