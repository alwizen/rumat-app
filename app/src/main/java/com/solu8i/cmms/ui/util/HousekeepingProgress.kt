package com.solu8i.cmms.ui.util

/**
 * Helper tampilan progres & SLA plan housekeeping.
 *
 * Progres TIDAK diisi manual: backend menghitung `progress_percent`
 * dari jumlah item checklist Done dibagi total item. Helper di sini hanya
 * memformat nilai tersebut (plus fallback dari task_count/task_done_count
 * untuk respons lama yang belum punya `progress_percent`).
 */
object HousekeepingProgress {

    /**
     * Ambil persentase progres (0..100). Bila `progressPercent` null,
     * hitung dari jumlah checklist; bila total tidak diketahui hasilnya 0.
     */
    fun percent(progressPercent: Int?, taskDoneCount: Int?, taskCount: Int?): Int {
        val percent = progressPercent
            ?: if ((taskCount ?: 0) > 0) {
                (((taskDoneCount ?: 0).toDouble() / taskCount!!.toDouble()) * 100).toInt()
            } else {
                0
            }

        return percent.coerceIn(0, 100)
    }

    fun progressLabel(progressPercent: Int?, taskDoneCount: Int?, taskCount: Int?): String =
        "${percent(progressPercent, taskDoneCount, taskCount)}%"

    fun checklistLabel(taskDoneCount: Int?, taskCount: Int?): String =
        "${taskDoneCount ?: 0}/${taskCount ?: 0}"

    /**
     * Label SLA, mis. "SLA 1 hari" atau "SLA 14 hari".
     */
    fun slaLabel(slaDays: Int?): String = "SLA ${(slaDays ?: 1).coerceAtLeast(1)} hari"

    /**
     * Label batas waktu pengerjaan, mis. "Batas: 2026-11-19".
     */
    fun dueLabel(dueDate: String?): String? =
        dueDate?.takeIf { it.isNotBlank() }?.let { "Batas: $it" }

    /**
     * Status SLA yang ditampilkan ke pengguna.
     * Nilai tak dikenal / kosong dianggap "On Track".
     */
    fun slaStatusLabel(slaStatus: String?): String = when (slaStatus?.trim()) {
        "Completed" -> "Selesai"
        "Overdue" -> "Melewati SLA"
        "At Risk" -> "Mendekati SLA"
        "On Track" -> "On Track"
        else -> "On Track"
    }

    fun isOverdue(isOverdue: Boolean?, slaStatus: String?): Boolean =
        isOverdue == true || slaStatus == "Overdue"

    /**
     * Item checklist hanya boleh dimulai setelah foto before fasilitas ada.
     * Item tanpa fasilitas tidak dibatasi (backend juga tidak memblokir).
     */
    fun canStartTask(status: String, hasBeforePhoto: Boolean): Boolean =
        status == "Pending" && hasBeforePhoto

    /**
     * Checkbox hanya bisa dicentang setelah pekerjaan dimulai (In Progress).
     * Status Done sudah final di aplikasi dan dipindahkan ke bawah daftar.
     */
    fun canCheckTask(status: String): Boolean = status == "In Progress"

    /**
     * Tombol "Mulai Kerjakan" per card memerlukan foto before fasilitas
     * dan minimal satu checklist yang masih Pending.
     */
    fun canStartAll(hasBeforePhoto: Boolean, hasPending: Boolean, isSubmitting: Boolean): Boolean =
        ! isSubmitting && hasBeforePhoto && hasPending

    /**
     * Tombol "Selesaikan" baru aktif setelah foto after fasilitas diunggah
     * dan ada checklist yang dicentang.
     */
    fun canFinishAll(hasAfterPhoto: Boolean, checkedCount: Int, isSubmitting: Boolean): Boolean =
        ! isSubmitting && hasAfterPhoto && checkedCount > 0
}
