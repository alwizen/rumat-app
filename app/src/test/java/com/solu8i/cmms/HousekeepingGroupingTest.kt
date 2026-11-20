package com.solu8i.cmms

import com.solu8i.cmms.data.model.HousekeepingFacilityBrief
import com.solu8i.cmms.data.model.HousekeepingPlanFacilityItem
import com.solu8i.cmms.data.model.HousekeepingTaskItem
import com.solu8i.cmms.ui.util.HousekeepingGrouping
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HousekeepingGroupingTest {

    private fun facility(id: Int, name: String) = HousekeepingFacilityBrief(id, name, "F-$id")

    private fun task(
        id: Int,
        name: String,
        status: String = "Pending",
        facility: HousekeepingFacilityBrief? = null,
    ) = HousekeepingTaskItem(
        id = id,
        name = name,
        status = status,
        note = null,
        startedAt = null,
        completedAt = null,
        facility = facility,
        masterTask = null,
    )

    private fun planFacility(id: Int, facilityId: Int, name: String) =
        HousekeepingPlanFacilityItem(
            id = id,
            facility = facility(facilityId, name),
            photoBefore = emptyList(),
            photoAfter = emptyList(),
        )

    @Test
    fun groupsTasksAndDocumentationPerFacility() {
        val gazebo = facility(1, "Gazebo")
        val taman = facility(2, "Taman")

        val result = HousekeepingGrouping.group(
            tasks = listOf(
                task(1, "Sapu Gazebo", "Done", gazebo),
                task(2, "Bersih Bangku", "Pending", gazebo),
                task(3, "Potong Rumput", "In Progress", taman),
            ),
            facilities = listOf(
                planFacility(11, 1, "Gazebo"),
                planFacility(12, 2, "Taman"),
            ),
        )

        assertEquals(2, result.groups.size)
        assertTrue(result.looseTasks.isEmpty())

        val gazeboGroup = result.groups.first { it.name == "Gazebo" }
        assertEquals(11, gazeboGroup.planFacility?.id)
        assertEquals(listOf("Bersih Bangku", "Sapu Gazebo"), gazeboGroup.tasks.map { it.name })
        assertEquals("plan-facility-11", gazeboGroup.key)

        val tamanGroup = result.groups.first { it.name == "Taman" }
        assertEquals(listOf("Potong Rumput"), tamanGroup.tasks.map { it.name })
    }

    @Test
    fun keepsFacilityWithoutChecklistAsEmptyGroup() {
        val result = HousekeepingGrouping.group(
            tasks = emptyList(),
            facilities = listOf(planFacility(11, 1, "Gazebo")),
        )

        assertEquals(1, result.groups.size)
        assertEquals("Gazebo", result.groups.single().name)
        assertTrue(result.groups.single().tasks.isEmpty())
    }

    @Test
    fun addsGroupForFacilityNotYetDocumented() {
        val result = HousekeepingGrouping.group(
            tasks = listOf(task(1, "Sapu", "Pending", facility(3, "Selokan"))),
            facilities = emptyList(),
        )

        assertEquals(1, result.groups.size)
        assertNull(result.groups.single().planFacility)
        assertEquals("Selokan", result.groups.single().name)
        assertEquals(listOf("Sapu"), result.groups.single().tasks.map { it.name })
        assertEquals("facility-3", result.groups.single().key)
    }

    @Test
    fun separatesTasksWithoutFacility() {
        val result = HousekeepingGrouping.group(
            tasks = listOf(
                task(1, "Isi Air Minum"),
                task(2, "Sapu Gazebo", "Done", facility(1, "Gazebo")),
            ),
            facilities = listOf(planFacility(11, 1, "Gazebo")),
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
            facilities = listOf(planFacility(11, 1, "Gazebo")),
        )

        assertEquals(
            listOf("B Pending", "D In Progress", "A Done", "C Done"),
            result.groups.single().tasks.map { it.name },
        )
    }
}
