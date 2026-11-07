package com.solu8i.cmms.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.solu8i.cmms.data.model.WorkOrderActivityItem
import com.solu8i.cmms.data.model.WorkOrderDetail
import android.net.Uri
import com.solu8i.cmms.data.remote.RetrofitInstance
import com.solu8i.cmms.ui.util.ImageFiles
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WorkOrderDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val _detail = MutableStateFlow<WorkOrderDetail?>(null)
    val detail: StateFlow<WorkOrderDetail?> = _detail

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun loadDetail(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val response = RetrofitInstance.api.getWorkOrderDetail(id)
                if (response.isSuccessful) {
                    _detail.value = response.body()?.data
                } else {
                    _errorMessage.value = "Gagal memuat detail (${response.code()})"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Tidak dapat terhubung ke server"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Simpan hasil inspeksi/eksekusi satu item checklist.
     */
    fun saveActivity(
        workOrderId: Int,
        activity: WorkOrderActivityItem,
        preInspection: String?,
        finalResult: String?,
        followUp: String?,
        executed: Boolean,
        note: String?
    ) {
        viewModelScope.launch {
            _isSubmitting.value = true
            _errorMessage.value = null

            val body = mutableMapOf<String, Any?>(
                "executed" to executed,
            )

            preInspection?.let { body["pre_inspection"] = it }
            finalResult?.let { body["final_result"] = it }
            followUp?.let { body["follow_up"] = it }
            note?.let { body["note"] = it }

            try {
                val response = RetrofitInstance.api.updateWorkOrderActivity(
                    workOrderId = workOrderId,
                    activityId = activity.id,
                    body = body
                )

                if (response.isSuccessful) {
                    _message.value = "Aktivitas berhasil disimpan"
                    refreshAfterChange(workOrderId)
                } else {
                    _errorMessage.value = when (response.code()) {
                        403 -> "Anda tidak berhak mengubah aktivitas ini"
                        422 -> "Data tidak valid, periksa kembali isian"
                        else -> "Gagal menyimpan (${response.code()})"
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Tidak dapat terhubung ke server"
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    /**
     * Ubah status WO, misalnya mulai pekerjaan atau menandai selesai.
     */
    fun updateStatus(workOrderId: Int, status: String) {
        viewModelScope.launch {
            _isSubmitting.value = true
            _errorMessage.value = null

            val body = mutableMapOf<String, Any?>("status" to status)

            if (status == "In Progress") {
                body["start_at"] = nowForApi()
            }

            if (status == "Completed") {
                body["finish_at"] = nowForApi()
            }

            try {
                val response = RetrofitInstance.api.updateWorkOrder(workOrderId, body)

                if (response.isSuccessful) {
                    _message.value = "Status diperbarui menjadi $status"
                    refreshAfterChange(workOrderId)
                } else {
                    _errorMessage.value = when (response.code()) {
                        403 -> "Anda tidak berhak mengubah work order ini"
                        422 -> "Perubahan status tidak valid"
                        else -> "Gagal memperbarui status (${response.code()})"
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Tidak dapat terhubung ke server"
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    /**
     * Unggah foto dokumentasi untuk satu item checklist (maksimal 3 foto total).
     */
    fun uploadActivityPhotos(workOrderId: Int, activityId: Int, uris: List<Uri>) {
        if (uris.isEmpty()) {
            return
        }

        viewModelScope.launch {
            _isSubmitting.value = true
            _errorMessage.value = null

            val context = getApplication<Application>()
            val parts = uris.mapNotNull { uri ->
                ImageFiles.fromUri(context, uri)?.let { file ->
                    MultipartBody.Part.createFormData(
                        "photos[]",
                        file.name,
                        file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    )
                }
            }

            if (parts.isEmpty()) {
                _isSubmitting.value = false
                _errorMessage.value = "Foto tidak dapat dibaca. Coba pilih foto lain."
                return@launch
            }

            try {
                val response = RetrofitInstance.api.uploadActivityPhotos(
                    workOrderId = workOrderId,
                    activityId = activityId,
                    photos = parts
                )

                if (response.isSuccessful) {
                    _message.value = "Foto berhasil diunggah"
                    refreshAfterChange(workOrderId)
                } else {
                    _errorMessage.value = when (response.code()) {
                        403 -> "Anda tidak berhak mengunggah foto pada aktivitas ini"
                        422 -> "Foto ditolak. Maksimal 3 foto per aktivitas, ukuran maks. 5 MB."
                        else -> "Gagal mengunggah foto (${response.code()})"
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Tidak dapat terhubung ke server"
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    /**
     * Hapus satu foto dokumentasi aktivitas.
     */
    fun deleteActivityPhoto(workOrderId: Int, activityId: Int, path: String) {
        viewModelScope.launch {
            _isSubmitting.value = true
            _errorMessage.value = null

            try {
                val response = RetrofitInstance.api.deleteActivityPhoto(
                    workOrderId = workOrderId,
                    activityId = activityId,
                    path = path
                )

                if (response.isSuccessful) {
                    _message.value = "Foto berhasil dihapus"
                    refreshAfterChange(workOrderId)
                } else {
                    _errorMessage.value = "Gagal menghapus foto (${response.code()})"
                }
            } catch (e: Exception) {
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

    private suspend fun refreshAfterChange(workOrderId: Int) {
        runCatching {
            val refreshed = RetrofitInstance.api.getWorkOrderDetail(workOrderId)
            if (refreshed.isSuccessful) {
                _detail.value = refreshed.body()?.data
            }
        }
    }

    private fun nowForApi(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
}
