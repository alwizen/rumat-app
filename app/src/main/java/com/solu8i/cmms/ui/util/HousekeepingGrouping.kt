package com.solu8i.cmms.ui.util

import com.solu8i.cmms.data.model.HousekeepingTaskItem

/**
 * Satu kelompok checklist untuk satu fasilitas pada plan.
 * Foto before/after menempel pada tiap item checklist (`HousekeepingTaskItem`),
 * bukan pada kelompok fasilitas.
 */
data class HousekeepingFacilityTaskGroup(
    val name: String,
    val tasks: List<HousekeepingTaskItem>
) {
    val key: String = "facility-${tasks.firstOrNull()?.facility?.id ?: name}"
}

object HousekeepingGrouping {

    /**
     * Kelompokkan checklist per fasilitas menjadi satu daftar card.
     * Item Done selalu diletakkan di bawah agar pekerjaan berikutnya tetap
     * terlihat. Checklist tanpa fasilitas dikembalikan terpisah lewat
     * [looseTasks].
     */
    fun group(tasks: List<HousekeepingTaskItem>): Result {
        val sorted = tasks.sortedBy { if (it.status == "Done") 1 else 0 }

        val groups = sorted
            .filter { it.facility?.id != null }
            .groupBy { it.facility?.id }
            .map { (_, facilityTasks) ->
                HousekeepingFacilityTaskGroup(
                    name = facilityTasks.firstOrNull()?.facility?.name ?: "Fasilitas",
                    tasks = facilityTasks,
                )
            }

        return Result(
            groups = groups,
            looseTasks = sorted.filter { it.facility?.id == null },
        )
    }

    data class Result(
        val groups: List<HousekeepingFacilityTaskGroup>,
        val looseTasks: List<HousekeepingTaskItem>,
    )
}
