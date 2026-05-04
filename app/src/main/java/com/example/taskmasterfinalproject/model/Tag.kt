package com.example.taskmasterfinalproject.model

// ספרייה להגדרת ישות במסד הנתונים
import androidx.room.Entity
// ספרייה להגדרת מפתח ראשי
import androidx.room.PrimaryKey
// ספרייה ליצירת מזהים ייחודיים (UUID)
import java.util.UUID

// מודל המייצג תגית (קטגוריה) למשימות – מאפשר לסווג משימות לקבוצות
@Entity(tableName = "tags")
data class Tag(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),  // מזהה ייחודי של התגית
    val name: String,                                            // שם התגית (לדוגמה: "עבודה", "לימודים")
    val colorHex: String,                                        // צבע התגית בפורמט HEX (לדוגמה: "#FF5722")
    val ownerUserId: Long = -1L                                  // מזהה המשתמש הבעלים (-1 = ליגאסי)
)
