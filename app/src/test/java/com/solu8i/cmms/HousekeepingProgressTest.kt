package com.solu8i.cmms

import com.solu8i.cmms.ui.util.HousekeepingProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HousekeepingProgressTest {

    @Test
    fun usesServerProgressWhenAvailable() {
        assertEquals(50, HousekeepingProgress.percent(50, 1, 2))
        assertEquals(100, HousekeepingProgress.percent(100, 2, 2))
    }

    @Test
    fun fallsBackToTaskCountsWhenProgressMissing() {
        assertEquals(50, HousekeepingProgress.percent(null, 1, 2))
        assertEquals(100, HousekeepingProgress.percent(null, 3, 3))
        assertEquals(0, HousekeepingProgress.percent(null, 0, 0))
        assertEquals(0, HousekeepingProgress.percent(null, null, null))
    }

    @Test
    fun clampsOutOfRangeProgress() {
        assertEquals(100, HousekeepingProgress.percent(120, 0, 0))
        assertEquals(0, HousekeepingProgress.percent(-5, 0, 0))
    }

    @Test
    fun formatsChecklistAndSla() {
        assertEquals("1/2", HousekeepingProgress.checklistLabel(1, 2))
        assertEquals("0/0", HousekeepingProgress.checklistLabel(null, null))
        assertEquals("SLA 1 hari", HousekeepingProgress.slaLabel(1))
        assertEquals("SLA 14 hari", HousekeepingProgress.slaLabel(14))
        assertEquals("SLA 1 hari", HousekeepingProgress.slaLabel(null))
        assertEquals("SLA 1 hari", HousekeepingProgress.slaLabel(0))
    }

    @Test
    fun formatsDueAndSlaStatus() {
        assertEquals("Batas: 2026-11-19", HousekeepingProgress.dueLabel("2026-11-19"))
        assertEquals(null, HousekeepingProgress.dueLabel(null))
        assertEquals(null, HousekeepingProgress.dueLabel("  "))

        assertEquals("Selesai", HousekeepingProgress.slaStatusLabel("Completed"))
        assertEquals("Melewati SLA", HousekeepingProgress.slaStatusLabel("Overdue"))
        assertEquals("Mendekati SLA", HousekeepingProgress.slaStatusLabel("At Risk"))
        assertEquals("On Track", HousekeepingProgress.slaStatusLabel("On Track"))
        assertEquals("On Track", HousekeepingProgress.slaStatusLabel(null))
    }

    @Test
    fun taskRequiresFacilityBeforePhotoToStart() {
        assertFalse(HousekeepingProgress.canStartTask("Pending", false))
        assertTrue(HousekeepingProgress.canStartTask("Pending", true))
        assertFalse(HousekeepingProgress.canStartTask("Done", true))
        assertFalse(HousekeepingProgress.canStartTask("In Progress", true))
    }

    @Test
    fun checkboxOnlyActiveForInProgressTasks() {
        assertFalse(HousekeepingProgress.canCheckTask("Pending"))
        assertTrue(HousekeepingProgress.canCheckTask("In Progress"))
        assertFalse(HousekeepingProgress.canCheckTask("Done"))
    }

    @Test
    fun startAllRequiresBeforePhotoAndPendingTasks() {
        assertTrue(HousekeepingProgress.canStartAll(hasBeforePhoto = true, hasPending = true, isSubmitting = false))
        assertFalse(HousekeepingProgress.canStartAll(hasBeforePhoto = false, hasPending = true, isSubmitting = false))
        assertFalse(HousekeepingProgress.canStartAll(hasBeforePhoto = true, hasPending = false, isSubmitting = false))
        assertFalse(HousekeepingProgress.canStartAll(hasBeforePhoto = true, hasPending = true, isSubmitting = true))
    }

    @Test
    fun finishAllRequiresAfterPhotoAndCheckedTasks() {
        assertTrue(HousekeepingProgress.canFinishAll(hasAfterPhoto = true, checkedCount = 1, isSubmitting = false))
        assertFalse(HousekeepingProgress.canFinishAll(hasAfterPhoto = false, checkedCount = 1, isSubmitting = false))
        assertFalse(HousekeepingProgress.canFinishAll(hasAfterPhoto = true, checkedCount = 0, isSubmitting = false))
        assertFalse(HousekeepingProgress.canFinishAll(hasAfterPhoto = true, checkedCount = 2, isSubmitting = true))
    }

    @Test
    fun detectsOverdue() {
        assertTrue(HousekeepingProgress.isOverdue(true, null))
        assertTrue(HousekeepingProgress.isOverdue(null, "Overdue"))
        assertFalse(HousekeepingProgress.isOverdue(false, "On Track"))
        assertFalse(HousekeepingProgress.isOverdue(null, null))
    }
}
