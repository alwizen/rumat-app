package com.solu8i.cmms

import com.solu8i.cmms.ui.util.CalendarGrid
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class CalendarGridTest {

    private fun calendarOf(year: Int, month: Int, day: Int = 1): Calendar =
        Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

    @Test
    fun gridStartsOnMondayBeforeFirstDay() {
        // 1 November 2026 jatuh pada hari Minggu
        val start = CalendarGrid.gridStart(calendarOf(2026, 11))

        val dayOfWeek = start.get(Calendar.DAY_OF_WEEK)
        assertEquals(Calendar.MONDAY, dayOfWeek)
        assertEquals("2026-10-26", CalendarGrid.formatKey(start))
    }

    @Test
    fun gridStartsSameDayWhenMonthStartsOnMonday() {
        // 1 Juni 2026 jatuh pada hari Senin
        val start = CalendarGrid.gridStart(calendarOf(2026, 6))

        assertEquals("2026-06-01", CalendarGrid.formatKey(start))
    }

    @Test
    fun gridHasSixWeeks() {
        val cells = CalendarGrid.build(calendarOf(2026, 11), today = calendarOf(2026, 11, 5))

        assertEquals(42, cells.size)
        assertEquals("2026-10-26", cells.first().key)
        assertEquals("2026-12-06", cells.last().key)
    }

    @Test
    fun onlyCurrentMonthDaysAreFlagged() {
        val cells = CalendarGrid.build(calendarOf(2026, 11), today = calendarOf(2026, 11, 5))

        val currentMonth = cells.filter { it.inCurrentMonth }
        assertEquals(30, currentMonth.size)
        assertEquals("2026-11-01", currentMonth.first().key)
        assertEquals("2026-11-30", currentMonth.last().key)

        assertFalse(cells.first().inCurrentMonth)
    }

    @Test
    fun todayIsMarkedOnlyOnce() {
        val cells = CalendarGrid.build(calendarOf(2026, 11), today = calendarOf(2026, 11, 5))

        val todayCells = cells.filter { it.isToday }
        assertEquals(1, todayCells.size)
        assertEquals("2026-11-05", todayCells.first().key)
        assertTrue(todayCells.first().inCurrentMonth)
    }

    @Test
    fun visibleRangeCoversWholeGrid() {
        val (from, until) = CalendarGrid.visibleRange(calendarOf(2026, 11))

        assertEquals("2026-10-26", CalendarGrid.formatKey(from))
        assertEquals("2026-12-06", CalendarGrid.formatKey(until))
    }

    @Test
    fun februaryLeapYearIncludes29Days() {
        val cells = CalendarGrid.build(calendarOf(2028, 2), today = calendarOf(2028, 2, 1))

        val currentMonth = cells.filter { it.inCurrentMonth }
        assertEquals(29, currentMonth.size)
    }
}
