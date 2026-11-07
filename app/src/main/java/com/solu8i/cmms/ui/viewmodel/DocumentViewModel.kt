package com.solu8i.cmms.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.solu8i.cmms.data.model.DocumentItem
import com.solu8i.cmms.data.remote.RetrofitInstance
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DocumentViewModel(application: Application) : AndroidViewModel(application) {

    private val _documents = MutableStateFlow<List<DocumentItem>>(emptyList())
    val documents: StateFlow<List<DocumentItem>> = _documents

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _typeFilter = MutableStateFlow("")
    val typeFilter: StateFlow<String> = _typeFilter

    private val _expiryFilter = MutableStateFlow("")
    val expiryFilter: StateFlow<String> = _expiryFilter

    private var searchJob: Job? = null

    fun onQueryChanged(value: String) {
        _query.value = value

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(350)
            load()
        }
    }

    fun setTypeFilter(type: String) {
        _typeFilter.value = type
        load()
    }

    fun setExpiryFilter(status: String) {
        _expiryFilter.value = status
        load()
    }

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val response = RetrofitInstance.api.getDocuments(
                    search = _query.value.takeIf { it.isNotBlank() },
                    type = _typeFilter.value.takeIf { it.isNotBlank() },
                    expiryStatus = _expiryFilter.value.takeIf { it.isNotBlank() },
                    perPage = 100
                )

                if (response.isSuccessful) {
                    _documents.value = response.body()?.data.orEmpty()
                } else {
                    _documents.value = emptyList()
                    _errorMessage.value = "Gagal memuat dokumen (${response.code()})"
                }
            } catch (e: Exception) {
                _documents.value = emptyList()
                _errorMessage.value = "Tidak dapat terhubung ke server"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun consumeError() {
        _errorMessage.value = null
    }

    companion object {
        val TYPE_OPTIONS = listOf(
            "" to "Semua",
            "certification" to "Sertifikat",
            "pnid" to "P&ID",
            "user_manual" to "User Manual",
            "drawing" to "Drawing",
            "procedure" to "SOP",
            "inspection_report" to "Inspection Report",
            "other" to "Lainnya"
        )

        val EXPIRY_OPTIONS = listOf(
            "" to "Semua",
            "expired" to "Expired",
            "expiring_soon" to "Akan Expired",
            "valid" to "Valid",
            "no_expiry" to "Tanpa Expiry"
        )
    }
}
