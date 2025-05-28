package com.example.taskplanner

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

import java.util.Locale

class ArchiveActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TaskAdapter
    private var tasks: List<Task> = emptyList()
    private lateinit var token: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.archive_activity)
        token = getSharedPreferences("user_prefs", Context.MODE_PRIVATE).getString("auth_token", null).toString()

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Архив"

        recyclerView = findViewById(R.id.task_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = TaskAdapter(tasks, isArchive = true,
            onItemClick = { task ->
                val intent = Intent(this, TaskDetailsActivity::class.java)
                intent.putExtra("task_title", task.title)
                intent.putExtra("task_description", task.description)
                intent.putExtra("task_date", task.reminderDate)
                intent.putExtra("task_category", task.categoryId)
                startActivity(intent)
            },
            onItemLongClick = {},
            onArchiveClick = {}
        )
        recyclerView.adapter = adapter
        TaskLoader.loadTasks(this,
            showDeleted = false,
            showArchived = true,
            token = token) { tasks ->
            adapter.updateTasks(tasks)

        }
        findViewById<Button>(R.id.btn_clear_archive).setOnClickListener {
            lifecycleScope.launch {
                try {
                    Log.d("RRR", token)
                    val response = RetrofitClient.apiService.clearArchive(token)
                    if (response.isSuccessful) {
                        Toast.makeText(this@ArchiveActivity, "Архив очищен", Toast.LENGTH_SHORT).show()
                        TaskLoader.loadTasks(this@ArchiveActivity,
                            showDeleted = false,
                            showArchived = false,
                            token = token) { tasks ->
                            adapter.updateTasks(tasks)
                        } // загружаем задачи заново при возврате на экран
                    } else {
                        Toast.makeText(this@ArchiveActivity, "Ошибка очистки", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@ArchiveActivity, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
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

