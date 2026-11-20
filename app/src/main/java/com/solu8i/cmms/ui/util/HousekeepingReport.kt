package com.solu8i.cmms.ui.util

import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Helper untuk laporan harian housekeeping yang diunduh dari aplikasi.
 * Backend memakai format tanggal `yyyy-MM-dd`.
 */
object HousekeepingReport {

    const val DATE_PATTERN = "yyyy-MM-dd"

    /**
     * Material3 DatePicker mengembalikan millis pada UTC midnight, jadi
     * konversi juga harus memakai UTC agar tanggal tidak bergeser satu hari
     * pada perangkat dengan zona waktu negatif.
     */
    fun dateParam(millis: Long): String =
        SimpleDateFormat(DATE_PATTERN, Locale.US)
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
            .format(Date(millis))

    /**
     * Material3 DatePicker memakai UTC midnight. Bila `initialSelectedDateMillis`
     * diisi `System.currentTimeMillis()`, pada dini hari zona waktu tertentu
     * tanggal yang tampil bisa bergeser. Helper ini menghasilkan UTC midnight
     * untuk tanggal lokal hari ini.
     */
    fun todayUtcMillis(): Long {
        val local = Calendar.getInstance()
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(
                local.get(Calendar.YEAR),
                local.get(Calendar.MONTH),
                local.get(Calendar.DAY_OF_MONTH),
                0,
                0,
                0,
            )
        }

        return utc.timeInMillis
    }

    fun fileName(date: String): String =
        "laporan-housekeeping-${date}.pdf"

    /**
     * Nama file yang aman dipakai di cache: ganti karakter di luar
     * angka/huruf/strip dengan strip.
     */
    fun sanitizedFileName(date: String): String =
        fileName(date.replace(Regex("[^0-9A-Za-z-]"), "-"))

    /**
     * Nama file unik per unduhan. Aplikasi penampil PDF (Chrome/Drive/MuPDF)
     * menyimpan cache berdasarkan nama file, sehingga laporan baru dengan nama
     * sama bisa menampilkan versi lama yang belum ada fotonya.
     */
    fun uniqueFileName(date: String, timestamp: Long): String =
        fileName(date.replace(Regex("[^0-9A-Za-z-]"), "-"))
            .removeSuffix(".pdf") + "-" + timestamp + ".pdf"

    /**
     * Pastikan unduhan benar-benar PDF (server bisa mengembalikan JSON saat
     * terjadi kesalahan).
     */
    fun isPdfContent(bytes: ByteArray): Boolean {
        if (bytes.size <= 4) {
            return false
        }

        return String(bytes, 0, 4, Charsets.US_ASCII) == "%PDF"
    }

    /**
     * Pesan penolakan 422 dari server saat laporan belum bisa digenerate
     * karena hasil pekerjaan belum diverifikasi SPV GOS.
     */
    fun blockedMessage(errorBody: String?): String {
        val fallback = "Laporan belum bisa digenerate. Pastikan pekerjaan sudah direview dan disetujui SPV GOS."

        if (errorBody.isNullOrBlank()) {
            return fallback
        }

        val message = runCatching {
            Gson().fromJson(errorBody, Map::class.java)["message"] as? String
        }.getOrNull()

        return message?.takeIf { it.isNotBlank() } ?: fallback
    }

    /**
     * Label status verifikasi hasil pekerjaan oleh SPV GOS.
     */
    fun approvalLabel(approvalStatus: String?): String = when (approvalStatus?.trim()) {
        "Approved" -> "Disetujui SPV GOS"
        "Rejected" -> "Ditolak SPV GOS"
        "Pending" -> "Menunggu review SPV GOS"
        else -> "Menunggu review SPV GOS"
    }

    fun isApproved(approvalStatus: String?): Boolean = approvalStatus == "Approved"

    fun isRejected(approvalStatus: String?): Boolean = approvalStatus == "Rejected"

    /**
     * Label verifikasi yang memperhitungkan status pekerjaan: selama pekerjaan
     * belum selesai, SPV GOS memang belum perlu memverifikasi.
     */
    fun verificationLabel(status: String?, approvalStatus: String?): String =
        if (!isWorkCompleted(status) && !isApproved(approvalStatus) && !isRejected(approvalStatus)) {
            "Belum perlu review"
        } else {
            approvalLabel(approvalStatus)
        }

    fun isWorkCompleted(status: String?): Boolean = status == "Completed"
}
