package com.solu8i.cmms

import com.solu8i.cmms.data.model.HousekeepingFacilityBrief
import com.solu8i.cmms.data.model.HousekeepingTaskItem
import com.solu8i.cmms.ui.util.HousekeepingGrouping
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HousekeepingGroupingTest {

    private fun facility(id: Int, name: String) = HousekeepingFacilityBrief(id, name, "F-$id")

    private fun task(
        id: Int,
        name: String,
        status: String = "Pending",
        facility: HousekeepingFacilityBrief? = null,
        photoBefore: List<String>? = null,
        photoAfter: List<String>? = null,
    ) = HousekeepingTaskItem(
        id = id,
        name = name,
        status = status,
        note = null,
        photoBefore = photoBefore,
        photoAfter = photoAfter,
        startedAt = null,
        completedAt = null,
        facility = facility,
        masterTask = null,
    )

    @Test
    fun groupsTasksPerFacility() {
        val gazebo = facility(1, "Gazebo")
        val taman = facility(2, "Taman")

        val result = HousekeepingGrouping.group(
            tasks = listOf(
                task(1, "Sapu Gazebo", "Done", gazebo),
                task(2, "Bersih Bangku", "Pending", gazebo),
                task(3, "Potong Rumput", "In Progress", taman),
            ),
        )

        assertEquals(2, result.groups.size)
        assertTrue(result.looseTasks.isEmpty())

        val gazeboGroup = result.groups.first { it.name == "Gazebo" }
        assertEquals(listOf("Bersih Bangku", "Sapu Gazebo"), gazeboGroup.tasks.map { it.name })
        assertEquals("facility-1", gazeboGroup.key)

        val tamanGroup = result.groups.first { it.name == "Taman" }
        assertEquals(listOf("Potong Rumput"), tamanGroup.tasks.map { it.name })
    }

    @Test
    fun keepsTaskPhotosOnEachItem() {
        val gazebo = facility(1, "Gazebo")

        val result = HousekeepingGrouping.group(
            tasks = listOf(
                task(
                    1,
                    "Sapu Gazebo",
                    "Done",
                    gazebo,
                    photoBefore = listOf("housekeeping/plan-tasks/before/a.jpg"),
                    photoAfter = listOf("housekeeping/plan-tasks/after/a.jpg"),
                ),
                task(2, "Bersih Bangku", "Pending", gazebo),
            ),
        )

        val tasks = result.groups.single().tasks
        val done = tasks.first { it.name == "Sapu Gazebo" }
        val pending = tasks.first { it.name == "Bersih Bangku" }

        assertEquals(listOf("housekeeping/plan-tasks/before/a.jpg"), done.photoBefore)
        assertEquals(listOf("housekeeping/plan-tasks/after/a.jpg"), done.photoAfter)
        assertTrue(pending.photoBefore.isNullOrEmpty())
        assertTrue(pending.photoAfter.isNullOrEmpty())
    }

    @Test
    fun separatesTasksWithoutFacility() {
        val result = HousekeepingGrouping.group(
            tasks = listOf(
                task(1, "Isi Air Minum"),
                task(2, "Sapu Gazebo", "Done", facility(1, "Gazebo")),
            ),
        )

        assertEquals(1, result.groups.size)
        assertEquals(listOf("Isi Air Minum"), result.looseTasks.map { it.name })
    }

    @Test
    fun completedTasksSortedLastWithinGroup() {
        val gazebo = facility(1, "Gazebo")

        val result = HousekeepingGrouping.group(
            tasks = listOf(
                task(1, "A Done", "Done", gazebo),
                task(2, "B Pending", "Pending", gazebo),
                task(3, "C Done", "Done", gazebo),
                task(4, "D In Progress", "In Progress", gazebo),
            ),
        )

        assertEquals(
            listOf("B Pending", "D In Progress", "A Done", "C Done"),
            result.groups.single().tasks.map { it.name },
        )
    }
}
