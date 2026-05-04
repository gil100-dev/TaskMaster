package com.example.taskmasterfinalproject.model

// ספרייה להגדרת ישות במסד הנתונים
import androidx.room.Entity

// ישות המייצגת את טבלת הקישור בין משימות לתגיות (Many-to-Many).
// כל משימה יכולה להיות משויכת למספר תגיות, וכל תגית יכולה להיות משויכת למספר משימות.
// המפתח הראשי מורכב משני שדות ביחד (Composite Primary Key) – מבטיח שאין כפילויות בקישור
@Entity(
    primaryKeys = ["taskId", "tagId"],               // מפתח ראשי מורכב – צירוף ייחודי של משימה ותגית
    indices = [androidx.room.Index("tagId")]          // אינדקס על tagId לשיפור ביצועי חיפוש
)
data class TaskTagCrossRef(
    val taskId: String,   // מזהה המשימה
    val tagId: String     // מזהה התגית
)
