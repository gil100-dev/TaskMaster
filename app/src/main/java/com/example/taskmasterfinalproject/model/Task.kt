package com.example.taskmasterfinalproject.model

// ספרייה להגדרת ישות במסד הנתונים – כל data class עם @Entity הופכת לטבלה ב-Room
import androidx.room.Entity
// ספרייה להגדרת מפתח ראשי – עמודה ייחודית המזהה כל רשומה בטבלה
import androidx.room.PrimaryKey

// מודל המייצג את המשימה הראשית בטבלת המשימות.
// data class מספקת אוטומטית: equals(), hashCode(), toString(), copy()
// @Entity אומרת ל-Room ליצור טבלה בשם "tasks" עם העמודות המוגדרות כאן
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey val id: String,                       // מזהה ייחודי של המשימה (מפתח ראשי)
    val title: String?,                                // כותרת המשימה
    val description: String?,                          // תיאור מפורט של המשימה (אופציונלי)
    val dueDate: String?,                              // תאריך יעד כמחרוזת (לתצוגה)
    val priority: Int?,                                // עדיפות: 1=נמוכה, 2=בינונית, 3=גבוהה
    val dueTimeMillis: Long?,                          // זמן יעד במילישניות (לתזמון תזכורות מדויקות)
    val isCompleted: Boolean = false,                  // האם המשימה הושלמה (ברירת מחדל: לא)
    val createdAt: Long = System.currentTimeMillis(),  // חותמת זמן יצירה (מילישניות מ-1970)
    val completedAt: Long? = null,                     // חותמת זמן סיום (null = עדיין לא הושלמה)
    val ownerUserId: Long = -1L                        // מזהה המשתמש הבעלים (-1 = משימה ללא שיוך / ליגאסי)
)
