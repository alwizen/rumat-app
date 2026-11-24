package com.solu8i.cmms.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.solu8i.cmms.data.model.DashboardData
import com.solu8i.cmms.data.model.WorkOrderItem
import com.solu8i.cmms.data.remote.RetrofitInstance
import com.solu8i.cmms.data.store.TokenManager
import kotlinx.coroutines.Job
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

    private val _isWorkOrdersLoadingMore = MutableStateFlow(false)
    val isWorkOrdersLoadingMore: StateFlow<Boolean> = _isWorkOrdersLoadingMore

    private val _hasMoreWorkOrders = MutableStateFlow(false)
    val hasMoreWorkOrders: StateFlow<Boolean> = _hasMoreWorkOrders

    private var workOrdersPage = 1
    private var workOrdersLastPage = 1
    private var workOrdersStatus: String? = null
    private var workOrdersClassification: String? = null
    private var workOrdersSearch: String? = null

    private val workOrdersPerPage = 30

    private val _selectedStatus = MutableStateFlow("All")
    val selectedStatus: StateFlow<String> = _selectedStatus

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _classificationFilter = MutableStateFlow("")
    val classificationFilter: StateFlow<String> = _classificationFilter

    private val _dateFrom = MutableStateFlow("")
    val dateFrom: StateFlow<String> = _dateFrom

    private val _dateTo = MutableStateFlow("")
    val dateTo: StateFlow<String> = _dateTo

    private var workOrdersDateFrom: String? = null
    private var workOrdersDateTo: String? = null

    private var searchJob: Job? = null

    fun onQueryChanged(value: String) {
        _query.value = value
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            kotlinx.coroutines.delay(350)
            reloadWorkOrders()
        }
    }

    fun applyFilters(status: String, classification: String, dateFrom: String = "", dateTo: String = "") {
        _selectedStatus.value = status
        _classificationFilter.value = classification
        _dateFrom.value = dateFrom
        _dateTo.value = dateTo
        reloadWorkOrders()
    }

    /**
     * Muat ulang dari halaman pertama memakai seluruh filter aktif.
     */
    private fun reloadWorkOrders() {
        workOrdersStatus = _selectedStatus.value.takeIf { it != "All" }
        workOrdersClassification = _classificationFilter.value.takeIf { it.isNotBlank() }
        workOrdersSearch = _query.value.takeIf { it.isNotBlank() }
        workOrdersDateFrom = _dateFrom.value.takeIf { it.isNotBlank() }
        workOrdersDateTo = _dateTo.value.takeIf { it.isNotBlank() }
        fetchWorkOrders(page = 1)
    }

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

    /**
     * Muat ulang dari halaman pertama (dipakai saat layar dibuka / ganti filter).
     */
    fun loadWorkOrders(status: String? = null) {
        _selectedStatus.value = status ?: "All"
        reloadWorkOrders()
    }

    /**
     * Muat halaman berikutnya (infinite scroll). Diabaikan bila sedang loading / sudah habis.
     */
    fun loadMoreWorkOrders() {
        if (_isWorkOrdersLoading.value || _isWorkOrdersLoadingMore.value || !_hasMoreWorkOrders.value) {
            return
        }

        fetchWorkOrders(page = workOrdersPage + 1, append = true)
    }

    private fun fetchWorkOrders(page: Int, append: Boolean = false) {
        viewModelScope.launch {
            if (append) _isWorkOrdersLoadingMore.value = true else _isWorkOrdersLoading.value = true
            try {
                val response = RetrofitInstance.api.getWorkOrders(
                    status = workOrdersStatus,
                    classification = workOrdersClassification,
                    search = workOrdersSearch,
                    dateFrom = workOrdersDateFrom,
                    dateTo = workOrdersDateTo,
                    perPage = workOrdersPerPage,
                    page = page
                )
                if (response.isSuccessful) {
                    val body = response.body()
                    val items = body?.data.orEmpty()
                    _workOrders.value = if (append) {
                        _workOrders.value + items.filter { item -> _workOrders.value.none { it.id == item.id } }
                    } else {
                        items
                    }
                    workOrdersLastPage = body?.meta?.lastPage ?: 1
                    workOrdersPage = body?.meta?.currentPage ?: page
                    _hasMoreWorkOrders.value = workOrdersPage < workOrdersLastPage
                }
            } catch (_: Exception) {
            } finally {
                if (append) _isWorkOrdersLoadingMore.value = false else _isWorkOrdersLoading.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            com.solu8i.cmms.data.remote.TokenProvider.token = null
            tokenManager.clearAuthData()
        }
    }

    fun reset() {
        _dashboardData.value = null
        _workOrders.value = emptyList()
        _isDashboardLoading.value = false
        _isWorkOrdersLoading.value = false
        _isWorkOrdersLoadingMore.value = false
        _hasMoreWorkOrders.value = false
        workOrdersPage = 1
        workOrdersLastPage = 1
        workOrdersStatus = null
        workOrdersClassification = null
        workOrdersSearch = null
        _selectedStatus.value = "All"
        _query.value = ""
        _classificationFilter.value = ""
        _dateFrom.value = ""
        _dateTo.value = ""
        workOrdersDateFrom = null
        workOrdersDateTo = null
        _userName.value = ""
    }
}
