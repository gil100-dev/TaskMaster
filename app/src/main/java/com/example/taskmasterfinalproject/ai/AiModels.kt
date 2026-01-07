package com.example.taskmasterfinalproject.ai

data class AiSubtaskPlan(
    val subtasks: List<AiSubtask>,
    val totalMinutesRange: String,
    val bestTimeOfDay: String,
    val reason: String
)

data class AiSubtask(
    val title: String,
    val minutes: Int
)
