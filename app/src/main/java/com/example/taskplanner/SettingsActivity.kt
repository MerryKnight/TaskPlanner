package com.example.taskplanner

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.MenuItem
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.button.MaterialButton

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val switchTheme = findViewById<Switch>(R.id.switch_theme)
        val btnLogout = findViewById<TextView>(R.id.tv_logout)

        val logoutTextView = findViewById<TextView>(R.id.tv_logout)
        logoutTextView.paintFlags = logoutTextView.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)  // Показываем стрелку назад
        supportActionBar?.title = "Настройки"
        // Пока без логики, просто заглушка
        switchTheme.setOnCheckedChangeListener { _, isChecked ->
            // TODO: Реализация темы
        }

        btnLogout.setOnClickListener {
            // TODO: Реализация выхода
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                startActivity(Intent(this, MainActivity::class.java))  // Действие при нажатии на стрелку
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}