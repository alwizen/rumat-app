package com.solu8i.cmms.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.solu8i.cmms.data.model.HousekeepingPlanItem
import com.solu8i.cmms.data.remote.RetrofitInstance
import com.solu8i.cmms.ui.util.HousekeepingReport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

/**
 * Daftar Plan Harian housekeeping.
 *
 * Plan dibuat oleh admin melalui web, jadi aplikasi hanya menampilkan daftar
 * beserta progres dan status SLA yang sudah dihitung server.
 */
class HousekeepingViewModel(application: Application) : AndroidViewModel(application) {

    private val _plans = MutableStateFlow<List<HousekeepingPlanItem>>(emptyList())
    val plans: StateFlow<List<HousekeepingPlanItem>> = _plans

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isDownloadingReport = MutableStateFlow(false)
    val isDownloadingReport: StateFlow<Boolean> = _isDownloadingReport

    /** Error khusus proses generate laporan (terpisah dari error daftar plan). */
    private val _reportError = MutableStateFlow<String?>(null)
    val reportError: StateFlow<String?> = _reportError

    /** Pesan sukses proses generate laporan. */
    private val _reportMessage = MutableStateFlow<String?>(null)
    val reportMessage: StateFlow<String?> = _reportMessage

    private var loaded = false

    fun loadPlans(force: Boolean = false) {
        if (loaded && !force) {
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val response = RetrofitInstance.api.getHousekeepingPlans(perPage = 100)
                if (response.isSuccessful) {
                    _plans.value = response.body()?.data ?: emptyList()
                    loaded = true
                } else {
                    _errorMessage.value = "Gagal memuat plan (${response.code()})"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Tidak dapat terhubung ke server"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Unduh laporan harian housekeeping (PDF) lalu simpan ke cache aplikasi.
     * Mengembalikan file lewat callback agar UI bisa langsung membukanya.
     */
    fun downloadDailyReport(date: String, onSaved: (File) -> Unit) {
        if (_isDownloadingReport.value) {
            return
        }

        viewModelScope.launch {
            _isDownloadingReport.value = true
            _reportError.value = null
            _reportMessage.value = null

            try {
                val response = RetrofitInstance.api.downloadDailyHousekeepingReport(date)

                if (!response.isSuccessful) {
                    _reportError.value = when (response.code()) {
                        403 -> "Anda tidak berhak mengunduh laporan ini."
                        422 -> HousekeepingReport.blockedMessage(
                            response.errorBody()?.string(),
                        )
                        else -> "Gagal mengunduh laporan (${response.code()})"
                    }

                    return@launch
                }

                val body = response.body()

                if (body == null) {
                    _reportError.value = "Laporan kosong."

                    return@launch
                }

                val directory = File(getApplication<Application>().cacheDir, "reports").apply { mkdirs() }

                // Nama unik + bersihkan laporan lama: penampil PDF menyimpan
                // cache berdasarkan nama file sehingga laporan baru bisa
                // menampilkan versi lama (tanpa foto).
                directory.listFiles()
                    ?.filter { it.name.startsWith("laporan-housekeeping-") }
                    ?.forEach { it.delete() }

                val file = File(directory, HousekeepingReport.uniqueFileName(date, System.currentTimeMillis()))

                body.byteStream().use { input ->
                    file.outputStream().use { output -> input.copyTo(output) }
                }

                if (!HousekeepingReport.isPdfContent(file.readBytes())) {
                    file.delete()
                    _reportError.value = "Laporan yang diunduh tidak valid."

                    return@launch
                }

                _reportMessage.value = "Laporan harian berhasil dibuat."
                onSaved(file)
            } catch (e: Exception) {
                _reportError.value = "Tidak dapat terhubung ke server"
            } finally {
                _isDownloadingReport.value = false
            }
        }
    }

    fun consumeReportError() {
        _reportError.value = null
    }

    fun consumeReportMessage() {
        _reportMessage.value = null
    }

    fun consumeError() {
        _errorMessage.value = null
    }

    fun refresh() = loadPlans(force = true)

    fun reset() {
        _plans.value = emptyList()
        _errorMessage.value = null
        _isDownloadingReport.value = false
        _reportError.value = null
        _reportMessage.value = null
        loaded = false
    }
}
