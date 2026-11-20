package com.solu8i.cmms.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.solu8i.cmms.data.model.EquipmentListItem
import com.solu8i.cmms.data.model.MaintenanceRequestDetail
import com.solu8i.cmms.data.model.MaintenanceRequestItem
import com.solu8i.cmms.data.remote.RetrofitInstance
import com.solu8i.cmms.ui.util.ImageFiles
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class MaintenanceRequestViewModel(application: Application) : AndroidViewModel(application) {

    private val _requests = MutableStateFlow<List<MaintenanceRequestItem>>(emptyList())
    val requests: StateFlow<List<MaintenanceRequestItem>> = _requests

    private val _isRequestsLoading = MutableStateFlow(false)
    val isRequestsLoading: StateFlow<Boolean> = _isRequestsLoading

    private val _isRequestsLoadingMore = MutableStateFlow(false)
    val isRequestsLoadingMore: StateFlow<Boolean> = _isRequestsLoadingMore

    private val _hasMoreRequests = MutableStateFlow(false)
    val hasMoreRequests: StateFlow<Boolean> = _hasMoreRequests

    private var requestsPage = 1
    private var requestsLastPage = 1
    private var requestsStatus: String? = null

    private val requestsPerPage = 30

    private val _selectedStatus = MutableStateFlow("All")
    val selectedStatus: StateFlow<String> = _selectedStatus

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _dateFrom = MutableStateFlow("")
    val dateFrom: StateFlow<String> = _dateFrom

    private val _dateTo = MutableStateFlow("")
    val dateTo: StateFlow<String> = _dateTo

    private var requestsDateFrom: String? = null
    private var requestsDateTo: String? = null

    private var searchJob: kotlinx.coroutines.Job? = null

    fun onQueryChanged(value: String) {
        _query.value = value
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            kotlinx.coroutines.delay(350)
            reloadRequests()
        }
    }

    fun applyFilters(status: String, dateFrom: String = "", dateTo: String = "") {
        _selectedStatus.value = status
        _dateFrom.value = dateFrom
        _dateTo.value = dateTo
        reloadRequests()
    }

    private fun reloadRequests() {
        requestsStatus = _selectedStatus.value.takeIf { it != "All" }
        requestsDateFrom = _dateFrom.value.takeIf { it.isNotBlank() }
        requestsDateTo = _dateTo.value.takeIf { it.isNotBlank() }
        fetchRequests(page = 1)
    }

    private val _detail = MutableStateFlow<MaintenanceRequestDetail?>(null)
    val detail: StateFlow<MaintenanceRequestDetail?> = _detail

    private val _isDetailLoading = MutableStateFlow(false)
    val isDetailLoading: StateFlow<Boolean> = _isDetailLoading

    private val _equipmentList = MutableStateFlow<List<EquipmentListItem>>(emptyList())
    val equipmentList: StateFlow<List<EquipmentListItem>> = _equipmentList

    private val _isCreating = MutableStateFlow(false)
    val isCreating: StateFlow<Boolean> = _isCreating

    private val _createSuccess = MutableStateFlow(false)
    val createSuccess: StateFlow<Boolean> = _createSuccess

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun loadRequests(status: String? = null) {
        _selectedStatus.value = status ?: "All"
        reloadRequests()
    }

    fun loadMoreRequests() {
        if (_isRequestsLoading.value || _isRequestsLoadingMore.value || !_hasMoreRequests.value) {
            return
        }

        fetchRequests(page = requestsPage + 1, append = true)
    }

    private fun fetchRequests(page: Int, append: Boolean = false) {
        viewModelScope.launch {
            if (append) _isRequestsLoadingMore.value = true else _isRequestsLoading.value = true
            try {
                val response = RetrofitInstance.api.getMaintenanceRequests(
                    status = requestsStatus,
                    search = _query.value.takeIf { it.isNotBlank() },
                    dateFrom = requestsDateFrom,
                    dateTo = requestsDateTo,
                    perPage = requestsPerPage,
                    page = page
                )
                if (response.isSuccessful) {
                    val body = response.body()
                    val items = body?.data.orEmpty()
                    _requests.value = if (append) {
                        _requests.value + items.filter { item -> _requests.value.none { it.id == item.id } }
                    } else {
                        items
                    }
                    requestsLastPage = body?.meta?.lastPage ?: 1
                    requestsPage = body?.meta?.currentPage ?: page
                    _hasMoreRequests.value = requestsPage < requestsLastPage
                }
            } catch (_: Exception) {
            } finally {
                if (append) _isRequestsLoadingMore.value = false else _isRequestsLoading.value = false
            }
        }
    }

    fun onStatusSelected(status: String) {
        _selectedStatus.value = status
    }

    fun loadDetail(id: Int) {
        viewModelScope.launch {
            _isDetailLoading.value = true
            try {
                val response = RetrofitInstance.api.getMaintenanceRequestDetail(id)
                if (response.isSuccessful) {
                    _detail.value = response.body()?.data
                }
            } catch (_: Exception) {
            } finally {
                _isDetailLoading.value = false
            }
        }
    }

    fun loadEquipment() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getEquipment(perPage = 100)
                if (response.isSuccessful) {
                    _equipmentList.value = response.body()?.data ?: emptyList()
                }
            } catch (_: Exception) {
            }
        }
    }

    fun createRequest(
        equipmentId: Int,
        operationStatus: String,
        description: String,
        damageDate: String,
        damageTime: String?,
        equipmentCondition: String?,
        impact: String?,
        earlyAction: String?,
        status: String,
        photos: List<Uri>
    ) {
        viewModelScope.launch {
            _isCreating.value = true
            _errorMessage.value = null
            try {
                val photoParts = photos.mapNotNull { uri ->
                    val file = getFileFromUri(uri)
                    file?.let {
                        val requestFile = it.asRequestBody("image/jpeg".toMediaTypeOrNull())
                        MultipartBody.Part.createFormData("photo[]", it.name, requestFile)
                    }
                }

                val response = RetrofitInstance.api.createMaintenanceRequest(
                    equipmentId = equipmentId.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                    operationStatus = operationStatus.toRequestBody("text/plain".toMediaTypeOrNull()),
                    description = description.toRequestBody("text/plain".toMediaTypeOrNull()),
                    damageDate = damageDate.toRequestBody("text/plain".toMediaTypeOrNull()),
                    damageTime = damageTime?.takeIf { it.isNotBlank() }?.toRequestBody("text/plain".toMediaTypeOrNull()),
                    equipmentCondition = equipmentCondition?.takeIf { it.isNotBlank() }?.toRequestBody("text/plain".toMediaTypeOrNull()),
                    impact = impact?.takeIf { it.isNotBlank() }?.toRequestBody("text/plain".toMediaTypeOrNull()),
                    earlyAction = earlyAction?.takeIf { it.isNotBlank() }?.toRequestBody("text/plain".toMediaTypeOrNull()),
                    status = status.toRequestBody("text/plain".toMediaTypeOrNull()),
                    photos = photoParts.ifEmpty { null }
                )

                if (response.isSuccessful) {
                    _createSuccess.value = true
                    loadRequests()
                } else {
                    val errorBody = response.errorBody()?.string()
                    val parsedMessage = try {
                        val json = org.json.JSONObject(errorBody ?: "")
                        val errors = json.optJSONObject("errors")
                        errors?.keys()?.asSequence()?.joinToString("\n") { key ->
                            "$key: ${errors.getJSONArray(key).getString(0)}"
                        } ?: json.optString("message", "Gagal membuat request")
                    } catch (_: Exception) {
                        response.message().ifBlank { "Gagal membuat request" }
                    }
                    _errorMessage.value = parsedMessage
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Terjadi kesalahan"
            } finally {
                _isCreating.value = false
            }
        }
    }

    fun deleteRequest(id: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.deleteMaintenanceRequest(id)
                if (response.isSuccessful) {
                    loadRequests()
                    _detail.value = null
                }
            } catch (_: Exception) {
            }
        }
    }

    fun resetCreateState() {
        _createSuccess.value = false
        _errorMessage.value = null
    }

    private fun getFileFromUri(uri: Uri): File? {
        return ImageFiles.fromUri(getApplication<Application>(), uri)
    }

    fun reset() {
        _requests.value = emptyList()
        _detail.value = null
        _equipmentList.value = emptyList()
        _selectedStatus.value = "All"
        _createSuccess.value = false
        _errorMessage.value = null
        _isRequestsLoadingMore.value = false
        _hasMoreRequests.value = false
        requestsPage = 1
        requestsLastPage = 1
        requestsStatus = null
        _query.value = ""
        _dateFrom.value = ""
        _dateTo.value = ""
        requestsDateFrom = null
        requestsDateTo = null
    }
}
