package com.example.taskplanner

import android.content.Context
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

        val prefs = getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("is_dark_mode", false)
        switchTheme.isChecked = isDarkMode

        val logoutTextView = findViewById<TextView>(R.id.tv_logout)
        logoutTextView.paintFlags = logoutTextView.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        val accountTextView = findViewById<TextView>(R.id.tv_account)
        val userPrefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userEmail = userPrefs.getString("user_email", "Неизвестно")
        accountTextView.text = "Аккаунт: $userEmail"

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)  // Показываем стрелку назад
        supportActionBar?.title = "Настройки"
        // Пока без логики, просто заглушка
        switchTheme.setOnCheckedChangeListener { _, isChecked ->
            ThemeUtils.toggleTheme(this, isChecked)
        }

        btnLogout.setOnClickListener {
            val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            prefs.edit().clear().apply()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                startActivity(Intent(this, MainActivity::class.java))  // Действие при нажатии на стрелку
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}