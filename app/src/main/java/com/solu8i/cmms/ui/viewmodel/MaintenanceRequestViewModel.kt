package com.solu8i.cmms.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.solu8i.cmms.data.model.EquipmentListItem
import com.solu8i.cmms.data.model.MaintenanceRequestDetail
import com.solu8i.cmms.data.model.MaintenanceRequestItem
import com.solu8i.cmms.data.remote.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.ByteArrayOutputStream

class MaintenanceRequestViewModel(application: Application) : AndroidViewModel(application) {

    private val _requests = MutableStateFlow<List<MaintenanceRequestItem>>(emptyList())
    val requests: StateFlow<List<MaintenanceRequestItem>> = _requests

    private val _isRequestsLoading = MutableStateFlow(false)
    val isRequestsLoading: StateFlow<Boolean> = _isRequestsLoading

    private val _selectedStatus = MutableStateFlow("All")
    val selectedStatus: StateFlow<String> = _selectedStatus

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

    fun loadRequests(status: String? = null, page: Int = 1) {
        viewModelScope.launch {
            _isRequestsLoading.value = true
            try {
                val response = RetrofitInstance.api.getMaintenanceRequests(
                    status = status,
                    perPage = 50,
                    page = page
                )
                if (response.isSuccessful) {
                    _requests.value = response.body()?.data ?: emptyList()
                }
            } catch (_: Exception) {
            } finally {
                _isRequestsLoading.value = false
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
        return try {
            val contentResolver = getApplication<Application>().contentResolver
            val inputStream = contentResolver.openInputStream(uri) ?: return null

            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (bitmap == null) return null

            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
            bitmap.recycle()

            val tempFile = File.createTempFile("photo_", ".jpg", getApplication<Application>().cacheDir)
            tempFile.writeBytes(baos.toByteArray())

            if (tempFile.length() == 0L) {
                tempFile.delete()
                return null
            }

            tempFile
        } catch (_: Exception) {
            null
        }
    }
}
