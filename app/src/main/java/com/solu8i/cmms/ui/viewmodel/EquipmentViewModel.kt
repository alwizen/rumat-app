package com.solu8i.cmms.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.solu8i.cmms.data.model.EquipmentDetailData
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

    private val _detail = MutableStateFlow<EquipmentDetailData?>(null)
    val detail: StateFlow<EquipmentDetailData?> = _detail

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

    fun loadEquipment(search: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val response = RetrofitInstance.api.getEquipment(
                    search = search?.takeIf { it.isNotBlank() } ?: _query.value.takeIf { it.isNotBlank() },
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
    }
}
