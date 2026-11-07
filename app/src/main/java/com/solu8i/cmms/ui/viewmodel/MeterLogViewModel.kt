package com.solu8i.cmms.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.solu8i.cmms.data.model.MeterLogItem
import com.solu8i.cmms.data.remote.RetrofitInstance
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MeterLogViewModel(application: Application) : AndroidViewModel(application) {

    private val _logs = MutableStateFlow<List<MeterLogItem>>(emptyList())
    val logs: StateFlow<List<MeterLogItem>> = _logs

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _equipmentIdFilter = MutableStateFlow<Int?>(null)
    val equipmentIdFilter: StateFlow<Int?> = _equipmentIdFilter

    private var searchJob: Job? = null

    fun onQueryChanged(value: String) {
        _query.value = value

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(350)
            loadLogs()
        }
    }

    fun filterByEquipment(equipmentId: Int?) {
        _equipmentIdFilter.value = equipmentId
        _query.value = ""
        loadLogs()
    }

    /**
     * Kembali ke daftar seluruh meter log (dipakai saat membuka menu Meter Log).
     */
    fun resetFilter() {
        _equipmentIdFilter.value = null
        _query.value = ""
        loadLogs()
    }

    fun loadLogs(equipmentId: Int? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val targetEquipment = equipmentId ?: _equipmentIdFilter.value

            try {
                val response = RetrofitInstance.api.getMeterLogs(
                    equipmentId = targetEquipment,
                    search = _query.value.takeIf { it.isNotBlank() },
                    perPage = 100
                )

                if (response.isSuccessful) {
                    _logs.value = response.body()?.data.orEmpty()
                } else {
                    _logs.value = emptyList()
                    _errorMessage.value = "Gagal memuat meter log (${response.code()})"
                }
            } catch (e: Exception) {
                _logs.value = emptyList()
                _errorMessage.value = "Tidak dapat terhubung ke server"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createLog(equipmentId: Int, readingDate: String, value: String) {
        val parsed = value.replace(",", ".").toDoubleOrNull()

        if (parsed == null) {
            _errorMessage.value = "Nilai meter harus berupa angka"
            return
        }

        viewModelScope.launch {
            _isSaving.value = true
            _errorMessage.value = null

            try {
                val response = RetrofitInstance.api.createMeterLog(
                    mapOf(
                        "equipment_id" to equipmentId,
                        "reading_date" to readingDate,
                        "value" to parsed
                    )
                )

                if (response.isSuccessful) {
                    _message.value = "Meter log berhasil disimpan"
                    loadLogs()
                } else {
                    _errorMessage.value = when (response.code()) {
                        422 -> "Data tidak valid. Periksa equipment, tanggal, dan nilai."
                        else -> "Gagal menyimpan meter log (${response.code()})"
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Tidak dapat terhubung ke server"
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun consumeMessage() {
        _message.value = null
    }

    fun consumeError() {
        _errorMessage.value = null
    }

    companion object {
        fun today(): String =
            SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }
}
