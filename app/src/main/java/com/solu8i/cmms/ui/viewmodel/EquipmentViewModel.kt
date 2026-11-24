package com.solu8i.cmms.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.solu8i.cmms.data.model.EquipmentDetailData
import com.solu8i.cmms.data.model.FilterOptionItem
import com.solu8i.cmms.data.model.MaintenanceHistoryItem
import com.solu8i.cmms.data.model.EquipmentListItem
import com.solu8i.cmms.data.remote.RetrofitInstance
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EquipmentViewModel(application: Application) : AndroidViewModel(application) {

    private val _equipment = MutableStateFlow<List<EquipmentListItem>>(emptyList())
    val equipment: StateFlow<List<EquipmentListItem>> = _equipment

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _statusFilter = MutableStateFlow("")
    val statusFilter: StateFlow<String> = _statusFilter

    private val _areaFilter = MutableStateFlow("")
    val areaFilter: StateFlow<String> = _areaFilter

    private val _typeFilter = MutableStateFlow("")
    val typeFilter: StateFlow<String> = _typeFilter

    private val _areaOptions = MutableStateFlow<List<FilterOptionItem>>(emptyList())
    val areaOptions: StateFlow<List<FilterOptionItem>> = _areaOptions

    private val _typeOptions = MutableStateFlow<List<FilterOptionItem>>(emptyList())
    val typeOptions: StateFlow<List<FilterOptionItem>> = _typeOptions

    private val _detail = MutableStateFlow<EquipmentDetailData?>(null)
    val detail: StateFlow<EquipmentDetailData?> = _detail

    private val _histories = MutableStateFlow<List<MaintenanceHistoryItem>>(emptyList())
    val histories: StateFlow<List<MaintenanceHistoryItem>> = _histories

    private val _isHistoriesLoading = MutableStateFlow(false)
    val isHistoriesLoading: StateFlow<Boolean> = _isHistoriesLoading

    private val _isDetailLoading = MutableStateFlow(false)
    val isDetailLoading: StateFlow<Boolean> = _isDetailLoading

    private var searchJob: Job? = null

    fun onQueryChanged(value: String) {
        _query.value = value

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(350)
            loadEquipment(value)
        }
    }

    fun applyFilters(status: String, areaId: String, typeId: String) {
        _statusFilter.value = status
        _areaFilter.value = areaId
        _typeFilter.value = typeId
        loadEquipment()
    }

    fun loadFilterOptions() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getEquipmentFilterOptions()
                if (response.isSuccessful) {
                    val data = response.body()?.data
                    _areaOptions.value = data?.areas.orEmpty()
                    _typeOptions.value = data?.equipmentTypes.orEmpty()
                }
            } catch (_: Exception) {
            }
        }
    }

    fun loadEquipment(search: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val response = RetrofitInstance.api.getEquipment(
                    search = search?.takeIf { it.isNotBlank() } ?: _query.value.takeIf { it.isNotBlank() },
                    status = _statusFilter.value.takeIf { it.isNotBlank() },
                    areaId = _areaFilter.value.takeIf { it.isNotBlank() }?.toIntOrNull(),
                    equipmentTypeId = _typeFilter.value.takeIf { it.isNotBlank() }?.toIntOrNull(),
                    perPage = 100
                )

                if (response.isSuccessful) {
                    _equipment.value = response.body()?.data.orEmpty()
                } else {
                    _equipment.value = emptyList()
                    _errorMessage.value = "Gagal memuat equipment (${response.code()})"
                }
            } catch (e: Exception) {
                _equipment.value = emptyList()
                _errorMessage.value = "Tidak dapat terhubung ke server"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadDetail(id: Int) {
        viewModelScope.launch {
            _isDetailLoading.value = true
            _detail.value = null
            _errorMessage.value = null

            try {
                val response = RetrofitInstance.api.getEquipmentDetail(id)

                if (response.isSuccessful) {
                    _detail.value = response.body()?.data
                } else {
                    _errorMessage.value = "Gagal memuat detail equipment (${response.code()})"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Tidak dapat terhubung ke server"
            } finally {
                _isDetailLoading.value = false
            }
        }

        loadHistories(id)
    }

    fun loadHistories(id: Int) {
        viewModelScope.launch {
            _isHistoriesLoading.value = true
            _histories.value = emptyList()
            try {
                val response = RetrofitInstance.api.getEquipmentMaintenanceHistories(id, perPage = 50)
                if (response.isSuccessful) {
                    _histories.value = response.body()?.data.orEmpty()
                }
            } catch (_: Exception) {
            } finally {
                _isHistoriesLoading.value = false
            }
        }
    }

    fun reset() {
        _equipment.value = emptyList()
        _detail.value = null
        _histories.value = emptyList()
        _isHistoriesLoading.value = false
        _query.value = ""
        _errorMessage.value = null
    }
}
