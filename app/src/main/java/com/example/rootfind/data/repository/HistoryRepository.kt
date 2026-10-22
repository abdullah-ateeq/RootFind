package com.example.rootfind.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.rootfind.model.RootFindHistory
import com.example.rootfind.model.RootType

class HistoryRepository(
    private val prefs: SharedPreferences
) {
    constructor(context: Context) : this(
        context.getSharedPreferences("rootfind_history", Context.MODE_PRIVATE)
    )

    fun getHistory(): List<RootFindHistory> {
        val serialized = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        if (serialized.isBlank()) return emptyList()

        val list = mutableListOf<RootFindHistory>()
        val entries = serialized.split("\n")
        for (line in entries) {
            val parts = line.split("|")
            if (parts.size == 5) {
                val id = parts[0]
                val rootType = try {
                    RootType.valueOf(parts[1])
                } catch (e: Exception) {
                    RootType.SQUARE
                }
                val value = parts[2]
                val rootIndex = parts[3]
                val savedAt = parts[4].toLongOrNull() ?: System.currentTimeMillis()
                list.add(RootFindHistory(id, rootType, value, rootIndex, savedAt))
            }
        }
        return list
    }

    /**
     * Adds a history entry if not a consecutive duplicate.
     * Enforces the 10-entry limit by removing the oldest entry when the 11th is added.
     */
    fun addHistory(entry: RootFindHistory): List<RootFindHistory> {
        val current = getHistory().toMutableList()

        // Consecutive duplicate check
        val latest = current.firstOrNull()
        if (latest != null &&
            latest.rootType == entry.rootType &&
            latest.value == entry.value &&
            latest.rootIndex == entry.rootIndex
        ) {
            return current
        }

        // Add new at the beginning (most recent first)
        current.add(0, entry)

        // Limit to 10 entries
        val trimmed = if (current.size > MAX_HISTORY_ITEMS) {
            current.take(MAX_HISTORY_ITEMS)
        } else {
            current
        }

        saveList(trimmed)
        return trimmed
    }

    fun deleteHistory(id: String): List<RootFindHistory> {
        val current = getHistory().filterNot { it.id == id }
        saveList(current)
        return current
    }

    fun clearHistory() {
        prefs.edit().remove(KEY_HISTORY).apply()
    }

    private fun saveList(list: List<RootFindHistory>) {
        val serialized = list.joinToString("\n") { item ->
            "${item.id}|${item.rootType.name}|${item.value}|${item.rootIndex}|${item.savedAt}"
        }
        prefs.edit().putString(KEY_HISTORY, serialized).apply()
    }

    companion object {
        const val MAX_HISTORY_ITEMS = 10
        private const val KEY_HISTORY = "saved_history_v2"
    }
}
