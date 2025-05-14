package com.example.taskplanner

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import java.text.SimpleDateFormat

import java.util.Locale

class ArchiveActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TaskAdapter
    private var tasks: List<Task> = emptyList()
    private lateinit var drawerLayout: DrawerLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.archive_activity)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Архив"
        drawerLayout = findViewById(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val navView = findViewById<NavigationView>(R.id.nav_view)
        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_archive -> {
                    startActivity(Intent(this, ArchiveActivity::class.java))
                    true
                }
                R.id.nav_trash -> {
                    startActivity(Intent(this, TrashActivity::class.java))
                    true
                }
                else -> false
            }
        }
        recyclerView = findViewById(R.id.task_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Создание адаптера и подключение к RecyclerView

        // Создание адаптера с обработчиком клика
        adapter = TaskAdapter(tasks, isArchive = true) { task ->
            // Переход на экран просмотра задачи при клике
            val intent = Intent(this, TaskDetailsActivity::class.java)
            intent.putExtra("task_title", task.title)
            intent.putExtra("task_description", task.description)
            intent.putExtra("task_subtext", task.reminderDate)
            startActivity(intent)

        }

        // Установка адаптера в RecyclerView
        recyclerView.adapter = adapter

        loadTasks()
    }

    private fun loadTasks() {
        val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("ru"))
        tasks = listOf(
            Task("Задача 1", "Описание задачи 1", "Работа",formatter.parse("14.05.2025 13:07")!!),
            Task("Задача 2", "Описание задачи 2", "Личное",formatter.parse("14.05.2025 13:07")!!),
            Task("Задача 3", "Описание задачи 3", "Работа",formatter.parse("14.05.2025 13:07")!!),
            Task("Задача 4", "Описание задачи 4", "Учеба",formatter.parse("14.05.2025 13:07")!!)
        )
        Log.d("TASKS", "Загружено задач: ${tasks.size}")
        adapter.updateTasks(tasks) // Обновляем адаптер с новыми данными
    }
}
