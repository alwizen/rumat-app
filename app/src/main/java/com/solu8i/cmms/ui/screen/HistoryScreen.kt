package com.solu8i.cmms.ui.screen

import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.IconButton
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.solu8i.cmms.data.model.WorkOrderItem
import com.solu8i.cmms.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: MainViewModel,
    onWorkOrderClick: (Int) -> Unit = {},
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp,
    onMenuClick: () -> Unit = {}
) {
    val workOrders by viewModel.workOrders.collectAsState()
    val isWorkOrdersLoading by viewModel.isWorkOrdersLoading.collectAsState()
    val isWorkOrdersLoadingMore by viewModel.isWorkOrdersLoadingMore.collectAsState()
    val hasMoreWorkOrders by viewModel.hasMoreWorkOrders.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()
    val query by viewModel.query.collectAsState()
    val classificationFilter by viewModel.classificationFilter.collectAsState()
    val dateFrom by viewModel.dateFrom.collectAsState()
    val dateTo by viewModel.dateTo.collectAsState()
    val listState = rememberLazyListState()

    var showFilterSheet by remember { mutableStateOf(false) }
    val hasActiveFilter = selectedStatus != "All" || classificationFilter.isNotBlank() ||
        dateFrom.isNotBlank() || dateTo.isNotBlank()

    val filterGroups = remember {
        listOf(
            FilterGroup(
                "Status",
                listOf(
                    FilterOption("All", "Semua"),
                    FilterOption("Pending", "Pending"),
                    FilterOption("In Progress", "In Progress"),
                    FilterOption("On Hold", "On Hold"),
                    FilterOption("Completed", "Completed"),
                    FilterOption("Cancelled", "Cancelled"),
                )
            ),
            FilterGroup(
                "Klasifikasi",
                listOf(
                    FilterOption("", "Semua"),
                    FilterOption("Preventive", "Preventive"),
                    FilterOption("Corrective", "Corrective"),
                )
            ),
        )
    }

    LaunchedEffect(Unit) {
        viewModel.loadWorkOrders(status = null)
    }

    if (showFilterSheet) {
        FilterBottomSheet(
            title = "Filter Work Order",
            groups = filterGroups,
            current = mapOf("Status" to selectedStatus, "Klasifikasi" to classificationFilter),
            onDismiss = { showFilterSheet = false },
            onApply = {},
            showDateRange = true,
            dateFrom = dateFrom,
            dateTo = dateTo,
            onApplyWithRange = { values, from, to ->
                viewModel.applyFilters(
                    status = values["Status"] ?: "All",
                    classification = values["Klasifikasi"] ?: "",
                    dateFrom = from,
                    dateTo = to,
                )
                showFilterSheet = false
            }
        )
    }

    // Infinite scroll: muat halaman berikutnya saat mendekati bawah.
    LaunchedEffect(listState, hasMoreWorkOrders) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible to layoutInfo.totalItemsCount
        }.collect { (lastVisible, totalCount) ->
            if (hasMoreWorkOrders && totalCount > 0 && lastVisible >= totalCount - 3) {
                viewModel.loadMoreWorkOrders()
            }
        }
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
                    Text(
                        text = "Work Order",
                        fontWeight = FontWeight.Bold
                    )
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
                placeholder = "Cari nomor WO, equipment..."
            )

            if (isWorkOrdersLoading && workOrders.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF015E4E))
                }
            } else if (workOrders.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Tidak ada data", color = Color(0xFF9CA3AF))
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
                    items(workOrders) { wo ->
                        HistoryWorkOrderCard(
                            wo = wo,
                            onClick = { onWorkOrderClick(wo.id) }
                        )
                    }

                    if (isWorkOrdersLoadingMore) {
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
                    } else if (!hasMoreWorkOrders && workOrders.isNotEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Semua riwayat sudah dimuat",
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
fun HistoryWorkOrderCard(wo: WorkOrderItem, onClick: () -> Unit = {}) {
    val statusColor = when (wo.status) {
        "Completed" -> Color(0xFF16A34A)
        "In Progress" -> Color(0xFF2563EB)
        "On Hold" -> Color(0xFFD97706)
        "Cancelled" -> Color(0xFFDC2626)
        "Pending" -> Color(0xFF6B7280)
        else -> Color(0xFF6B7280)
    }

    val statusIcon = when (wo.status) {
        "Completed" -> Icons.Default.CheckCircle
        "In Progress" -> Icons.Default.PlayCircle
        else -> Icons.Default.HourglassEmpty
    }

    androidx.compose.material3.Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color.White),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                    imageVector = statusIcon,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = wo.workOrderNumber,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1F2937),
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .background(
                            statusColor.copy(alpha = 0.1f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = wo.status,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Equipment",
                        fontSize = 11.sp,
                        color = Color(0xFF9CA3AF)
                    )
                    Text(
                        text = wo.equipment?.let { "${it.tagNumber} - ${it.equipmentType}" } ?: "-",
                        fontSize = 12.sp,
                        color = Color(0xFF374151)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Klasifikasi",
                        fontSize = 11.sp,
                        color = Color(0xFF9CA3AF)
                    )
                    Text(
                        text = wo.classification,
                        fontSize = 12.sp,
                        color = Color(0xFF374151)
                    )
                }
            }

            if (wo.startAt != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Mulai: ${wo.startAt.take(10)}",
                    fontSize = 11.sp,
                    color = Color(0xFF9CA3AF)
                )
            }
        }
    }
}
