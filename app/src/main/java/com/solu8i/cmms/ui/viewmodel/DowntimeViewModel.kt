package com.solu8i.cmms.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.solu8i.cmms.data.model.DowntimeItem
import com.solu8i.cmms.data.remote.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DowntimeViewModel(application: Application) : AndroidViewModel(application) {

    private val _items = MutableStateFlow<List<DowntimeItem>>(emptyList())
    val items: StateFlow<List<DowntimeItem>> = _items

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val response = RetrofitInstance.api.getDowntime(perPage = 100)

                if (response.isSuccessful) {
                    _items.value = response.body()?.data.orEmpty()
                } else {
                    _items.value = emptyList()
                    _errorMessage.value = "Gagal memuat downtime (${response.code()})"
                }
            } catch (e: Exception) {
                _items.value = emptyList()
                _errorMessage.value = "Tidak dapat terhubung ke server"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun consumeError() {
        _errorMessage.value = null
    }
}
