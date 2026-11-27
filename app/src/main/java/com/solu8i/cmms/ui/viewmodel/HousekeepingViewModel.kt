package com.solu8i.cmms.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.solu8i.cmms.data.model.HousekeepingAreaSimple
import com.solu8i.cmms.data.model.HousekeepingPlanItem
import com.solu8i.cmms.data.remote.RetrofitInstance
import com.solu8i.cmms.ui.util.HousekeepingReport
import kotlinx.coroutines.Job
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

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore

    private val _hasMorePlans = MutableStateFlow(false)
    val hasMorePlans: StateFlow<Boolean> = _hasMorePlans

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _selectedStatus = MutableStateFlow("All")
    val selectedStatus: StateFlow<String> = _selectedStatus

    private val _selectedAreaId = MutableStateFlow<Int?>(null)
    val selectedAreaId: StateFlow<Int?> = _selectedAreaId

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _dateFrom = MutableStateFlow("")
    val dateFrom: StateFlow<String> = _dateFrom

    private val _dateTo = MutableStateFlow("")
    val dateTo: StateFlow<String> = _dateTo

    private val _areas = MutableStateFlow<List<HousekeepingAreaSimple>>(emptyList())
    val areas: StateFlow<List<HousekeepingAreaSimple>> = _areas

    private var statusFilter: String? = null
    private var areaFilter: Int? = null
    private var dateFromFilter: String? = null
    private var dateToFilter: String? = null
    private var searchFilter: String? = null

    private var searchJob: Job? = null

    private var page = 1
    private var lastPage = 1
    private val perPage = 30

    private val _isDownloadingReport = MutableStateFlow(false)
    val isDownloadingReport: StateFlow<Boolean> = _isDownloadingReport

    /** Error khusus proses generate laporan (terpisah dari error daftar plan). */
    private val _reportError = MutableStateFlow<String?>(null)
    val reportError: StateFlow<String?> = _reportError

    /** Pesan sukses proses generate laporan. */
    private val _reportMessage = MutableStateFlow<String?>(null)
    val reportMessage: StateFlow<String?> = _reportMessage

    private var loaded = false

    fun onQueryChanged(value: String) {
        _query.value = value
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            kotlinx.coroutines.delay(350)
            loadPlans(force = true)
        }
    }

    fun applyFilters(status: String, areaId: Int?, dateFrom: String, dateTo: String) {
        _selectedStatus.value = status
        _selectedAreaId.value = areaId
        _dateFrom.value = dateFrom
        _dateTo.value = dateTo
        loadPlans(force = true)
    }

    fun resetFilters() {
        _selectedStatus.value = "All"
        _selectedAreaId.value = null
        _dateFrom.value = ""
        _dateTo.value = ""
        _query.value = ""
        loadPlans(force = true)
    }

    /**
     * Muat daftar area housekeeping untuk pilihan filter.
     */
    fun loadAreas() {
        if (_areas.value.isNotEmpty()) {
            return
        }

        viewModelScope.launch {
            runCatching {
                val response = RetrofitInstance.api.getHousekeepingAreas()
                if (response.isSuccessful) {
                    _areas.value = response.body()?.data ?: emptyList()
                }
            }
        }
    }

    fun loadPlans(force: Boolean = false) {
        if (loaded && !force) {
            return
        }

        statusFilter = _selectedStatus.value.takeIf { it != "All" }
        areaFilter = _selectedAreaId.value
        dateFromFilter = _dateFrom.value.takeIf { it.isNotBlank() }
        dateToFilter = _dateTo.value.takeIf { it.isNotBlank() }
        searchFilter = _query.value.takeIf { it.isNotBlank() }

        fetchPlans(page = 1)
    }

    /**
     * Muat halaman berikutnya (infinite scroll).
     */
    fun loadMorePlans() {
        if (_isLoading.value || _isLoadingMore.value || !_hasMorePlans.value) {
            return
        }

        fetchPlans(page = page + 1, append = true)
    }

    private fun fetchPlans(page: Int, append: Boolean = false) {
        viewModelScope.launch {
            if (append) _isLoadingMore.value = true else _isLoading.value = true
            _errorMessage.value = null

            try {
                val response = RetrofitInstance.api.getHousekeepingPlans(
                    status = statusFilter,
                    areaId = areaFilter,
                    dateFrom = dateFromFilter,
                    dateTo = dateToFilter,
                    search = searchFilter,
                    perPage = perPage,
                    page = page,
                )
                if (response.isSuccessful) {
                    val body = response.body()
                    val items = body?.data ?: emptyList()
                    _plans.value = if (append) {
                        _plans.value + items.filter { item -> _plans.value.none { it.id == item.id } }
                    } else {
                        items
                    }
                    lastPage = body?.meta?.lastPage ?: 1
                    this@HousekeepingViewModel.page = body?.meta?.currentPage ?: page
                    _hasMorePlans.value = this@HousekeepingViewModel.page < lastPage
                    loaded = true
                } else {
                    _errorMessage.value = "Gagal memuat plan (${response.code()})"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Tidak dapat terhubung ke server"
            } finally {
                if (append) _isLoadingMore.value = false else _isLoading.value = false
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
        _areas.value = emptyList()
        _errorMessage.value = null
        _selectedStatus.value = "All"
        _selectedAreaId.value = null
        _query.value = ""
        _dateFrom.value = ""
        _dateTo.value = ""
        statusFilter = null
        areaFilter = null
        dateFromFilter = null
        dateToFilter = null
        searchFilter = null
        page = 1
        lastPage = 1
        _hasMorePlans.value = false
        _isLoadingMore.value = false
        _isDownloadingReport.value = false
        _reportError.value = null
        _reportMessage.value = null
        loaded = false
    }
}
