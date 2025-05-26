package com.example.taskplanner

import retrofit2.http.GET
import retrofit2.http.Header

interface TodoistApi {
    @GET("tasks")
    suspend fun getTasks(
        @Header("Authorization") token: String
    ): List<TodoistTask>
}