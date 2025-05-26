package com.example.taskplanner

data class TodoistTask(
    val id: String,
    val content: String,
    val due: Due?
)
data class Due(
    val date: String
)