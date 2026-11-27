package com.solu8i.cmms.ui.util

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import java.io.File

/**
 * Dialog pilih tanggal laporan harian housekeeping.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HousekeepingReportDatePickerDialog(
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit,
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = HousekeepingReport.todayUtcMillis(),
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    // DatePicker menyimpan pilihan sebagai UTC midnight; bila
                    // kosong pakai hari ini (UTC) agar tidak bergeser tanggal.
                    val millis = state.selectedDateMillis ?: System.currentTimeMillis()
                    onDateSelected(HousekeepingReport.dateParam(millis))
                }
            ) {
                Text("Generate", color = Color(0xFF015E4E), fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = Color(0xFF6B7280))
            }
        },
    ) {
        DatePicker(state = state)
    }
}

/**
 * Buka PDF laporan hasil unduhan dari cache aplikasi.
 */
fun openPdfFile(context: Context, file: File) {
    try {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(
            context,
            "Laporan tersimpan, tetapi tidak ada aplikasi PDF untuk membukanya.",
            Toast.LENGTH_LONG,
        ).show()
    }
}

/**
 * Buka PDF laporan harian housekeeping.
 */
fun openHousekeepingReportPdf(context: Context, file: File) = openPdfFile(context, file)

/**
 * Indikator progres saat laporan harian sedang dibuat/diunduh, supaya user
 * tahu aplikasi sedang bekerja dan tidak menekan tombol berulang kali.
 */
@Composable
fun HousekeepingReportProgressDialog() {
    AlertDialog(
        onDismissRequest = { /* tidak bisa ditutup saat proses berjalan */ },
        title = { Text("Membuat laporan", fontSize = 15.sp, fontWeight = FontWeight.SemiBold) },
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color(0xFF015E4E),
                    strokeWidth = 2.dp,
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Mohon tunggu, laporan harian sedang diproses...",
                    fontSize = 13.sp,
                    color = Color(0xFF374151),
                )
            }
        },
        confirmButton = {},
    )
}

/**
 * Dialog hasil generate laporan: sukses atau gagal beserta alasannya,
 * sehingga user tahu kenapa laporan tidak bisa dibuat (mis. belum diverifikasi
 * SPV GOS, tidak punya hak akses, atau laporan tidak valid).
 */
@Composable
fun HousekeepingReportResultDialog(
    message: String,
    isError: Boolean,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isError) "Laporan gagal dibuat" else "Laporan siap",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isError) Color(0xFFB91C1C) else Color(0xFF015E4E),
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = message, fontSize = 13.sp, color = Color(0xFF374151))

                if (isError) {
                    Text(
                        text = "Periksa kembali apakah pekerjaan sudah selesai dan disetujui SPV GOS.",
                        fontSize = 11.sp,
                        color = Color(0xFF6B7280),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "OK",
                    color = if (isError) Color(0xFFB91C1C) else Color(0xFF015E4E),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        },
    )
}
