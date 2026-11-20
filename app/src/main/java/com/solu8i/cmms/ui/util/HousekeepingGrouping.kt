package com.solu8i.cmms.ui.util

import com.solu8i.cmms.data.model.HousekeepingPlanFacilityItem
import com.solu8i.cmms.data.model.HousekeepingTaskItem

/**
 * Satu kelompok pekerjaan + dokumentasi untuk satu fasilitas pada plan.
 * `planFacility` null berarti fasilitas hanya disebut checklist tanpa
 * baris dokumentasi foto.
 */
data class HousekeepingFacilityTaskGroup(
    val planFacility: HousekeepingPlanFacilityItem?,
    val name: String,
    val tasks: List<HousekeepingTaskItem>
) {
    val key: String = planFacility?.id?.let { "plan-facility-$it" }
        ?: "facility-${tasks.firstOrNull()?.facility?.id ?: name}"
}

object HousekeepingGrouping {

    /**
     * Gabungkan checklist dan dokumentasi foto per fasilitas menjadi satu
     * daftar card. Item Done selalu diletakkan di bawah agar pekerjaan
     * berikutnya tetap terlihat.
     *
     * Checklist tanpa fasilitas dikembalikan terpisah lewat [looseTasks].
     */
    fun group(
        tasks: List<HousekeepingTaskItem>,
        facilities: List<HousekeepingPlanFacilityItem>,
    ): Result {
        val sorted = tasks.sortedBy { if (it.status == "Done") 1 else 0 }

        val tasksByFacility = sorted
            .filter { it.facility?.id != null }
            .groupBy { it.facility?.id }

        val documentedFacilityIds = facilities.mapNotNull { it.facility?.id }.toSet()

        val groups = buildList {
            facilities.forEach { planFacility ->
                val facilityId = planFacility.facility?.id

                add(
                    HousekeepingFacilityTaskGroup(
                        planFacility = planFacility,
                        name = planFacility.facility?.name ?: "Fasilitas",
                        tasks = tasksByFacility[facilityId].orEmpty(),
                    )
                )
            }

            tasksByFacility
                .filterKeys { it != null && it !in documentedFacilityIds }
                .forEach { (_, facilityTasks) ->
                    add(
                        HousekeepingFacilityTaskGroup(
                            planFacility = null,
                            name = facilityTasks.firstOrNull()?.facility?.name ?: "Fasilitas",
                            tasks = facilityTasks,
                        )
                    )
                }
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
