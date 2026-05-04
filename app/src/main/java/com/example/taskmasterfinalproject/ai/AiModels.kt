package com.example.taskmasterfinalproject.ai

// מודל נתונים המייצג תוכנית משימות שנוצרה על ידי ה-AI
data class AiSubtaskPlan(
    val subtasks: List<AiSubtask>,
    val totalMinutesRange: String,
    val bestTimeOfDay: String,
    val reason: String
)

// מודל נתונים המייצג תת-משימה בודדת
data class AiSubtask(
    val title: String,
    val minutes: Int
)
