package com.solu8i.cmms.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.solu8i.cmms.data.model.HousekeepingDashboardData
import com.solu8i.cmms.data.remote.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Dashboard housekeeping untuk user `patra-jasa`.
 *
 * Menampilkan ringkasan plan aktif, progres, dan SLA. Data dihitung server
 * sehingga angka pada dashboard selalu sama dengan laporan harian.
 */
class HousekeepingDashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val _data = MutableStateFlow<HousekeepingDashboardData?>(null)
    val data: StateFlow<HousekeepingDashboardData?> = _data

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun load(force: Boolean = false) {
        if (!force && _data.value != null) {
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val response = RetrofitInstance.api.getHousekeepingDashboard()

                if (response.isSuccessful) {
                    _data.value = response.body()?.data
                } else {
                    _errorMessage.value = when (response.code()) {
                        403 -> "Anda tidak berhak mengakses dashboard ini."
                        else -> "Gagal memuat dashboard (${response.code()})"
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Tidak dapat terhubung ke server"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refresh() = load(force = true)

    fun consumeError() {
        _errorMessage.value = null
    }

    fun reset() {
        _data.value = null
        _errorMessage.value = null
        _isLoading.value = false
    }
}
