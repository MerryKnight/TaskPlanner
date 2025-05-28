package com.example.taskplanner

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class TaskDetailsActivity : AppCompatActivity() {
    private var categories: List<TaskCategoriesDTO> = listOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_taskdetails)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val title = intent.getStringExtra("task_title")
        supportActionBar?.title = "Задача: " + title
        val description = intent.getStringExtra("task_description")

        val id = intent.getIntExtra("task_id",-1)

        val category = intent.getStringExtra("task_description")
        findViewById<TextView>(R.id.tv_title).text = title
        findViewById<TextView>(R.id.tv_description).text = description
        val dateString = intent.getStringExtra("task_date")

        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        isoFormat.timeZone = TimeZone.getTimeZone("UTC") // чтобы корректно интерпретировать дату, если сервер отдает в UTC

        val userFriendlyFormat = SimpleDateFormat("d MMMM yyyy, HH:mm", Locale("ru"))
        userFriendlyFormat.timeZone = TimeZone.getDefault() // показываем пользователю в его локальной зоне

        val parsedDate: Date? = try {
            dateString?.let { isoFormat.parse(it) }
        } catch (e: Exception) {
            null
        }

        findViewById<TextView>(R.id.tv_date).text =
            parsedDate?.let { userFriendlyFormat.format(it) } ?: "Дата недоступна"

        val categoryId = intent.getIntExtra("task_category", -1)
        findViewById<Button>(R.id.btn_edit_task).setOnClickListener {
            val editIntent = Intent(this, EditTaskActivity::class.java)
            editIntent.putExtra("task_id", id)
            editIntent.putExtra("task_title", title)
            editIntent.putExtra("task_description", description)
            editIntent.putExtra("task_date", dateString)
            editIntent.putExtra("task_category", categoryId)

            // Передаём при необходимости задачу для редактирования
            startActivity(editIntent)
        }
        loadCategories()
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                startActivity(Intent(this, MainActivity::class.java))
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)// Действие при нажатии на стрелку
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun loadCategories() {
        lifecycleScope.launch {
            try {
                categories = RetrofitClient.apiService.getCategories()
                val categoryId = intent.getIntExtra("task_category", -1)
                val categoryName = categories.find { it.id == categoryId }?.name ?: "Без категории"
                findViewById<TextView>(R.id.tv_category).text = categoryName
            } catch (e: Exception) {
                Toast.makeText(this@TaskDetailsActivity, "Ошибка загрузки категорий: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
