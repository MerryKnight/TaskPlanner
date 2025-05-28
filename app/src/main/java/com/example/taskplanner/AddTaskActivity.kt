package com.example.taskplanner


import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddTaskActivity : AppCompatActivity() {
    private lateinit var spinnerCategory: Spinner
    private var categories: List<TaskCategoriesDTO> = listOf()
    private var selectedCategoryId: Int? = null
    private var selectedDate: Calendar = Calendar.getInstance()
    private lateinit var dateEditText: EditText
    private lateinit var formattedDateTime: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_addtask)

        supportActionBar?.title = "Новая задача"
        spinnerCategory = findViewById(R.id.et_category)

        loadCategories()

        // Инициализация других полей
        val titleEditText: EditText = findViewById(R.id.et_title)
        val descriptionEditText: EditText = findViewById(R.id.et_description)
        dateEditText = findViewById(R.id.et_date) // Предположим, вы используете EditText

        dateEditText.setOnClickListener {
            showDateTimePicker()
        }
        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {

                selectedCategoryId = categories[position].id
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Пример нажатия на кнопку "Сохранить"
        val saveButton: Button = findViewById(R.id.btn_save)
        saveButton.setOnClickListener {
            val title = titleEditText.text.toString().trim()
            val description = descriptionEditText.text.toString().trim()
            val dateIso = formattedDateTime.trim() // преобразуй, если нужно

            val categoryId = selectedCategoryId
            val calendar = Calendar.getInstance()
            calendar.time = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(dateIso)!!

            val intent = Intent(this, NotificationReceiver::class.java).apply {
                putExtra("task_title", title)
                putExtra("task_id", taskId)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                this, taskId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)

            if (title.isEmpty() || description.isEmpty() || categoryId == null) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val task = Task(taskId = 0,
                title = title,
                description = description,
                reminderDate = dateIso,
                categoryId = selectedCategoryId,
                isArchived = false,
                isDeleted = false)

            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.apiService.createTask(task)
                    if (response.isSuccessful) {
                        Toast.makeText(this@AddTaskActivity, "Задача создана!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@AddTaskActivity, "Ошибка: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@AddTaskActivity, "Ошибка сети: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            try {
                categories = RetrofitClient.apiService.getCategories()
                val adapter = ArrayAdapter(
                    this@AddTaskActivity,
                    android.R.layout.simple_spinner_item,
                    categories.map { it.name }
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerCategory.adapter = adapter
            } catch (e: Exception) {
                Toast.makeText(this@AddTaskActivity, "Ошибка загрузки категорий: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDateTimePicker() {
        val context = this
        // Сначала показываем выбор даты
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, month)
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                // Потом показываем выбор времени
                TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        selectedDate.set(Calendar.MINUTE, minute)

                        // Форматируем дату как yyyy-MM-dd (только дата)
                        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                        formattedDateTime = isoFormat.format(selectedDate.time)
                        val userFriendlyFormat = SimpleDateFormat("d MMMM yyyy, HH:mm", Locale("ru"))
                        dateEditText.setText(userFriendlyFormat.format(selectedDate.time))

                    },
                    selectedDate.get(Calendar.HOUR_OF_DAY),
                    selectedDate.get(Calendar.MINUTE),
                    true
                ).show()

            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}
