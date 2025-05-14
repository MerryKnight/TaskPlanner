package com.example.taskplanner

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        findViewById<TextView>(R.id.toRegister).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        //Выпилить после добавления БД
        findViewById<TextView>(R.id.devButton).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        } //ВЫПИЛИТЬ ПОТОМ
    }
}