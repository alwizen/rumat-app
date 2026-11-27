package com.solu8i.cmms.ui.screen

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solu8i.cmms.data.model.FindingItem
import com.solu8i.cmms.ui.viewmodel.FindingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindingListScreen(
    viewModel: FindingViewModel,
    onWorkOrderClick: (Int) -> Unit = {},
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp,
    onMenuClick: () -> Unit = {}
) {
    val findings by viewModel.findings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val hasMore by viewModel.hasMore.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()
    val selectedSeverity by viewModel.selectedSeverity.collectAsState()
    val query by viewModel.query.collectAsState()
    val dateFrom by viewModel.dateFrom.collectAsState()
    val dateTo by viewModel.dateTo.collectAsState()
    val message by viewModel.message.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val listState = rememberLazyListState()
    var showFilterSheet by remember { mutableStateOf(false) }
    var confirmFinding by remember { mutableStateOf<FindingItem?>(null) }

    val hasActiveFilter = selectedStatus != "All" || selectedSeverity.isNotBlank() ||
        dateFrom.isNotBlank() || dateTo.isNotBlank()

    val filterGroups = remember {
        listOf(
            FilterGroup(
                "Status",
                listOf(
                    FilterOption("All", "Semua"),
                    FilterOption("Open", "Open"),
                    FilterOption("Requested", "Sudah Diajukan"),
                    FilterOption("Resolved", "Selesai"),
                    FilterOption("Dismissed", "Diabaikan"),
                )
            ),
            FilterGroup(
                "Severity",
                listOf(
                    FilterOption("", "Semua"),
                    FilterOption("Low", "Low"),
                    FilterOption("Medium", "Medium"),
                    FilterOption("High", "High"),
                    FilterOption("Critical", "Critical"),
                )
            ),
        )
    }

    LaunchedEffect(Unit) {
        viewModel.loadFindings(status = null)
    }

    LaunchedEffect(listState, hasMore) {
        snapshotFlow {
            val info = listState.layoutInfo
            (info.visibleItemsInfo.lastOrNull()?.index ?: 0) to info.totalItemsCount
        }.collect { (lastVisible, total) ->
            if (hasMore && total > 0 && lastVisible >= total - 3) {
                viewModel.loadMore()
            }
        }
    }

    if (showFilterSheet) {
        FilterBottomSheet(
            title = "Filter Temuan",
            groups = filterGroups,
            current = mapOf("Status" to selectedStatus, "Severity" to selectedSeverity),
            onDismiss = { showFilterSheet = false },
            onApply = {},
            showDateRange = true,
            dateFrom = dateFrom,
            dateTo = dateTo,
            onApplyWithRange = { values, from, to ->
                viewModel.applyFilters(
                    status = values["Status"] ?: "All",
                    severity = values["Severity"] ?: "",
                    dateFrom = from,
                    dateTo = to,
                )
                showFilterSheet = false
            }
        )
    }

    confirmFinding?.let { finding ->
        AlertDialog(
            onDismissRequest = { if (!isSubmitting) confirmFinding = null },
            title = { Text("Ajukan Request Perbaikan") },
            text = {
                Text(
                    "Buat maintenance request perbaikan dari temuan ${finding.findingNumber}? " +
                        "Request akan dikirim ke SPV untuk ditindaklanjuti."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.submitRequest(finding.id)
                        confirmFinding = null
                    },
                    enabled = !isSubmitting
                ) {
                    Text("Ya, ajukan", color = Color(0xFF015E4E))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmFinding = null }, enabled = !isSubmitting) {
                    Text("Batal", color = Color(0xFF6B7280))
                }
            }
        )
    }

    (message ?: errorMessage)?.let { text ->
        val isError = errorMessage != null
        AlertDialog(
            onDismissRequest = {
                viewModel.consumeMessage()
                viewModel.consumeError()
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.consumeMessage()
                    viewModel.consumeError()
                }) {
                    Text("OK", color = if (isError) Color(0xFFDC2626) else Color(0xFF015E4E))
                }
            },
            title = { Text(if (isError) "Gagal" else "Berhasil") },
            text = { Text(text) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Buka menu",
                            tint = Color.White
                        )
                    }
                },
                title = {
                    Text(text = "Temuan", fontWeight = FontWeight.Bold)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF015E4E),
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F6FA))
                .padding(innerPadding)
        ) {
            SearchFilterBar(
                query = query,
                onQueryChange = viewModel::onQueryChanged,
                hasActiveFilter = hasActiveFilter,
                onClickFilter = { showFilterSheet = true },
                placeholder = "Cari nomor temuan, equipment..."
            )

            if (isLoading && findings.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF015E4E))
                }
            } else if (findings.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Belum ada temuan", color = Color(0xFF9CA3AF))
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = bottomPadding + 16.dp)
                ) {
                    items(findings) { finding ->
                        FindingCard(
                            finding = finding,
                            isSubmitting = isSubmitting,
                            onWorkOrderClick = onWorkOrderClick,
                            onSubmitRequest = { confirmFinding = finding }
                        )
                    }

                    if (isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF015E4E)
                                )
                            }
                        }
                    } else if (!hasMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Semua temuan sudah dimuat",
                                    fontSize = 11.sp,
                                    color = Color(0xFF9CA3AF)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FindingCard(
    finding: FindingItem,
    isSubmitting: Boolean = false,
    onWorkOrderClick: (Int) -> Unit = {},
    onSubmitRequest: () -> Unit = {}
) {
    val severityColor = when (finding.severity) {
        "Critical" -> Color(0xFFDC2626)
        "High" -> Color(0xFFEA580C)
        "Medium" -> Color(0xFFD97706)
        "Low" -> Color(0xFF2563EB)
        else -> Color(0xFF6B7280)
    }

    val statusColor = when (finding.status) {
        "Open" -> Color(0xFFD97706)
        "Requested" -> Color(0xFF2563EB)
        "Resolved" -> Color(0xFF16A34A)
        "Dismissed" -> Color(0xFF6B7280)
        else -> Color(0xFF6B7280)
    }

    val statusLabel = when (finding.status) {
        "Open" -> "Open"
        "Requested" -> "Sudah Diajukan"
        "Resolved" -> "Selesai"
        "Dismissed" -> "Diabaikan"
        else -> finding.status
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (finding.status == "Resolved") {
                        Icons.Default.CheckCircle
                    } else {
                        Icons.Default.Warning
                    },
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = finding.findingNumber,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1F2937),
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .background(severityColor.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = finding.severity,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = severityColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            finding.activityName?.let {
                Text(
                    text = it,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF374151)
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Equipment", fontSize = 11.sp, color = Color(0xFF9CA3AF))
                    Text(
                        text = finding.equipment?.let { "${it.tagNumber} - ${it.equipmentType ?: "-"}" } ?: "-",
                        fontSize = 12.sp,
                        color = Color(0xFF374151)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Status", fontSize = 11.sp, color = Color(0xFF9CA3AF))
                    Text(
                        text = statusLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = statusColor
                    )
                }
            }

            if (!finding.result.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Hasil: ${finding.result}",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
            }

            finding.description?.takeIf { it.isNotBlank() }?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = it, fontSize = 12.sp, color = Color(0xFF6B7280), maxLines = 3)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                finding.workOrder?.let { wo ->
                    Button(
                        onClick = { onWorkOrderClick(wo.id) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF015E4E))
                    ) {
                        Text("Lihat WO", fontSize = 13.sp)
                    }
                }

                if (finding.status == "Open") {
                    Button(
                        onClick = onSubmitRequest,
                        enabled = !isSubmitting,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                    ) {
                        Icon(
                            Icons.Default.Build,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ajukan Perbaikan", fontSize = 13.sp)
                    }
                } else if (finding.maintenanceRequest != null) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Icon(
                            Icons.Default.HourglassEmpty,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = finding.maintenanceRequest.requestNumber,
                            fontSize = 12.sp,
                            color = statusColor
                        )
                    }
                }
            }
        }
    }
}
