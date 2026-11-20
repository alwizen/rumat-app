package com.solu8i.cmms.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.solu8i.cmms.data.model.HousekeepingAreaSimple
import com.solu8i.cmms.data.model.HousekeepingFacilityWithTasks
import com.solu8i.cmms.data.model.HousekeepingRequestItem
import com.solu8i.cmms.data.remote.RetrofitInstance
import com.solu8i.cmms.ui.util.ImageFiles
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class HousekeepingRequestViewModel(application: Application) : AndroidViewModel(application) {

    private val _requests = MutableStateFlow<List<HousekeepingRequestItem>>(emptyList())
    val requests: StateFlow<List<HousekeepingRequestItem>> = _requests

    private val _isRequestsLoading = MutableStateFlow(false)
    val isRequestsLoading: StateFlow<Boolean> = _isRequestsLoading

    private val _isRequestsLoadingMore = MutableStateFlow(false)
    val isRequestsLoadingMore: StateFlow<Boolean> = _isRequestsLoadingMore

    private val _hasMoreRequests = MutableStateFlow(false)
    val hasMoreRequests: StateFlow<Boolean> = _hasMoreRequests

    private var requestsPage = 1
    private var requestsLastPage = 1
    private var requestsStatus: String? = null
    private var requestsDateFrom: String? = null
    private var requestsDateTo: String? = null

    private val requestsPerPage = 30

    private val _selectedStatus = MutableStateFlow("All")
    val selectedStatus: StateFlow<String> = _selectedStatus

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _dateFrom = MutableStateFlow("")
    val dateFrom: StateFlow<String> = _dateFrom

    private val _dateTo = MutableStateFlow("")
    val dateTo: StateFlow<String> = _dateTo

    private var searchJob: Job? = null

    private val _detail = MutableStateFlow<HousekeepingRequestItem?>(null)
    val detail: StateFlow<HousekeepingRequestItem?> = _detail

    private val _isDetailLoading = MutableStateFlow(false)
    val isDetailLoading: StateFlow<Boolean> = _isDetailLoading

    private val _areas = MutableStateFlow<List<HousekeepingAreaSimple>>(emptyList())
    val areas: StateFlow<List<HousekeepingAreaSimple>> = _areas

    private val _isLoadingAreas = MutableStateFlow(false)
    val isLoadingAreas: StateFlow<Boolean> = _isLoadingAreas

    private val _facilities = MutableStateFlow<List<HousekeepingFacilityWithTasks>>(emptyList())
    val facilities: StateFlow<List<HousekeepingFacilityWithTasks>> = _facilities

    private val _isLoadingFacilities = MutableStateFlow(false)
    val isLoadingFacilities: StateFlow<Boolean> = _isLoadingFacilities

    private var loadedFacilitiesAreaId: Int? = null

    private val _isCreating = MutableStateFlow(false)
    val isCreating: StateFlow<Boolean> = _isCreating

    private val _createSuccess = MutableStateFlow(false)
    val createSuccess: StateFlow<Boolean> = _createSuccess

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    fun onQueryChanged(value: String) {
        _query.value = value
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(350)
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
                val response = RetrofitInstance.api.getHousekeepingRequests(
                    status = requestsStatus,
                    search = _query.value.takeIf { it.isNotBlank() },
                    dateFrom = requestsDateFrom,
                    dateTo = requestsDateTo,
                    mine = true,
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
                } else if (!append) {
                    _errorMessage.value = "Gagal memuat request (${response.code()})"
                }
            } catch (_: Exception) {
                if (!append) _errorMessage.value = "Tidak dapat terhubung ke server"
            } finally {
                if (append) _isRequestsLoadingMore.value = false else _isRequestsLoading.value = false
            }
        }
    }

    fun loadDetail(id: Int) {
        viewModelScope.launch {
            _isDetailLoading.value = true
            _errorMessage.value = null
            try {
                val response = RetrofitInstance.api.getHousekeepingRequestDetail(id)
                if (response.isSuccessful) {
                    _detail.value = response.body()?.data
                } else {
                    _errorMessage.value = "Gagal memuat detail (${response.code()})"
                }
            } catch (_: Exception) {
                _errorMessage.value = "Tidak dapat terhubung ke server"
            } finally {
                _isDetailLoading.value = false
            }
        }
    }

    fun loadAreas(force: Boolean = false) {
        if (!force && _areas.value.isNotEmpty()) {
            return
        }

        viewModelScope.launch {
            _isLoadingAreas.value = true
            try {
                val response = RetrofitInstance.api.getHousekeepingAreas()
                if (response.isSuccessful) {
                    _areas.value = response.body()?.data.orEmpty()
                }
            } catch (_: Exception) {
            } finally {
                _isLoadingAreas.value = false
            }
        }
    }

    fun loadFacilities(areaId: Int, force: Boolean = false) {
        if (!force && loadedFacilitiesAreaId == areaId) {
            return
        }

        viewModelScope.launch {
            _isLoadingFacilities.value = true
            try {
                val response = RetrofitInstance.api.getHousekeepingAreaFacilities(areaId)
                if (response.isSuccessful) {
                    _facilities.value = response.body()?.data.orEmpty()
                    loadedFacilitiesAreaId = areaId
                }
            } catch (_: Exception) {
            } finally {
                _isLoadingFacilities.value = false
            }
        }
    }

    fun clearFacilities() {
        _facilities.value = emptyList()
        loadedFacilitiesAreaId = null
    }

    fun createRequest(
        areaId: Int,
        facilityId: Int?,
        requestDate: String,
        requestTime: String?,
        description: String,
        priority: String,
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
                        MultipartBody.Part.createFormData("photos[]", it.name, requestFile)
                    }
                }

                val response = RetrofitInstance.api.createHousekeepingRequest(
                    areaId = areaId.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                    facilityId = facilityId?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull()),
                    requestDate = requestDate.toRequestBody("text/plain".toMediaTypeOrNull()),
                    requestTime = requestTime?.takeIf { it.isNotBlank() }
                        ?.toRequestBody("text/plain".toMediaTypeOrNull()),
                    description = description.toRequestBody("text/plain".toMediaTypeOrNull()),
                    priority = priority.toRequestBody("text/plain".toMediaTypeOrNull()),
                    photos = photoParts.ifEmpty { null }
                )

                if (response.isSuccessful) {
                    _createSuccess.value = true
                    loadRequests()
                } else {
                    _errorMessage.value = parseError(response.errorBody()?.string(), response.message())
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
                val response = RetrofitInstance.api.deleteHousekeepingRequest(id)
                if (response.isSuccessful) {
                    _message.value = "Request berhasil dihapus"
                    _detail.value = null
                    loadRequests()
                } else {
                    _errorMessage.value = parseError(response.errorBody()?.string(), response.message())
                }
            } catch (_: Exception) {
                _errorMessage.value = "Tidak dapat terhubung ke server"
            }
        }
    }

    fun resetCreateState() {
        _createSuccess.value = false
        _errorMessage.value = null
    }

    fun consumeMessage() {
        _message.value = null
    }

    fun consumeError() {
        _errorMessage.value = null
    }

    private fun parseError(errorBody: String?, fallback: String): String {
        return try {
            val json = org.json.JSONObject(errorBody ?: "")
            val errors = json.optJSONObject("errors")
            errors?.keys()?.asSequence()?.joinToString("\n") { key ->
                "$key: ${errors.getJSONArray(key).getString(0)}"
            } ?: json.optString("message", fallback.ifBlank { "Terjadi kesalahan" })
        } catch (_: Exception) {
            fallback.ifBlank { "Terjadi kesalahan" }
        }
    }

    private fun getFileFromUri(uri: Uri): File? {
        return ImageFiles.fromUri(getApplication<Application>(), uri)
    }

    fun reset() {
        _requests.value = emptyList()
        _detail.value = null
        _areas.value = emptyList()
        _facilities.value = emptyList()
        _selectedStatus.value = "All"
        _query.value = ""
        _dateFrom.value = ""
        _dateTo.value = ""
        requestsDateFrom = null
        requestsDateTo = null
        requestsStatus = null
        requestsPage = 1
        requestsLastPage = 1
        loadedFacilitiesAreaId = null
        _createSuccess.value = false
        _errorMessage.value = null
        _message.value = null
        _isRequestsLoadingMore.value = false
        _hasMoreRequests.value = false
    }
}
