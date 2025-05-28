package com.example.taskplanner

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.example.taskplanner.RetrofitClient.apiService
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale


class MainActivity : AppCompatActivity() {


    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TaskAdapter
    private var tasks: List<Task> = emptyList()
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var token: String
    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtils.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        RetrofitClient.init(this)
        token = getSharedPreferences("user_prefs", Context.MODE_PRIVATE).getString("auth_token", null).toString()
        Log.d("RRR", token)
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

        recyclerView = findViewById(R.id.task_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)


        adapter = TaskAdapter(tasks,
            onItemClick = { task ->
                task.taskId?.let { id ->
                    val intent = Intent(this, TaskDetailsActivity::class.java).apply {
                        putExtra("task_id", id)
                        putExtra("task_title", task.title)
                        putExtra("task_description", task.description)
                        putExtra("task_date", task.reminderDate)
                        putExtra("task_category", task.categoryId)
                    }
                    startActivity(intent)
                } ?: run {
                    Toast.makeText(this, "Ошибка: taskId = null", Toast.LENGTH_SHORT).show()
                }
            },
            onItemLongClick = { task ->
                AlertDialog.Builder(this)
                    .setTitle("Удалить задачу?")
                    .setMessage("Вы уверены, что хотите удалить эту задачу?")
                    .setPositiveButton("Да") { _, _ ->
                        task.taskId?.let { id ->
                            Log.d("RRR", id.toString())
                            deleteTask(id)
                        } ?: run {
                            Toast.makeText(this, "Ошибка: ID задачи отсутствует", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Нет", null)
                    .show()
            },
            onArchiveClick = { task ->
                task.taskId?.let { id ->
                    archiveTask(id)
                } ?: Toast.makeText(this, "Ошибка: ID задачи отсутствует", Toast.LENGTH_SHORT).show()
            }



        )
        // Установка адаптера в RecyclerView
        recyclerView.adapter = adapter

         TaskLoader.loadTasks(this,
             showDeleted = false,
             showArchived = false,
             token = token) { tasks ->
             adapter.updateTasks(tasks)
         }

        val searchButton: Button = findViewById(R.id.btn_search)

        searchButton.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }
        findViewById<Button>(R.id.btn_add_task).setOnClickListener {
            startActivity(Intent(this, AddTaskActivity::class.java))
        }

    }
    override fun onResume() {
        super.onResume()
        TaskLoader.loadTasks(this,
            showDeleted = false,
            showArchived = false,
            token = token) { tasks ->
            adapter.updateTasks(tasks)
        } // загружаем задачи заново при возврате на экран
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
    private fun deleteTask(taskId: Int) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.deleteTask(taskId)
                if (response.isSuccessful) {
                    Toast.makeText(this@MainActivity, "Задача удалена", Toast.LENGTH_SHORT).show()
                    TaskLoader.loadTasks(this@MainActivity,
                        showDeleted = false,
                        showArchived = false,
                        token = token) { tasks ->
                        adapter.updateTasks(tasks)
                    }  // обновить список
                } else {
                    Toast.makeText(this@MainActivity, "Ошибка удаления", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun archiveTask(taskId: Int) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.archiveTask(taskId)
                if (response.isSuccessful) {
                    Toast.makeText(this@MainActivity, "Задача архивирована", Toast.LENGTH_SHORT).show()
                    // Загрузить обновленные задачи
                    TaskLoader.loadTasks(
                        this@MainActivity,
                        showDeleted = false,
                        showArchived = false,
                        token = token
                    ) { tasks ->
                        adapter.updateTasks(tasks)
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Ошибка архивации", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


}

