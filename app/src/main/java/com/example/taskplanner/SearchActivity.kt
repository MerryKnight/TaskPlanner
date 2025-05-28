package com.example.taskplanner

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import kotlinx.coroutines.launch

import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton

import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar

import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale

class SearchActivity : AppCompatActivity() {

    private lateinit var searchView: SearchView
    private lateinit var clearButton: ImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var placeholderEmpty: TextView
    private lateinit var placeholderError: View
    private lateinit var btnRetry: Button
    private var lastQuery: String = ""
    private var tasks: List<Task> = emptyList()
    private lateinit var adapter: TaskAdapter
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var historyAdapter: SearchHistoryAdapter
    private lateinit var historyManager: SearchHistoryManager
    private var searchJob: kotlinx.coroutines.Job? = null
    private lateinit var progressBar: ProgressBar
    private lateinit var token: String

    companion object {
        private const val SEARCH_QUERY_KEY = "search_query"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        searchView = findViewById(R.id.search_view)
        clearButton = findViewById(R.id.btn_clear)
        recyclerView = findViewById(R.id.rv_search_results)
        adapter = TaskAdapter(
            tasks = emptyList(),
            isArchive = false,
            isTrash = false,
            onItemClick = { task ->
                val intent = Intent(this, TaskDetailsActivity::class.java)
                intent.putExtra("task_title", task.title)
                intent.putExtra("task_description", task.description)
                intent.putExtra("task_date", task.reminderDate)
                intent.putExtra("task_category", task.categoryId)
                startActivity(intent)
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
        token = getSharedPreferences("user_prefs", Context.MODE_PRIVATE).getString("auth_token", null).toString()

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        placeholderEmpty = findViewById(R.id.placeholder_empty)
        placeholderError = findViewById(R.id.placeholder_error)
        btnRetry = findViewById(R.id.btn_retry)
        progressBar = findViewById(R.id.progress_bar)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Поиск"
        val editTextId = searchView.context.resources.getIdentifier("android:id/search_src_text", null, null)
        val editText = searchView.findViewById<EditText>(editTextId)
        historyManager = SearchHistoryManager(this)


        // Восстановление текста запроса
        val savedQuery = savedInstanceState?.getString(SEARCH_QUERY_KEY) ?: ""
        searchView.setQuery(savedQuery, false)

        historyRecyclerView = findViewById(R.id.rv_search_history)
        historyAdapter = SearchHistoryAdapter(emptyList()) { query ->
            searchView.setQuery(query, true) // выполняет поиск
        }
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyRecyclerView.adapter = historyAdapter

        // Обрабатываем нажатие на стрелку назад
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        editText?.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && searchView.query.isEmpty()) {
                showHistory()
            } else {
                hideHistory()
            }
        }

        if (searchView.query.isEmpty()) {
            showHistory()
        }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val safeQuery = query.orEmpty()
                if (safeQuery.isNotBlank()) {
                    historyManager.saveQuery(safeQuery)
                    hideHistory()
                    loadTasks(safeQuery)
                }

                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                toggleClearButton(newText ?: "")
                val query = newText.orEmpty()
                val closeButtonId = searchView.context.resources.getIdentifier("android:id/search_close_btn", null, null)
                val closeButton = searchView.findViewById<View>(closeButtonId)
                closeButton?.visibility = View.GONE
                searchJob?.cancel() // отменить предыдущую задачу
                searchJob = lifecycleScope.launch {
                    delay(2000) // 2 секунды
                    if (query.isNotBlank()) {
                        historyManager.saveQuery(query)
                        hideHistory()
                        loadTasks(query)
                    }
                }
                return true
            }


        })
        if (searchView.query.isEmpty()) {
            updateHistory()
        }
        val btnClearHistory = findViewById<Button>(R.id.btn_clear_history)
        btnClearHistory.setOnClickListener {
            historyManager.clearHistory()
            updateHistory()
        }
        btnRetry.setOnClickListener {
            loadTasks(lastQuery)
        }

        clearButton.setOnClickListener {
            searchView.setQuery("", false)
            hideKeyboard(editText)
            showHistory()
        }
        searchView.isIconified = false
        editText?.requestFocus()
        showKeyboard(editText)

    }

    private fun deleteTask(taskId: Int) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.deleteTask(taskId)
                if (response.isSuccessful) {
                    Toast.makeText(this@SearchActivity, "Задача удалена", Toast.LENGTH_SHORT).show()
                    TaskLoader.loadTasks(this@SearchActivity,
                        showDeleted = false,
                        showArchived = false,
                        token = token) { tasks ->
                        adapter.updateTasks(tasks)
                    }  // обновить список
                } else {
                    Toast.makeText(this@SearchActivity, "Ошибка удаления", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@SearchActivity, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun archiveTask(taskId: Int) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.archiveTask(taskId)
                if (response.isSuccessful) {
                    Toast.makeText(this@SearchActivity, "Задача архивирована", Toast.LENGTH_SHORT).show()
                    // Загрузить обновленные задачи
                    TaskLoader.loadTasks(
                        this@SearchActivity,
                        showDeleted = false,
                        showArchived = false,
                        token = token
                    ) { tasks ->
                        adapter.updateTasks(tasks)
                    }
                } else {
                    Toast.makeText(this@SearchActivity, "Ошибка архивации", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@SearchActivity, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(SEARCH_QUERY_KEY, searchView.query.toString())
        super.onSaveInstanceState(outState)
    }

    private fun toggleClearButton(text: String) {
        clearButton.visibility = if (text.isNotEmpty()) View.VISIBLE else View.GONE
    }

    private fun showKeyboard(view: View?) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard(view: View?) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }
    private fun loadTasks(query: String) {
        lastQuery = query
        progressBar.visibility = View.VISIBLE
        findViewById<Button>(R.id.btn_clear_history).visibility = View.GONE
        lifecycleScope.launch {
            try {

                val result = RetrofitClient.apiService.searchTasks(token, query)

                if (result.body() == null) {
                    showEmptyPlaceholder()
                } else {
                    result.body()?.let { showResults(it) }
                }
            } catch (e: Exception) {
                showErrorPlaceholder()
            } finally {
            progressBar.visibility = View.GONE
                hideHistory()
        }
        }

    }
    private fun showEmptyPlaceholder() {
        recyclerView.visibility = View.GONE
        placeholderEmpty.visibility = View.VISIBLE
        placeholderError.visibility = View.GONE
        historyRecyclerView.visibility = View.GONE
        findViewById<Button>(R.id.btn_clear_history).visibility = View.GONE
    }

    private fun showErrorPlaceholder() {
        recyclerView.visibility = View.GONE
        placeholderEmpty.visibility = View.GONE
        placeholderError.visibility = View.VISIBLE
        historyRecyclerView.visibility = View.GONE
        findViewById<Button>(R.id.btn_clear_history).visibility = View.GONE
    }

    private fun showResults(task: List<Task>) {
        recyclerView.visibility = View.VISIBLE
        placeholderEmpty.visibility = View.GONE
        placeholderError.visibility = View.GONE
        historyRecyclerView.visibility = View.GONE
        findViewById<Button>(R.id.btn_clear_history).visibility = View.GONE

        val converted = task.map {
            Task(
                taskId = it.taskId,
                isDeleted = it.isDeleted,
                isArchived = it.isArchived,
                categoryId = it.categoryId,
                title = it.title,
                description = it.description,
                reminderDate = it.reminderDate

            )
        }

        adapter.updateTasks(converted)

    }

    private fun hideHistory() {
        historyRecyclerView.visibility = View.GONE
        findViewById<Button>(R.id.btn_clear_history).visibility = View.GONE
    }
    private fun showHistory() {
        val history = historyManager.getHistory()
        if (history.isNotEmpty()) {
            historyAdapter.update(history)
            historyRecyclerView.visibility = View.VISIBLE
            findViewById<Button>(R.id.btn_clear_history).visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            placeholderEmpty.visibility = View.GONE
            placeholderError.visibility = View.GONE
        }
    }

    private fun updateHistory() {
        val history = historyManager.getHistory()
        if (history.isNotEmpty()) {
            historyAdapter.update(history)
            historyRecyclerView.visibility = View.VISIBLE
            findViewById<Button>(R.id.btn_clear_history).visibility = View.VISIBLE
        } else {
            historyRecyclerView.visibility = View.GONE
            findViewById<Button>(R.id.btn_clear_history).visibility = View.GONE
        }
    }

}



