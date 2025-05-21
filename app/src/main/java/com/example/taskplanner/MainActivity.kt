package com.example.taskplanner

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.Button
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import java.text.SimpleDateFormat
import java.util.Locale


class MainActivity : AppCompatActivity() {


    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TaskAdapter
    private var tasks: List<Task> = emptyList() // Список задач (пока статический)
    private lateinit var drawerLayout: DrawerLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtils.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Мои задачи"

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
                R.id.action_profile -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
       // supportActionBar?.hide()
        recyclerView = findViewById(R.id.task_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Создание адаптера и подключение к RecyclerView

        // Создание адаптера с обработчиком клика
        adapter = TaskAdapter(tasks) { task ->
            // Переход на экран просмотра задачи при клике
            val intent = Intent(this, TaskDetailsActivity::class.java)
            intent.putExtra("task_title", task.title)
            intent.putExtra("task_description", task.description)
            startActivity(intent)

        }

        // Установка адаптера в RecyclerView
        recyclerView.adapter = adapter

        loadTasks()
        val searchButton: Button = findViewById(R.id.btn_search)

        searchButton.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }
        findViewById<Button>(R.id.btn_add_task).setOnClickListener {
            startActivity(Intent(this, AddTaskActivity::class.java))
        }

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
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_profile -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}

