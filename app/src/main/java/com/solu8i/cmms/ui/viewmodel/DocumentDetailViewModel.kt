package com.solu8i.cmms.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.solu8i.cmms.data.model.DocumentItem
import com.solu8i.cmms.data.remote.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DocumentDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val _document = MutableStateFlow<DocumentItem?>(null)
    val document: StateFlow<DocumentItem?> = _document

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun loadDetail(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val response = RetrofitInstance.api.getDocumentDetail(id)

                if (response.isSuccessful) {
                    _document.value = response.body()?.data
                } else {
                    _errorMessage.value = "Gagal memuat detail dokumen (${response.code()})"
                }
            } catch (e: Exception) {
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
