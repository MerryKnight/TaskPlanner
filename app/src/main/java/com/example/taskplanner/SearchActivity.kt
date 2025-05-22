package com.example.taskplanner

import android.os.Bundle
import kotlinx.coroutines.launch
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
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

    companion object {
        private const val SEARCH_QUERY_KEY = "search_query"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        searchView = findViewById(R.id.search_view)
        clearButton = findViewById(R.id.btn_clear)
        recyclerView = findViewById(R.id.rv_search_results)
        adapter = TaskAdapter(emptyList()) { task ->
            // Обработка клика по задаче (если нужно)
        }
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
                val token = "Bearer 3be63e10b064388024c6aee5e4a70a78a18e6aef"
                val tasks = RetrofitClient.api.getTasks(token)
                val filtered = tasks.filter { it.content.contains(query, ignoreCase = true) }

                if (filtered.isEmpty()) {
                    showEmptyPlaceholder()
                } else {
                    showResults(filtered)
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

    private fun showResults(todoistTasks: List<TodoistTask>) {
        recyclerView.visibility = View.VISIBLE
        placeholderEmpty.visibility = View.GONE
        placeholderError.visibility = View.GONE
        historyRecyclerView.visibility = View.GONE
        findViewById<Button>(R.id.btn_clear_history).visibility = View.GONE

        val converted = todoistTasks.map {
            Task(
                title = it.content,
                description = "", // у Todoist задач нет описания, можно оставить пустым
                reminderDate = it.due?.date?.let { dateStr ->
                    // Преобразуем ISO 8601 в Date
                    try {
                        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                        parser.parse(dateStr)
                    } catch (e: Exception) {
                        null
                    }
                } ?: java.util.Date() // если даты нет — текущая
            )
        }

        adapter.updateTasks(converted)
    }
  /*  private fun showHistory() {
        val history = historyManager.getHistory()
        if (history.isNotEmpty()) {
            historyAdapter.update(history)
            historyRecyclerView.visibility = View.VISIBLE
            findViewById<Button>(R.id.btn_clear_history).visibility = View.VISIBLE

            // СКРЫВАЕМ остальное:
            recyclerView.visibility = View.GONE
            placeholderEmpty.visibility = View.GONE
            placeholderError.visibility = View.GONE
        }
    }*/


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



