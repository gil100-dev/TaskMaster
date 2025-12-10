package com.example.taskmasterfinalproject.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey val id: String,
    val title: String? = null,
    val description: String? = null,
    val dueDate: String? = null,
    val priority: Int? = null,
    val dueTimeMillis: Long? = null,
    val isCompleted: Boolean = false
)


