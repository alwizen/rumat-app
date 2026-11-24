package com.solu8i.cmms.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solu8i.cmms.data.model.HousekeepingActivePlan
import com.solu8i.cmms.data.model.HousekeepingDashboardStats
import com.solu8i.cmms.ui.util.HousekeepingProgress
import com.solu8i.cmms.ui.util.HousekeepingReport
import com.solu8i.cmms.ui.util.HousekeepingReportDatePickerDialog
import com.solu8i.cmms.ui.util.HousekeepingReportProgressDialog
import com.solu8i.cmms.ui.util.HousekeepingReportResultDialog
import com.solu8i.cmms.ui.util.openHousekeepingReportPdf
import com.solu8i.cmms.ui.viewmodel.HousekeepingDashboardViewModel
import com.solu8i.cmms.ui.viewmodel.HousekeepingViewModel

/**
 * Dashboard khusus user housekeeping (`patra-jasa`).
 *
 * Terpisah dari dashboard maintenance karena data yang relevan berbeda:
 * patra-jasa bekerja dengan plan harian, checklist, dan SLA area — bukan
 * work order.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HousekeepingDashboardScreen(
    dashboardViewModel: HousekeepingDashboardViewModel,
    housekeepingViewModel: HousekeepingViewModel,
    userName: String?,
    onPlanClick: (Int) -> Unit = {},
    onOpenPlans: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp,
) {
    val data by dashboardViewModel.data.collectAsState()
    val isLoading by dashboardViewModel.isLoading.collectAsState()
    val errorMessage by dashboardViewModel.errorMessage.collectAsState()
    val isDownloadingReport by housekeepingViewModel.isDownloadingReport.collectAsState()
    val reportError by housekeepingViewModel.reportError.collectAsState()
    val reportMessage by housekeepingViewModel.reportMessage.collectAsState()

    val context = LocalContext.current
    var showReportDatePicker by remember { mutableStateOf(false) }

    // Selalu muat ulang saat layar ini dibuka kembali (mis. kembali dari detail
    // setelah checklist diselesaikan) agar progres & statistik tidak tertinggal.
    LaunchedEffect(Unit) {
        dashboardViewModel.refresh()
    }

    errorMessage?.let { text ->
        AlertDialog(
            onDismissRequest = { dashboardViewModel.consumeError() },
            confirmButton = {
                TextButton(onClick = { dashboardViewModel.consumeError() }) {
                    Text("OK", color = Color(0xFFDC2626))
                }
            },
            title = { Text("Gagal") },
            text = { Text(text) },
        )
    }

    // Progress generate laporan: user tahu aplikasi sedang bekerja.
    if (isDownloadingReport) {
        HousekeepingReportProgressDialog()
    }

    // Hasil generate laporan (sukses/gagal) disampaikan ke user.
    (reportError ?: reportMessage)?.let { text ->
        HousekeepingReportResultDialog(
            message = text,
            isError = reportError != null,
            onDismiss = {
                housekeepingViewModel.consumeReportError()
                housekeepingViewModel.consumeReportMessage()
            },
        )
    }

    if (showReportDatePicker) {
        HousekeepingReportDatePickerDialog(
            onDismiss = { showReportDatePicker = false },
            onDateSelected = { date ->
                showReportDatePicker = false
                housekeepingViewModel.downloadDailyReport(date) { file ->
                    openHousekeepingReportPdf(context, file)
                }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Buka menu", tint = Color.White)
                    }
                },
                title = { Text("Housekeeping", fontWeight = FontWeight.Bold) },
                actions = {
                    if (isDownloadingReport) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        IconButton(onClick = { showReportDatePicker = true }) {
                            Icon(
                                Icons.Default.Description,
                                contentDescription = "Generate laporan harian",
                                tint = Color.White,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF015E4E),
                    titleContentColor = Color.White,
                ),
            )
        },
    ) { innerPadding ->
        when {
            isLoading && data == null -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Color(0xFF015E4E))
            }

            data == null -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text("Gagal memuat dashboard", color = Color(0xFF9CA3AF), fontSize = 13.sp)
            }

            else -> {
                val dashboard = data!!
                val stats = dashboard.stats
                val activePlans = dashboard.activePlans.orEmpty()

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF5F6FA))
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = bottomPadding + 16.dp),
                ) {
                    item { HousekeepingSummaryCard(userName = userName, stats = stats) }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            HousekeepingStatChip(
                                modifier = Modifier.weight(1f),
                                label = "Aktif",
                                value = stats.plansActive,
                                tint = Color(0xFF2563EB),
                                icon = Icons.Default.HourglassEmpty,
                            )
                            HousekeepingStatChip(
                                modifier = Modifier.weight(1f),
                                label = "Lewat SLA",
                                value = stats.plansOverdue,
                                tint = Color(0xFFDC2626),
                                icon = Icons.Default.Warning,
                            )
                            HousekeepingStatChip(
                                modifier = Modifier.weight(1f),
                                label = "Selesai",
                                value = stats.plansCompletedToday,
                                tint = Color(0xFF16A34A),
                                icon = Icons.Default.CleaningServices,
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            HousekeepingMenuTile(
                                modifier = Modifier.weight(1f),
                                label = "Daftar Plan",
                                icon = Icons.Default.CleaningServices,
                                onClick = onOpenPlans,
                            )
                            HousekeepingMenuTile(
                                modifier = Modifier.weight(1f),
                                label = "Laporan",
                                icon = Icons.Default.Description,
                                onClick = { showReportDatePicker = true },
                            )
                            HousekeepingMenuTile(
                                modifier = Modifier.weight(1f),
                                label = "Profil",
                                icon = Icons.Default.Person,
                                onClick = onOpenProfile,
                            )
                        }
                    }

                    item {
                        Text(
                            text = "Plan Berjalan",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF015E4E),
                        )
                    }

                    if (activePlans.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                            ) {
                                Text(
                                    text = "Tidak ada plan yang berjalan.",
                                    modifier = Modifier.padding(20.dp),
                                    color = Color(0xFF9CA3AF),
                                    fontSize = 13.sp,
                                )
                            }
                        }
                    } else {
                        items(activePlans, key = { it.id }) { plan ->
                            HousekeepingActivePlanCard(
                                plan = plan,
                                onClick = { onPlanClick(plan.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HousekeepingSummaryCard(userName: String?, stats: HousekeepingDashboardStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(listOf(Color(0xFF015E4E), Color(0xFF0B7A63)))
                )
                .padding(18.dp),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (userName.isNullOrBlank()) "Selamat bekerja" else "Halo, $userName",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "${stats.completionRate}%",
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                )

                Text(
                    text = "progres pekerjaan aktif",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                )

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { stats.completionRate.coerceIn(0, 100) / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.28f),
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "${stats.tasksDone}/${stats.tasksTotal} checklist selesai • " +
                        "${stats.tasksOpen} belum selesai",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 11.sp,
                )
            }
        }
    }
}

@Composable
private fun HousekeepingStatChip(
    label: String,
    value: Int,
    tint: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827),
            )
            Text(text = label, fontSize = 11.sp, color = Color(0xFF6B7280))
        }
    }
}

@Composable
private fun HousekeepingMenuTile(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(Color(0xFF015E4E).copy(alpha = 0.10f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = label, tint = Color(0xFF015E4E), modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(text = label, fontSize = 11.sp, color = Color(0xFF374151))
    }
}

@Composable
private fun HousekeepingActivePlanCard(plan: HousekeepingActivePlan, onClick: () -> Unit) {
    val progress = HousekeepingProgress.percent(
        plan.progressPercent,
        plan.taskDoneCount,
        plan.taskCount,
    )
    val overdue = HousekeepingProgress.isOverdue(plan.isOverdue, plan.slaStatus)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = plan.area?.name ?: "-",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF015E4E),
                )

                Text(
                    text = "$progress%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = when {
                        progress >= 100 -> Color(0xFF16A34A)
                        progress > 0 -> Color(0xFFD97706)
                        else -> Color(0xFF6B7280)
                    },
                )
            }

            Text(
                text = listOfNotNull(plan.planNumber, plan.planDate).joinToString(" • "),
                fontSize = 11.sp,
                color = Color(0xFF6B7280),
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = when {
                    progress >= 100 -> Color(0xFF16A34A)
                    progress > 0 -> Color(0xFFD97706)
                    else -> Color(0xFF9CA3AF)
                },
                trackColor = Color(0xFFE5E7EB),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = HousekeepingReport.verificationLabel(plan.status, plan.approvalStatus),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = when {
                        HousekeepingReport.isApproved(plan.approvalStatus) -> Color(0xFF166534)
                        HousekeepingReport.isRejected(plan.approvalStatus) -> Color(0xFFB91C1C)
                        !HousekeepingReport.isWorkCompleted(plan.status) -> Color(0xFF6B7280)
                        else -> Color(0xFF92400E)
                    },
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = HousekeepingProgress.checklistLabel(plan.taskDoneCount, plan.taskCount),
                    fontSize = 11.sp,
                    color = Color(0xFF6B7280),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = HousekeepingProgress.slaLabel(plan.slaDays),
                    fontSize = 11.sp,
                    color = Color(0xFF6B7280),
                )

                if (overdue) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Melewati SLA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFB91C1C),
                    )
                } else {
                    HousekeepingProgress.dueLabel(plan.dueDate)?.let {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = it, fontSize = 11.sp, color = Color(0xFF6B7280))
                    }
                }
            }
        }
    }
}
