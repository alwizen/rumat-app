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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solu8i.cmms.data.model.HousekeepingPlanItem
import com.solu8i.cmms.ui.util.HousekeepingProgress
import com.solu8i.cmms.ui.viewmodel.HousekeepingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HousekeepingListScreen(
    viewModel: HousekeepingViewModel,
    onPlanClick: (Int) -> Unit = {},
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp,
    onMenuClick: () -> Unit = {}
) {
    val plans by viewModel.plans.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val hasMore by viewModel.hasMorePlans.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val selectedStatus by viewModel.selectedStatus.collectAsState()
    val selectedAreaId by viewModel.selectedAreaId.collectAsState()
    val query by viewModel.query.collectAsState()
    val dateFrom by viewModel.dateFrom.collectAsState()
    val dateTo by viewModel.dateTo.collectAsState()
    val areas by viewModel.areas.collectAsState()

    val listState = rememberLazyListState()
    var showFilterSheet by remember { mutableStateOf(false) }

    val hasActiveFilter = selectedStatus != "All" || selectedAreaId != null ||
        dateFrom.isNotBlank() || dateTo.isNotBlank()

    val filterGroups = remember(areas) {
        listOf(
            FilterGroup(
                "Status",
                listOf(
                    FilterOption("All", "Semua"),
                    FilterOption("Draft", "Draft"),
                    FilterOption("In Progress", "In Progress"),
                    FilterOption("Completed", "Completed"),
                )
            ),
            FilterGroup(
                "Area",
                listOf(FilterOption("", "Semua")) +
                    areas.map { FilterOption(it.id.toString(), it.name) }
            ),
        )
    }

    // Selalu ambil ulang saat layar ini dibuka kembali (mis. kembali dari detail
    // setelah checklist diselesaikan) agar status & progres plan tidak tertinggal.
    LaunchedEffect(Unit) {
        viewModel.loadAreas()
        viewModel.refresh()
    }

    // Infinite scroll: muat halaman berikutnya saat mendekati bawah.
    LaunchedEffect(listState, hasMore) {
        snapshotFlow {
            val info = listState.layoutInfo
            (info.visibleItemsInfo.lastOrNull()?.index ?: 0) to info.totalItemsCount
        }.collect { (lastVisible, total) ->
            if (hasMore && total > 0 && lastVisible >= total - 3) {
                viewModel.loadMorePlans()
            }
        }
    }

    if (showFilterSheet) {
        FilterBottomSheet(
            title = "Filter Housekeeping",
            groups = filterGroups,
            current = mapOf(
                "Status" to selectedStatus,
                "Area" to (selectedAreaId?.toString() ?: ""),
            ),
            onDismiss = { showFilterSheet = false },
            onApply = {},
            showDateRange = true,
            dateFrom = dateFrom,
            dateTo = dateTo,
            onApplyWithRange = { values, from, to ->
                viewModel.applyFilters(
                    status = values["Status"] ?: "All",
                    areaId = values["Area"]?.takeIf { it.isNotBlank() }?.toIntOrNull(),
                    dateFrom = from,
                    dateTo = to,
                )
                showFilterSheet = false
            }
        )
    }

    errorMessage?.let { text ->
        AlertDialog(
            onDismissRequest = { viewModel.consumeError() },
            confirmButton = {
                TextButton(onClick = { viewModel.consumeError() }) {
                    Text("OK", color = Color(0xFFDC2626))
                }
            },
            title = { Text("Gagal") },
            text = { Text(text) }
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
                placeholder = "Cari nomor plan, area..."
            )

            when {
                isLoading && plans.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF015E4E))
                }

                errorMessage != null && plans.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(errorMessage ?: "Gagal memuat data", color = Color(0xFF9CA3AF), fontSize = 13.sp)
                }

                plans.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.CleaningServices,
                            contentDescription = null,
                            tint = Color(0xFFD1D5DB),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Belum ada plan housekeeping", color = Color(0xFF9CA3AF), fontSize = 13.sp)
                    }
                }

                else -> LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 12.dp,
                        bottom = bottomPadding + 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(plans, key = { it.id }) { plan ->
                        HousekeepingPlanCard(plan = plan, onClick = { onPlanClick(plan.id) })
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
                    } else if (!hasMore && plans.isNotEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Semua plan sudah dimuat",
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
private fun HousekeepingPlanCard(plan: HousekeepingPlanItem, onClick: () -> Unit) {
    val progress = HousekeepingProgress.percent(
        plan.progressPercent,
        plan.taskDoneCount,
        plan.taskCount,
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = plan.planNumber,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF111827)
                )
                StatusBadge(status = plan.status)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = plan.area?.name ?: "-",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = Color(0xFF015E4E)
            )

            Text(
                text = listOfNotNull(
                    plan.planDate,
                    plan.area?.company?.name,
                    plan.supervisor?.let { "PJ: $it" }
                ).joinToString(" • "),
                fontSize = 12.sp,
                color = Color(0xFF6B7280)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Progress", fontSize = 11.sp, color = Color(0xFF6B7280))
                Text(
                    text = "$progress% (${HousekeepingProgress.checklistLabel(plan.taskDoneCount, plan.taskCount)})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF374151)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

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
                trackColor = Color(0xFFE5E7EB)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MiniStat(label = "Checklist", value = HousekeepingProgress.checklistLabel(plan.taskDoneCount, plan.taskCount))
                MiniStat(label = "Manpower", value = "${plan.manpowerCount ?: 0}")
                MiniStat(label = "SLA", value = HousekeepingProgress.slaLabel(plan.slaDays).removePrefix("SLA "))
            }

            if (HousekeepingProgress.isOverdue(plan.isOverdue, plan.slaStatus)) {
                Spacer(modifier = Modifier.height(8.dp))
                SlaBadge(
                    label = HousekeepingProgress.slaStatusLabel(plan.slaStatus),
                    dueDate = plan.dueDate,
                )
            } else {
                HousekeepingProgress.dueLabel(plan.dueDate)?.let { due ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(due, fontSize = 11.sp, color = Color(0xFF6B7280))
                }
            }
        }
    }
}

@Composable
private fun SlaBadge(label: String, dueDate: String?) {
    Row(
        modifier = Modifier
            .background(Color(0xFFFEE2E2), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("$label", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFB91C1C))
        HousekeepingProgress.dueLabel(dueDate)?.let {
            Spacer(modifier = Modifier.width(6.dp))
            Text(it, fontSize = 11.sp, color = Color(0xFFB91C1C))
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String) {
    Row(
        modifier = Modifier
            .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("$label: ", fontSize = 11.sp, color = Color(0xFF6B7280))
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF374151))
    }
}

@Composable
fun StatusBadge(status: String) {
    val (bg, fg) = when (status) {
        "Completed" -> Color(0xFFDCFCE7) to Color(0xFF166534)
        "In Progress" -> Color(0xFFFEF3C7) to Color(0xFF92400E)
        else -> Color(0xFFF3F4F6) to Color(0xFF374151)
    }

    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(status, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = fg)
    }
}
