package com.example.taskplanner

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RetrofitClient.init(this)
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val savedEmail = prefs.getString("user_email", null)

        if (savedEmail != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }
        setContentView(R.layout.activity_login)
        emailInput = findViewById<EditText>(R.id.emailInput)
        passwordInput = findViewById<EditText>(R.id.passwordInput)
        loginButton = findViewById<Button>(R.id.loginButton)

        findViewById<TextView>(R.id.toRegister).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }


        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginUser(email, password)
        }
    }

    private fun loginUser(email: String, password: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.login(LoginRequest(email, password))
                if (response.isSuccessful && response.body() != null) {
                    val token = response.body()!!.token
                    Toast.makeText(this@LoginActivity, "Успешный вход! Токен: $token", Toast.LENGTH_SHORT).show()
                    val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                    prefs.edit().putString("user_email", email)
                        .putString("auth_token", token) // ⬅ Сохраняем токен
                        .apply()
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "Ошибка входа: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Сервер недоступен: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
