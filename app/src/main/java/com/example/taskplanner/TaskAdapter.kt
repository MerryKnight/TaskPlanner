package com.example.taskplanner

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class TaskAdapter(
    private var tasks: List<Task>,
    private val isArchive: Boolean = false,
    private val isTrash: Boolean = false,
    private val onItemClick: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.task_title)
        private val subtextView: TextView = itemView.findViewById(R.id.task_subtext)
        fun bind(task: Task) {

            titleTextView.text = task.title
            val formatter = SimpleDateFormat("MMMM d, yyyy, HH:mm", Locale("ru"))
            subtextView.text = formatter.format(task.reminderDate)

            itemView.setOnClickListener {
                onItemClick(task)
            }

            if (isArchive) {
                titleTextView.paintFlags = titleTextView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                titleTextView.setTextColor(Color.GRAY)
            }
            if (isTrash) {
                titleTextView.paintFlags = titleTextView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                titleTextView.setTextColor(Color.RED)
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
