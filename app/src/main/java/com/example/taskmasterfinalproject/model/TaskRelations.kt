package com.example.taskmasterfinalproject.model

// ספרייה לסימון אובייקט כמוכל (Embedded) בתוך אובייקט אחר – משטיחה את עמודות הישות לתוך השאילתה
import androidx.room.Embedded
// ספרייה להגדרת טבלת קישור ביחסי Many-to-Many (Junction Table)
import androidx.room.Junction
// ספרייה להגדרת קשר (Relation) בין ישויות – Room טוענת את הנתונים המקושרים אוטומטית
import androidx.room.Relation

// מחלקת עזר לצירוף משימה עם תת-המשימות שלה (יחס One-to-Many).
// @Embedded משטיח את עמודות Task ישירות לתוך השאילתה.
// @Relation מגדיר ש-Room יטען אוטומטית את רשימת ה-Subtask לפי parentColumn = id
data class TaskWithSubtasks(
    @Embedded val task: Task,          // המשימה עצמה (כל העמודות נכנסות ישירות)
    @Relation(
        parentColumn = "id",            // העמודה בטבלת ה-Task (אב)
        entityColumn = "taskId"         // העמודה בטבלת ה-Subtask (בן) – המפתח הזר
    )
    val subtasks: List<Subtask>         // רשימת תת-המשימות (Room ממלא אוטומטית)
)

// מחלקת עזר לצירוף משימה עם התגיות שלה (יחס Many-to-Many באמצעות Junction).
// Junction אומרת ל-Room להשתמש בטבלת הקישור TaskTagCrossRef כדי למצוא את הקשרים
data class TaskWithTags(
    @Embedded val task: Task,          // המשימה עצמה
    @Relation(
        parentColumn = "id",            // עמודת ה-id בטבלת tasks
        entityColumn = "id",            // עמודת ה-id בטבלת tags
        associateBy = Junction(         // הקישור מתבצע דרך טבלת ביניים (Junction Table)
            value = TaskTagCrossRef::class,    // טבלת הקישור
            parentColumn = "taskId",            // העמודה שמצביעה על המשימה
            entityColumn = "tagId"              // העמודה שמצביעה על התגית
        )
    )
    val tags: List<Tag>                 // רשימת התגיות המקושרות (Room ממלא אוטומטית)
)

// מחלקת עזר ראשית לצירוף משימה עם כל פרטיה (תת-משימות ותגיות).
// משמשת ב-DAO עם @Transaction כדי להבטיח עקביות בטעינת כל הנתונים המקושרים יחד
data class TaskWithDetails(
    @Embedded val task: Task,          // המשימה עצמה
    @Relation(
        parentColumn = "id",
        entityColumn = "taskId"
    )
    val subtasks: List<Subtask>,       // כל תת-המשימות של המשימה
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = TaskTagCrossRef::class,
            parentColumn = "taskId",
            entityColumn = "tagId"
        )
    )
    val tags: List<Tag>                // כל התגיות המקושרות למשימה
)
