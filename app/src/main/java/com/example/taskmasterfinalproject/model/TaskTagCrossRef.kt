package com.example.taskmasterfinalproject.model

import androidx.room.Entity

@Entity(
    primaryKeys = ["taskId", "tagId"],
    indices = [androidx.room.Index("tagId")]
)
data class TaskTagCrossRef(
    val taskId: String,
    val tagId: String
)
