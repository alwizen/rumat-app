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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.solu8i.cmms.data.model.HousekeepingRequestItem
import com.solu8i.cmms.ui.viewmodel.HousekeepingRequestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HousekeepingRequestListScreen(
    viewModel: HousekeepingRequestViewModel,
    onRequestClick: (Int) -> Unit = {},
    onCreateClick: () -> Unit = {},
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp,
    onMenuClick: () -> Unit = {}
) {
    val requests by viewModel.requests.collectAsState()
    val isLoading by viewModel.isRequestsLoading.collectAsState()
    val isLoadingMore by viewModel.isRequestsLoadingMore.collectAsState()
    val hasMore by viewModel.hasMoreRequests.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()
    val query by viewModel.query.collectAsState()
    val dateFrom by viewModel.dateFrom.collectAsState()
    val dateTo by viewModel.dateTo.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val message by viewModel.message.collectAsState()

    val listState = rememberLazyListState()
    var showFilterSheet by remember { mutableStateOf(false) }
    val hasActiveFilter = selectedStatus != "All" || dateFrom.isNotBlank() || dateTo.isNotBlank()

    val filterGroups = remember {
        listOf(
            FilterGroup(
                "Status",
                listOf(
                    FilterOption("All", "Semua"),
                    FilterOption("Open", "Open"),
                    FilterOption("Approved", "Approved"),
                    FilterOption("Rejected", "Rejected"),
                )
            )
        )
    }

    LaunchedEffect(Unit) {
        viewModel.loadRequests(status = null)
    }

    LaunchedEffect(listState, hasMore) {
        snapshotFlow {
            val info = listState.layoutInfo
            (info.visibleItemsInfo.lastOrNull()?.index ?: 0) to info.totalItemsCount
        }.collect { (lastVisible, total) ->
            if (hasMore && total > 0 && lastVisible >= total - 3) {
                viewModel.loadMoreRequests()
            }
        }
    }

    if (showFilterSheet) {
        FilterBottomSheet(
            title = "Filter Request Housekeeping",
            groups = filterGroups,
            current = mapOf("Status" to selectedStatus),
            onDismiss = { showFilterSheet = false },
            onApply = {},
            showDateRange = true,
            dateFrom = dateFrom,
            dateTo = dateTo,
            onApplyWithRange = { values, from, to ->
                viewModel.applyFilters(
                    status = values["Status"] ?: "All",
                    dateFrom = from,
                    dateTo = to,
                )
                showFilterSheet = false
            }
        )
    }

    message?.let { text ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { viewModel.consumeMessage() },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { viewModel.consumeMessage() }) {
                    Text("OK", color = Color(0xFF015E4E))
                }
            },
            title = { Text("Berhasil") },
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
                    Text(
                        text = "Request Housekeeping",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF015E4E),
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateClick,
                containerColor = Color(0xFF015E4E),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Buat Request Housekeeping",
                    tint = Color.White
                )
            }
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
                placeholder = "Cari nomor, deskripsi..."
            )

            when {
                isLoading && requests.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF015E4E))
                }

                requests.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CleaningServices,
                            contentDescription = null,
                            tint = Color(0xFFD1D5DB),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage ?: "Belum ada request housekeeping",
                            color = Color(0xFF9CA3AF),
                            fontSize = 13.sp
                        )
                    }
                }

                else -> LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = bottomPadding + 16.dp)
                ) {
                    items(requests) { request ->
                        HousekeepingRequestCard(
                            request = request,
                            onClick = { onRequestClick(request.id) }
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
                                    text = "Semua data sudah dimuat",
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
private fun HousekeepingRequestCard(
    request: HousekeepingRequestItem,
    onClick: () -> Unit = {}
) {
    val statusColor = when (request.status) {
        "Open" -> Color(0xFF2563EB)
        "Approved" -> Color(0xFF16A34A)
        "Rejected" -> Color(0xFFDC2626)
        else -> Color(0xFF6B7280)
    }

    val statusIcon = when (request.status) {
        "Approved" -> Icons.Default.CheckCircle
        "Open" -> Icons.Default.PlayCircle
        else -> Icons.Default.HourglassEmpty
    }

    val priorityColor = when (request.priority) {
        "High" -> Color(0xFFDC2626)
        "Low" -> Color(0xFF6B7280)
        else -> Color(0xFFD97706)
    }

    Card(
        onClick = onClick,
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
                    imageVector = statusIcon,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = request.requestNumber,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1F2937),
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = request.status,
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Area",
                        fontSize = 11.sp,
                        color = Color(0xFF9CA3AF)
                    )
                    Text(
                        text = request.area?.name ?: "-",
                        fontSize = 12.sp,
                        color = Color(0xFF374151)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Prioritas",
                        fontSize = 11.sp,
                        color = Color(0xFF9CA3AF)
                    )
                    Text(
                        text = request.priority,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = priorityColor
                    )
                }
            }

            if (request.facility != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Fasilitas: ${request.facility.name}",
                    fontSize = 11.sp,
                    color = Color(0xFF6B7280)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = request.description,
                fontSize = 12.sp,
                color = Color(0xFF6B7280),
                maxLines = 2
            )

            val dateLabel = listOfNotNull(
                request.requestDate,
                request.requestTime
            ).joinToString(" ")

            if (dateLabel.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tanggal: $dateLabel",
                    fontSize = 11.sp,
                    color = Color(0xFF9CA3AF)
                )
            }
        }
    }
}
