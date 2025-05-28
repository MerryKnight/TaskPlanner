package com.example.taskplanner

import android.content.Context

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private lateinit var api: ApiService
    fun init(context: Context) {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context))
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.88.12:8080/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(ApiService::class.java)
    }

    val apiService: ApiService
        get() = api
}

