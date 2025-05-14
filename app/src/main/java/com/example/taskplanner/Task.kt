package com.example.taskplanner

data class Task(
    val title: String,
    val description: String,
    val category: String,
    val reminderDate: java.util.Date
)

