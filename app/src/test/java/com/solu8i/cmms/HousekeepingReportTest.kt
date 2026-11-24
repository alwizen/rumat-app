package com.solu8i.cmms

import com.solu8i.cmms.ui.util.HousekeepingReport
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class HousekeepingReportTest {

    @Test
    fun formatsDateParamAsIsoDate() {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2026, Calendar.NOVEMBER, 18, 12, 0, 0)
        }

        assertEquals("2026-11-18", HousekeepingReport.dateParam(calendar.timeInMillis))
    }

    @Test
    fun dateParamKeepsCalendarDateAcrossTimezones() {
        // DatePicker mengirim UTC midnight; konversi tidak boleh menggeser hari
        // pada perangkat dengan zona waktu negatif (mis. America/New_York).
        val utcMidnight = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(2026, Calendar.NOVEMBER, 18, 0, 0, 0)
        }.timeInMillis

        val original = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"))
            assertEquals("2026-11-18", HousekeepingReport.dateParam(utcMidnight))
        } finally {
            TimeZone.setDefault(original)
        }
    }

    @Test
    fun uniqueFileNameAvoidsStalePdfCache() {
        val first = HousekeepingReport.uniqueFileName("2026-11-19", 1_000L)
        val second = HousekeepingReport.uniqueFileName("2026-11-19", 2_000L)

        assertNotEquals(first, second)
        assertTrue(first.startsWith("laporan-housekeeping-2026-11-19-"))
        assertTrue(first.endsWith(".pdf"))
    }

    @Test
    fun validatesPdfContent() {
        assertTrue(HousekeepingReport.isPdfContent("%PDF-1.4\n...".toByteArray()))
        assertFalse(HousekeepingReport.isPdfContent("{\"success\":true}".toByteArray()))
        assertFalse(HousekeepingReport.isPdfContent(ByteArray(0)))
    }

    @Test
    fun buildsReportFileName() {
        assertEquals(
            "laporan-housekeeping-2026-11-18.pdf",
            HousekeepingReport.fileName("2026-11-18"),
        )
    }

    @Test
    fun parsesBlockedMessageFromServer() {
        assertEquals(
            "Laporan belum bisa digenerate: plan sudah selesai tetapi belum diverifikasi SPV GOS (HKP-001).",
            HousekeepingReport.blockedMessage(
                "{\"success\":false,\"message\":\"Laporan belum bisa digenerate: plan sudah selesai tetapi belum diverifikasi SPV GOS (HKP-001).\"}",
            ),
        )
    }

    @Test
    fun fallsBackWhenBlockedMessageMissing() {
        assertEquals(
            "Laporan belum bisa digenerate. Pastikan pekerjaan sudah direview dan disetujui SPV GOS.",
            HousekeepingReport.blockedMessage(null),
        )
        assertEquals(
            "Laporan belum bisa digenerate. Pastikan pekerjaan sudah direview dan disetujui SPV GOS.",
            HousekeepingReport.blockedMessage("not-json"),
        )
    }

    @Test
    fun labelsApprovalStatus() {
        assertEquals("Disetujui SPV GOS", HousekeepingReport.approvalLabel("Approved"))
        assertEquals("Ditolak SPV GOS", HousekeepingReport.approvalLabel("Rejected"))
        assertEquals("Menunggu review SPV GOS", HousekeepingReport.approvalLabel("Pending"))
        assertEquals("Menunggu review SPV GOS", HousekeepingReport.approvalLabel(null))

        assertTrue(HousekeepingReport.isApproved("Approved"))
        assertFalse(HousekeepingReport.isApproved("Pending"))
        assertTrue(HousekeepingReport.isRejected("Rejected"))
        assertFalse(HousekeepingReport.isRejected("Approved"))
    }

    @Test
    fun verificationLabelWaitsForCompletedWork() {
        assertEquals(
            "Belum perlu review",
            HousekeepingReport.verificationLabel("In Progress", "Pending"),
        )
        assertEquals(
            "Menunggu review SPV GOS",
            HousekeepingReport.verificationLabel("Completed", "Pending"),
        )
        assertEquals(
            "Disetujui SPV GOS",
            HousekeepingReport.verificationLabel("Completed", "Approved"),
        )
        assertEquals(
            "Ditolak SPV GOS",
            HousekeepingReport.verificationLabel("Completed", "Rejected"),
        )
    }

    @Test
    fun blockedMessageKeepsServerReasonForUser() {
        // Alasan dari server harus tampil apa adanya agar user tahu penyebabnya
        // (mis. pekerjaan belum disetujui SPV GOS).
        val reason = "Laporan belum bisa digenerate: plan belum direview/disetujui SPV GOS (HKP-001)."

        assertEquals(
            reason,
            HousekeepingReport.blockedMessage(
                "{\"success\":false,\"message\":\"$reason\"}",
            ),
        )
    }

    @Test
    fun sanitizesUnsafeCharactersInFileName() {
        assertEquals(
            "laporan-housekeeping-2026-11-18.pdf",
            HousekeepingReport.sanitizedFileName("2026/11/18"),
        )
        assertEquals(
            "laporan-housekeeping-2026-11-18.pdf",
            HousekeepingReport.sanitizedFileName("2026-11-18"),
        )
    }
}
