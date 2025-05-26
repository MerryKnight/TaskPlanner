
package com.example.taskplanner

import android.content.Context

class SearchHistoryManager(context: Context) {

    private val prefs = context.getSharedPreferences("search_history", Context.MODE_PRIVATE)
    private val maxSize = 10

    fun saveQuery(query: String) {
        if (query.isBlank()) return
        val current = getHistory().toMutableList()
        current.remove(query)
        current.add(0, query)
        if (current.size > maxSize) current.removeLast()

        // Сохраняем сами строки
        prefs.edit().putStringSet("history_set", current.toSet()).apply()

        // Сохраняем временные метки
        val timestampEditor = prefs.edit()
        timestampEditor.putLong(query, System.currentTimeMillis())
        timestampEditor.apply()
    }

    fun getHistory(): List<String> {
        val set = prefs.getStringSet("history_set", emptySet()) ?: emptySet()
        return set.toList().sortedByDescending { prefs.getLong(it, 0L) }
    }

    fun clearHistory() {
        val set = prefs.getStringSet("history_set", emptySet()) ?: emptySet()
        val editor = prefs.edit()
        editor.clear()
        set.forEach { editor.remove(it) } // очищаем и timestamp'ы
        editor.apply()
    }
}


