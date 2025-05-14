package com.example.taskplanner

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class SearchActivity : AppCompatActivity() {

    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TaskAdapter
    private var allTasks: List<Task> = listOf() // позже — полученные задачи

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        supportActionBar?.hide()

        searchView = findViewById(R.id.search_view)
        recyclerView = findViewById(R.id.rv_search_results)
        recyclerView.layoutManager = LinearLayoutManager(this)
      //  allTasks = getAllTasks()


// Инициализируем адаптер с задачами
        adapter = TaskAdapter(allTasks) { task ->
            // TODO: переход к деталям задачи
        }
        recyclerView.adapter = adapter
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterTasks(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterTasks(newText)
                return true
            }
        })
    }

    private fun filterTasks(query: String?) {
        val filtered = allTasks.filter {
            it.title.contains(query.orEmpty(), ignoreCase = true)
        }
        adapter.updateTasks(filtered)
    }

    // Заглушка
/*    private fun loadTasks() {
        val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("ru"))
        tasks = listOf(
            Task("Задача 1", "Описание задачи 1", "Работа",formatter.parse("14.05.2025 13:07")!!),
            Task("Задача 2", "Описание задачи 2", "Личное",formatter.parse("14.05.2025 13:07")!!),
            Task("Задача 3", "Описание задачи 3", "Работа",formatter.parse("14.05.2025 13:07")!!),
            Task("Задача 4", "Описание задачи 4", "Учеба",formatter.parse("14.05.2025 13:07")!!)
        )
        Log.d("TASKS", "Загружено задач: ${tasks.size}")
        adapter.updateTasks(tasks) // Обновляем адаптер с новыми данными
    }*/
}
