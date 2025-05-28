package com.example.taskplanner

import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class TaskAdapter(
    private var tasks: List<Task>,
    private val isArchive: Boolean = false,
    private val isTrash: Boolean = false,
    private val onItemClick: (Task) -> Unit,
    private val onItemLongClick: (Task) -> Unit,
    private val onArchiveClick: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val archiveCheckbox: CheckBox = itemView.findViewById(R.id.checkbox_archive)
        private val titleTextView: TextView = itemView.findViewById(R.id.task_title)
        private val subtextView: TextView = itemView.findViewById(R.id.task_subtext)
        fun bind(task: Task) {

            titleTextView.text = task.title
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            Log.d("Task", "Reminder date: ${task.reminderDate}")
            parser.timeZone = TimeZone.getTimeZone("UTC")
            val date: Date? = try {
                parser.parse(task.reminderDate)
            } catch (e: ParseException) {
                null
            }
            if (date != null) {
                val formatter = SimpleDateFormat(" d MMMM, yyyy, HH:mm", Locale("ru"))
                subtextView.text = formatter.format(date)
            } else {
                subtextView.text = "Дата недоступна"
            }
            itemView.setOnClickListener {
                onItemClick(task)
            }
            itemView.setOnLongClickListener {
                onItemLongClick(task)
                true  // чтобы событие считалось обработанным
            }

            if (isTrash) {
                titleTextView.paintFlags = titleTextView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                titleTextView.setTextColor(Color.RED)
                subtextView.setTextColor(Color.RED)
                archiveCheckbox.visibility = View.GONE
            }
            if (isArchive) {
                titleTextView.paintFlags = titleTextView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                titleTextView.setTextColor(Color.GRAY)
                archiveCheckbox.visibility = View.GONE
            }
            archiveCheckbox.setOnCheckedChangeListener(null) // Чтобы избежать повторного вызова
            archiveCheckbox.isChecked = task.isArchived == true
            archiveCheckbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked && task.isArchived != true) {
                    onArchiveClick(task)
                }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(tasks[position])
    }

    override fun getItemCount(): Int = tasks.size

    fun updateTasks(newTasks: List<Task>) {
        tasks = newTasks
        notifyDataSetChanged()
    }
}
