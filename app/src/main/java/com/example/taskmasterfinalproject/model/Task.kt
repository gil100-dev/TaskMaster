package com.example.taskmasterfinalproject.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey val id: String,
    val title: String?,
    val description: String?,
    val dueDate: String?,
    val priority: Int?,
    val dueTimeMillis: Long?,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)
