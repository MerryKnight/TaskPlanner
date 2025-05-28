package com.example.taskplanner

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val registerButton = findViewById<Button>(R.id.registerButton)

        registerButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val registerRequest = RegisterRequest(email = email, password = password)

            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.apiService.register(registerRequest)
                    // Если registration возвращает объект (а не Response), то просто:
                    Toast.makeText(this@RegisterActivity, "Регистрация успешна! Токен: ${response.token}", Toast.LENGTH_SHORT).show()
                    finish() // Закрываем регистрацию и возвращаемся назад
                } catch (e: Exception) {
                    Toast.makeText(this@RegisterActivity, "Ошибка регистрации: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }
}
