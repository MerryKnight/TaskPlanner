package com.example.taskplanner

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class EditTaskActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edittask)
        supportActionBar?.hide()
    }
}
