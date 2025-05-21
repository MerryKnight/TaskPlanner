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
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Поиск"
        val editTextId = searchView.context.resources.getIdentifier("android:id/search_src_text", null, null)
        val editText = searchView.findViewById<EditText>(editTextId)


        // Восстановление текста запроса
        val savedQuery = savedInstanceState?.getString(SEARCH_QUERY_KEY) ?: ""
        searchView.setQuery(savedQuery, false)


        // Обрабатываем нажатие на стрелку назад
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                loadTasks(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                toggleClearButton(newText ?: "")
                val closeButtonId = searchView.context.resources.getIdentifier("android:id/search_close_btn", null, null)
                val closeButton = searchView.findViewById<View>(closeButtonId)
                closeButton?.visibility = View.GONE
                return true
            }

        })

        btnRetry.setOnClickListener {
            loadTasks(lastQuery)
        }

        clearButton.setOnClickListener {
            searchView.setQuery("", false)
            hideKeyboard(editText)
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
            }
        }
    }
    private fun showEmptyPlaceholder() {
        recyclerView.visibility = View.GONE
        placeholderEmpty.visibility = View.VISIBLE
        placeholderError.visibility = View.GONE
    }

    private fun showErrorPlaceholder() {
        recyclerView.visibility = View.GONE
        placeholderEmpty.visibility = View.GONE
        placeholderError.visibility = View.VISIBLE
    }

    private fun showResults(todoistTasks: List<TodoistTask>) {
        recyclerView.visibility = View.VISIBLE
        placeholderEmpty.visibility = View.GONE
        placeholderError.visibility = View.GONE

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


}
