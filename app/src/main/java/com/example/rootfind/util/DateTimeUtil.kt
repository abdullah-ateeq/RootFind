package com.example.rootfind.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateTimeUtil {

    fun formatHistoryTimestamp(timestamp: Long): String {
        val now = Calendar.getInstance()
        val itemCal = Calendar.getInstance().apply { timeInMillis = timestamp }

        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val timeStr = timeFormat.format(Date(timestamp))

        val isSameDay = now.get(Calendar.YEAR) == itemCal.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == itemCal.get(Calendar.DAY_OF_YEAR)

        if (isSameDay) {
            return "Today • $timeStr"
        }

        val yesterdayCal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }
        val isYesterday = yesterdayCal.get(Calendar.YEAR) == itemCal.get(Calendar.YEAR) &&
                yesterdayCal.get(Calendar.DAY_OF_YEAR) == itemCal.get(Calendar.DAY_OF_YEAR)

        if (isYesterday) {
            return "Yesterday • $timeStr"
        }

        val dateFormat = SimpleDateFormat("MMM d • h:mm a", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }
}
