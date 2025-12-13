package com.example.taskmasterfinalproject.model

import androidx.room.Entity

@Entity(primaryKeys = ["taskId", "tagId"])
data class TaskTagCrossRef(
    val taskId: String,
    val tagId: String
)
