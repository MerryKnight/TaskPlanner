package com.example.taskplanner

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class SearchActivity : AppCompatActivity() {

    private lateinit var searchView: SearchView
    private lateinit var clearButton: ImageButton
    private lateinit var backButton: ImageButton

    companion object {
        private const val SEARCH_QUERY_KEY = "search_query"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        searchView = findViewById(R.id.search_view)
        clearButton = findViewById(R.id.btn_clear)
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
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                toggleClearButton(newText ?: "")
                val closeButtonId = searchView.context.resources.getIdentifier("android:id/search_close_btn", null, null)
                val closeButton = searchView.findViewById<View>(closeButtonId)
                closeButton?.visibility = View.GONE
                return true
            }
        })


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
}
