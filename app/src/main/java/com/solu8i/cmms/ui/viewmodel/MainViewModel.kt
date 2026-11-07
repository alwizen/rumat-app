package com.solu8i.cmms.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.solu8i.cmms.data.model.DashboardData
import com.solu8i.cmms.data.model.WorkOrderItem
import com.solu8i.cmms.data.remote.RetrofitInstance
import com.solu8i.cmms.data.store.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val tokenManager = TokenManager(application)

    private val _dashboardData = MutableStateFlow<DashboardData?>(null)
    val dashboardData: StateFlow<DashboardData?> = _dashboardData

    private val _isDashboardLoading = MutableStateFlow(false)
    val isDashboardLoading: StateFlow<Boolean> = _isDashboardLoading

    private val _workOrders = MutableStateFlow<List<WorkOrderItem>>(emptyList())
    val workOrders: StateFlow<List<WorkOrderItem>> = _workOrders

    private val _isWorkOrdersLoading = MutableStateFlow(false)
    val isWorkOrdersLoading: StateFlow<Boolean> = _isWorkOrdersLoading

    private val _selectedStatus = MutableStateFlow("All")
    val selectedStatus: StateFlow<String> = _selectedStatus

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName

    init {
        loadUserName()
    }

    private fun loadUserName() {
        viewModelScope.launch {
            tokenManager.userName.collect { name ->
                _userName.value = name ?: ""
            }
        }
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _isDashboardLoading.value = true
            try {
                val response = RetrofitInstance.api.getDashboard()
                if (response.isSuccessful) {
                    _dashboardData.value = response.body()?.data
                }
            } catch (_: Exception) {
            } finally {
                _isDashboardLoading.value = false
            }
        }
    }

    fun onStatusSelected(status: String) {
        _selectedStatus.value = status
    }

    fun loadWorkOrders(status: String? = null, page: Int = 1) {
        viewModelScope.launch {
            _isWorkOrdersLoading.value = true
            try {
                val response = RetrofitInstance.api.getWorkOrders(
                    status = status,
                    perPage = 50,
                    page = page
                )
                if (response.isSuccessful) {
                    _workOrders.value = response.body()?.data ?: emptyList()
                }
            } catch (_: Exception) {
            } finally {
                _isWorkOrdersLoading.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            com.solu8i.cmms.data.remote.TokenProvider.token = null
            tokenManager.clearAuthData()
        }
    }
}
