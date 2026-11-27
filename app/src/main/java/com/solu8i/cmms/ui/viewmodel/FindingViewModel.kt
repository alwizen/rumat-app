package com.solu8i.cmms.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.solu8i.cmms.data.model.FindingItem
import com.solu8i.cmms.data.remote.RetrofitInstance
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FindingViewModel(application: Application) : AndroidViewModel(application) {

    private val _findings = MutableStateFlow<List<FindingItem>>(emptyList())
    val findings: StateFlow<List<FindingItem>> = _findings

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore

    private val _hasMore = MutableStateFlow(false)
    val hasMore: StateFlow<Boolean> = _hasMore

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _selectedStatus = MutableStateFlow("All")
    val selectedStatus: StateFlow<String> = _selectedStatus

    private val _selectedSeverity = MutableStateFlow("")
    val selectedSeverity: StateFlow<String> = _selectedSeverity

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _dateFrom = MutableStateFlow("")
    val dateFrom: StateFlow<String> = _dateFrom

    private val _dateTo = MutableStateFlow("")
    val dateTo: StateFlow<String> = _dateTo

    private var statusFilter: String? = null
    private var severityFilter: String? = null
    private var dateFromFilter: String? = null
    private var dateToFilter: String? = null
    private var searchFilter: String? = null

    private var page = 1
    private var lastPage = 1
    private val perPage = 30

    private var searchJob: Job? = null

    fun onQueryChanged(value: String) {
        _query.value = value
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            kotlinx.coroutines.delay(350)
            reload()
        }
    }

    fun applyFilters(status: String, severity: String, dateFrom: String, dateTo: String) {
        _selectedStatus.value = status
        _selectedSeverity.value = severity
        _dateFrom.value = dateFrom
        _dateTo.value = dateTo
        reload()
    }

    fun loadFindings(status: String? = null) {
        _selectedStatus.value = status ?: "All"
        reload()
    }

    fun loadMore() {
        if (_isLoading.value || _isLoadingMore.value || !_hasMore.value) {
            return
        }

        fetch(page = page + 1, append = true)
    }

    private fun reload() {
        statusFilter = _selectedStatus.value.takeIf { it != "All" }
        severityFilter = _selectedSeverity.value.takeIf { it.isNotBlank() }
        dateFromFilter = _dateFrom.value.takeIf { it.isNotBlank() }
        dateToFilter = _dateTo.value.takeIf { it.isNotBlank() }
        searchFilter = _query.value.takeIf { it.isNotBlank() }
        fetch(page = 1)
    }

    private fun fetch(page: Int, append: Boolean = false) {
        viewModelScope.launch {
            if (append) _isLoadingMore.value = true else _isLoading.value = true
            try {
                val response = RetrofitInstance.api.getFindings(
                    status = statusFilter,
                    severity = severityFilter,
                    dateFrom = dateFromFilter,
                    dateTo = dateToFilter,
                    search = searchFilter,
                    perPage = perPage,
                    page = page
                )
                if (response.isSuccessful) {
                    val body = response.body()
                    val items = body?.data.orEmpty()
                    _findings.value = if (append) {
                        _findings.value + items.filter { item -> _findings.value.none { it.id == item.id } }
                    } else {
                        items
                    }
                    lastPage = body?.meta?.lastPage ?: 1
                    this@FindingViewModel.page = body?.meta?.currentPage ?: page
                    _hasMore.value = this@FindingViewModel.page < lastPage
                } else {
                    _errorMessage.value = "Gagal memuat temuan (${response.code()})"
                }
            } catch (_: Exception) {
                _errorMessage.value = "Tidak dapat terhubung ke server"
            } finally {
                if (append) _isLoadingMore.value = false else _isLoading.value = false
            }
        }
    }

    /**
     * Ajukan maintenance request perbaikan dari temuan lalu perbarui item lokal.
     */
    fun submitRequest(findingId: Int) {
        if (_isSubmitting.value) {
            return
        }

        viewModelScope.launch {
            _isSubmitting.value = true
            _errorMessage.value = null

            try {
                val response = RetrofitInstance.api.submitFindingRequest(findingId)

                if (response.isSuccessful) {
                    val updated = response.body()?.data
                    if (updated != null) {
                        _findings.value = _findings.value.map { item ->
                            if (item.id == updated.id) updated else item
                        }
                    }
                    _message.value = "Request perbaikan berhasil diajukan."
                } else {
                    _errorMessage.value = when (response.code()) {
                        403 -> "Anda tidak berhak mengajukan request untuk temuan ini."
                        422 -> "Temuan sudah ditindaklanjuti atau tidak dapat diajukan."
                        else -> "Gagal mengajukan request (${response.code()})"
                    }
                }
            } catch (_: Exception) {
                _errorMessage.value = "Tidak dapat terhubung ke server"
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun consumeMessage() {
        _message.value = null
    }

    fun consumeError() {
        _errorMessage.value = null
    }

    fun reset() {
        _findings.value = emptyList()
        _selectedStatus.value = "All"
        _selectedSeverity.value = ""
        _query.value = ""
        _dateFrom.value = ""
        _dateTo.value = ""
        _message.value = null
        _errorMessage.value = null
        page = 1
        lastPage = 1
        _hasMore.value = false
    }
}
