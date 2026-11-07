package com.solu8i.cmms.ui.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class MonthCell(
    val key: String,
    val dayOfMonth: Int,
    val inCurrentMonth: Boolean,
    val isToday: Boolean
)

/**
 * Perhitungan grid kalender dengan minggu dimulai hari Senin.
 */
object CalendarGrid {

    private val keyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun formatKey(value: Calendar): String = keyFormat.format(value.time)

    fun build(month: Calendar, today: Calendar = Calendar.getInstance()): List<MonthCell> {
        val first = (month.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
        }

        val currentMonth = first.get(Calendar.MONTH)
        val currentYear = first.get(Calendar.YEAR)
        val todayKey = formatKey(today)

        val start = gridStart(month)

        return (0 until 42).map { index ->
            val day = (start.clone() as Calendar).apply {
                add(Calendar.DAY_OF_MONTH, index)
            }

            val key = formatKey(day)

            MonthCell(
                key = key,
                dayOfMonth = day.get(Calendar.DAY_OF_MONTH),
                inCurrentMonth = day.get(Calendar.MONTH) == currentMonth && day.get(Calendar.YEAR) == currentYear,
                isToday = key == todayKey
            )
        }
    }

    /**
     * Senin pada minggu yang memuat tanggal 1.
     */
    fun gridStart(month: Calendar): Calendar {
        val first = (month.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val offset = (first.get(Calendar.DAY_OF_WEEK) + 5) % 7

        return (first.clone() as Calendar).apply {
            add(Calendar.DAY_OF_MONTH, -offset)
        }
    }

    /**
     * Rentang 6 baris grid (42 hari) yang mencakup seluruh bulan.
     */
    fun visibleRange(month: Calendar): Pair<Calendar, Calendar> {
        val start = gridStart(month)

        val end = (start.clone() as Calendar).apply {
            add(Calendar.DAY_OF_MONTH, 41)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        return start to end
    }
}
