package com.solu8i.cmms.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.solu8i.cmms.data.model.CalendarEvent
import com.solu8i.cmms.data.remote.RetrofitInstance
import com.solu8i.cmms.ui.util.CalendarGrid
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    private val _events = MutableStateFlow<List<CalendarEvent>>(emptyList())
    val events: StateFlow<List<CalendarEvent>> = _events

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _selectedDate = MutableStateFlow<String?>(
        apiDateFormat.format(Calendar.getInstance().time)
    )
    val selectedDate: StateFlow<String?> = _selectedDate

    private val _visibleMonth = MutableStateFlow(startOfMonth(Calendar.getInstance()))
    val visibleMonth: StateFlow<Calendar> = _visibleMonth

    fun selectDate(date: String) {
        _selectedDate.value = date
    }

    fun previousMonth() {
        val calendar = (_visibleMonth.value.clone() as Calendar).apply {
            add(Calendar.MONTH, -1)
        }
        _visibleMonth.value = startOfMonth(calendar)
        selectedDateForMonth()
        reload()
    }

    fun nextMonth() {
        val calendar = (_visibleMonth.value.clone() as Calendar).apply {
            add(Calendar.MONTH, 1)
        }
        _visibleMonth.value = startOfMonth(calendar)
        selectedDateForMonth()
        reload()
    }

    fun goToToday() {
        _visibleMonth.value = startOfMonth(Calendar.getInstance())
        selectedDateForMonth()
        reload()
    }

    fun reload() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val (from, until) = visibleRange()

            try {
                val response = RetrofitInstance.api.getMaintenanceCalendar(
                    dateFrom = apiDateFormat.format(from.time),
                    dateTo = apiDateFormat.format(until.time)
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    _events.value = response.body()?.data?.events.orEmpty()
                } else {
                    _events.value = emptyList()
                    _errorMessage.value = "Gagal memuat kalender (${response.code()})"
                }
            } catch (e: Exception) {
                _events.value = emptyList()
                _errorMessage.value = "Tidak dapat terhubung ke server"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Hari yang dipilih: kalau tanggal yang sedang dipilih masih di bulan yang sama,
     * pertahankan. Kalau tidak, pilih tanggal 1.
     */
    private fun selectedDateForMonth() {
        val month = _visibleMonth.value
        val selected = _selectedDate.value

        val isSameMonth = selected != null && runCatching {
            val parsed = apiDateFormat.parse(selected)!!
            val temp = Calendar.getInstance().apply { time = parsed }
            temp.get(Calendar.YEAR) == month.get(Calendar.YEAR) &&
                temp.get(Calendar.MONTH) == month.get(Calendar.MONTH)
        }.getOrDefault(false)

        if (!isSameMonth) {
            _selectedDate.value = apiDateFormat.format(month.time)
        }
    }

    /**
     * Rentang yang dimuat: seluruh grid bulan yang terlihat (6 baris x 7 hari).
     */
    private fun visibleRange(): Pair<Calendar, Calendar> = CalendarGrid.visibleRange(_visibleMonth.value)

    companion object {
        fun startOfMonth(calendar: Calendar): Calendar {
            return (calendar.clone() as Calendar).apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        }
    }
}
