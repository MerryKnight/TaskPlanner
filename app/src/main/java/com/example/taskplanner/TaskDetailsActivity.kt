package com.example.taskplanner

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TaskDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_taskdetails)
        supportActionBar?.hide()
        val title = intent.getStringExtra("task_title")
        val description = intent.getStringExtra("task_description")

        findViewById<TextView>(R.id.tv_title).text = title
        findViewById<TextView>(R.id.tv_description).text = description

        findViewById<Button>(R.id.btn_edit_task).setOnClickListener {
            val editIntent = Intent(this, EditTaskActivity::class.java)
            // Передаём при необходимости задачу для редактирования
            startActivity(editIntent)
        }
    }
}
