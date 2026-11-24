package com.solu8i.cmms.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solu8i.cmms.ui.viewmodel.EquipmentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentDetailScreen(
    equipmentId: Int,
    viewModel: EquipmentViewModel,
    onBack: () -> Unit,
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp
) {
    val detail by viewModel.detail.collectAsState()
    val isLoading by viewModel.isDetailLoading.collectAsState()
    val histories by viewModel.histories.collectAsState()
    val isHistoriesLoading by viewModel.isHistoriesLoading.collectAsState()

    LaunchedEffect(equipmentId) {
        viewModel.loadDetail(equipmentId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(detail?.tagNumber ?: "Detail Equipment", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF015E4E),
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        when {
            isLoading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF015E4E))
            }

            detail == null -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Gagal memuat data", color = Color(0xFF9CA3AF))
            }

            else -> {
                val item = detail!!

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF5F6FA))
                        .padding(innerPadding),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                        bottom = bottomPadding + 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        InfoCard("Informasi Equipment") {
                            InfoRow("Tag Number", item.tagNumber)
                            item.description?.let { InfoRow("Deskripsi", it) }
                            item.equipmentType?.let { InfoRow("Tipe", it) }
                            item.equipmentTypeDetail?.responsibleUser?.name?.let { InfoRow("Penanggung Jawab", it) }
                            item.area?.let { InfoRow("Area", it.name) }
                            item.manufacturer?.let { InfoRow("Manufacturer", it) }
                            item.model?.let { InfoRow("Model", it) }
                            item.serialNumber?.let { InfoRow("Serial Number", it) }
                            item.installationDate?.let { InfoRow("Tanggal Instalasi", it) }
                            item.operationalUnit?.let { InfoRow("Unit Operasional", it) }
                            item.status?.let { InfoRow("Status", it) }
                            item.criticality?.let { InfoRow("Criticality", it) }
                        }
                    }

                    item {
                        Text(
                            text = "Riwayat Maintenance",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF015E4E)
                        )
                    }

                    if (isHistoriesLoading && histories.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF015E4E)
                                )
                            }
                        }
                    } else if (histories.isEmpty()) {
                        item {
                            InfoCard("Riwayat") {
                                Text(
                                    text = "Belum ada riwayat maintenance untuk equipment ini.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF9CA3AF)
                                )
                            }
                        }
                    } else {
                        items(histories) { history ->
                            MaintenanceHistoryCard(history)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MaintenanceHistoryCard(history: com.solu8i.cmms.data.model.MaintenanceHistoryItem) {
    var expanded by remember { mutableStateOf(false) }

    val statusColor = when (history.status) {
        "Completed" -> Color(0xFF16A34A)
        "In Progress" -> Color(0xFF2563EB)
        else -> Color(0xFF6B7280)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = history.workOrderNumber ?: history.description ?: "Maintenance",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1F2937)
                    )
                    Text(
                        text = history.maintenanceDate?.take(10) ?: "-",
                        fontSize = 11.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Tutup" else "Buka checklist",
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Checklist",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF015E4E)
                        )
                        Box(
                            modifier = Modifier
                                .background(statusColor.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = history.status ?: "-",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = statusColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    val checklist = history.checklist.orEmpty()
                    if (checklist.isEmpty()) {
                        Text(
                            text = "Tidak ada item checklist pada work order ini.",
                            fontSize = 12.sp,
                            color = Color(0xFF9CA3AF)
                        )
                    } else {
                        checklist.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = if (item.executed == true) {
                                        Icons.Default.CheckCircle
                                    } else {
                                        Icons.Default.RadioButtonUnchecked
                                    },
                                    contentDescription = null,
                                    tint = if (item.executed == true) {
                                        Color(0xFF16A34A)
                                    } else {
                                        Color(0xFFD1D5DB)
                                    },
                                    modifier = Modifier
                                        .padding(top = 1.dp)
                                        .size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.name,
                                        fontSize = 12.sp,
                                        color = Color(0xFF374151)
                                    )
                                    item.finalResult?.takeIf { it.isNotBlank() }?.let {
                                        Text(
                                            text = "Hasil: $it",
                                            fontSize = 11.sp,
                                            color = Color(0xFF6B7280)
                                        )
                                    }
                                    item.note?.takeIf { it.isNotBlank() }?.let {
                                        Text(
                                            text = it,
                                            fontSize = 11.sp,
                                            color = Color(0xFF9CA3AF)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    history.actionsTaken?.takeIf { it.isNotBlank() }?.let {
                        Spacer(modifier = Modifier.height(6.dp))
                        InfoRow("Tindakan", it)
                    }
                    history.performedBy?.name?.let {
                        InfoRow("Dikerjakan", it)
                    }
                }
            }
        }
    }
}


