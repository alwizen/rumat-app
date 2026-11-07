package com.solu8i.cmms.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.solu8i.cmms.data.model.NotificationItem
import com.solu8i.cmms.data.remote.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NotificationViewModel(application: Application) : AndroidViewModel(application) {

    private val _items = MutableStateFlow<List<NotificationItem>>(emptyList())
    val items: StateFlow<List<NotificationItem>> = _items

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun refreshUnreadCount() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getUnreadNotificationCount()

                if (response.isSuccessful) {
                    _unreadCount.value = response.body()?.data?.count ?: 0
                }
            } catch (e: Exception) {
                // Lonceng tetap tampil dengan angka terakhir jika jaringan gagal.
            }
        }
    }

    fun loadInbox() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val response = RetrofitInstance.api.getNotifications(perPage = 50)

                if (response.isSuccessful) {
                    _items.value = response.body()?.data.orEmpty()
                    _unreadCount.value = response.body()?.meta?.unreadCount ?: _unreadCount.value
                } else {
                    _items.value = emptyList()
                    _errorMessage.value = "Gagal memuat notifikasi (${response.code()})"
                }
            } catch (e: Exception) {
                _items.value = emptyList()
                _errorMessage.value = "Tidak dapat terhubung ke server"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markAsRead(id: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.markNotificationRead(id)

                if (response.isSuccessful) {
                    _items.value = _items.value.map {
                        if (it.id == id) it.copy(read = true) else it
                    }
                    refreshUnreadCount()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Tidak dapat menandai notifikasi"
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.markAllNotificationsRead()

                if (response.isSuccessful) {
                    _items.value = _items.value.map { it.copy(read = true) }
                    _unreadCount.value = 0
                }
            } catch (e: Exception) {
                _errorMessage.value = "Tidak dapat menandai semua notifikasi"
            }
        }
    }

    fun consumeError() {
        _errorMessage.value = null
    }
}
