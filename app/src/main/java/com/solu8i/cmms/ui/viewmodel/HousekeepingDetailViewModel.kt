package com.solu8i.cmms.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.solu8i.cmms.data.model.HousekeepingPlanDetail
import com.solu8i.cmms.data.model.HousekeepingPlanFacilityItem
import com.solu8i.cmms.data.model.HousekeepingTaskItem
import com.solu8i.cmms.data.remote.RetrofitInstance
import com.solu8i.cmms.ui.util.ImageFiles
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class HousekeepingDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val _detail = MutableStateFlow<HousekeepingPlanDetail?>(null)
    val detail: StateFlow<HousekeepingPlanDetail?> = _detail

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
                val response = RetrofitInstance.api.getHousekeepingPlanDetail(id)
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

    fun uploadFacilityPhotos(planId: Int, planFacility: HousekeepingPlanFacilityItem, type: String, uris: List<Uri>) {
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
                val response = RetrofitInstance.api.uploadHousekeepingFacilityPhotos(
                    planId = planId,
                    planFacilityId = planFacility.id,
                    type = type.toRequestBody("text/plain".toMediaTypeOrNull()),
                    photos = parts
                )

                if (response.isSuccessful) {
                    _message.value = "Foto ${if (type == "before") "before" else "after"} berhasil diunggah"
                    refreshAfterChange(planId)
                } else {
                    _errorMessage.value = when (response.code()) {
                        422 -> "Foto ditolak. Maksimal 5 foto, ukuran maks. 5 MB."
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
     * Mulai seluruh item checklist sekaligus (tombol "Mulai Kerja" per fasilitas).
     * Hanya item berstatus Pending yang diubah menjadi In Progress.
     */
    fun startTasks(planId: Int, tasks: List<HousekeepingTaskItem>) {
        val pending = tasks.filter { it.status == "Pending" }

        if (pending.isEmpty()) {
            return
        }

        viewModelScope.launch {
            _isSubmitting.value = true
            _errorMessage.value = null

            try {
                var failure: String? = null

                for (task in pending) {
                    val response = RetrofitInstance.api.updateHousekeepingTask(
                        planId = planId,
                        taskId = task.id,
                        body = mapOf("status" to "In Progress"),
                    )

                    if (!response.isSuccessful) {
                        failure = when (response.code()) {
                            422 -> "Upload foto before fasilitas terlebih dahulu."
                            else -> "Gagal memulai pekerjaan (${response.code()})"
                        }
                        break
                    }
                }

                if (failure != null) {
                    _errorMessage.value = failure
                } else {
                    _message.value = "Pekerjaan dimulai (In Progress)"
                }

                refreshAfterChange(planId)
            } catch (e: Exception) {
                _errorMessage.value = "Tidak dapat terhubung ke server"
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    /**
     * Selesaikan checklist yang dicentang sekaligus (tombol "Selesaikan").
     * Backend mewajibkan foto after fasilitas sebelum status Done.
     */
    fun finishTasks(planId: Int, tasks: List<HousekeepingTaskItem>) {
        if (tasks.isEmpty()) {
            return
        }

        viewModelScope.launch {
            _isSubmitting.value = true
            _errorMessage.value = null

            try {
                var failure: String? = null

                for (task in tasks) {
                    val response = RetrofitInstance.api.updateHousekeepingTask(
                        planId = planId,
                        taskId = task.id,
                        body = mapOf("status" to "Done"),
                    )

                    if (!response.isSuccessful) {
                        failure = when (response.code()) {
                            422 -> "Upload foto after fasilitas terlebih dahulu."
                            else -> "Gagal menyelesaikan pekerjaan (${response.code()})"
                        }
                        break
                    }
                }

                if (failure != null) {
                    _errorMessage.value = failure
                } else {
                    _message.value = "Pekerjaan selesai (Done)"
                }

                refreshAfterChange(planId)
            } catch (e: Exception) {
                _errorMessage.value = "Tidak dapat terhubung ke server"
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun deleteFacilityPhoto(planId: Int, planFacility: HousekeepingPlanFacilityItem, type: String, path: String) {
        viewModelScope.launch {
            _isSubmitting.value = true
            _errorMessage.value = null

            try {
                val response = RetrofitInstance.api.deleteHousekeepingFacilityPhoto(
                    planId = planId,
                    planFacilityId = planFacility.id,
                    type = type,
                    path = path
                )

                if (response.isSuccessful) {
                    _message.value = "Foto berhasil dihapus"
                    refreshAfterChange(planId)
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

    private suspend fun refreshAfterChange(planId: Int) {
        runCatching {
            val refreshed = RetrofitInstance.api.getHousekeepingPlanDetail(planId)
            if (refreshed.isSuccessful) {
                _detail.value = refreshed.body()?.data
            }
        }
    }

    fun reset() {
        _detail.value = null
        _message.value = null
        _errorMessage.value = null
    }
}
