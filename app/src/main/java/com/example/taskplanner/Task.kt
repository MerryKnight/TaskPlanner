package com.example.taskplanner

import java.time.LocalDate

data class Task(
    val taskId: Int?,
    val title: String,
    val description: String,
    val reminderDate: String, // в ISO формате: "2025-05-27T15:30:00"
    val categoryId: Int?,
    val isDeleted: Boolean?,
    val isArchived: Boolean?

)

