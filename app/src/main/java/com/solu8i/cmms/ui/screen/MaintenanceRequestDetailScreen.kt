package com.solu8i.cmms.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.solu8i.cmms.data.remote.RetrofitInstance
import com.solu8i.cmms.data.model.MaintenanceRequestDetail
import com.solu8i.cmms.ui.viewmodel.MaintenanceRequestViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MaintenanceRequestDetailScreen(
    requestId: Int,
    viewModel: MaintenanceRequestViewModel,
    onBack: () -> Unit
) {
    val detail by viewModel.detail.collectAsState()
    val isLoading by viewModel.isDetailLoading.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedPhoto by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(requestId) {
        viewModel.loadDetail(requestId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = detail?.requestNumber ?: "Detail Request",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF015E4E))
                }
            }
            detail == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Gagal memuat data", color = Color(0xFF9CA3AF))
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF5F6FA))
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    item { DetailStatusHeader(detail!!) }

                    item {
                        DetailInfoCard("Informasi Utama") {
                            DetailInfoRow("Nomor Request", detail!!.requestNumber)
                            DetailInfoRow("Status", detail!!.status)
                            DetailInfoRow("Status Operasi", detail!!.operationStatus)
                            DetailInfoRow("Deskripsi", detail!!.description)
                            detail!!.damageDate?.let { DetailInfoRow("Tanggal Kerusakan", it) }
                            detail!!.damageTime?.let { DetailInfoRow("Waktu", it) }
                        }
                    }

                    item {
                        DetailInfoCard("Kondisi & Dampak") {
                            detail!!.equipmentCondition?.let { DetailInfoRow("Kondisi", it) }
                            detail!!.impact?.let { DetailInfoRow("Dampak", it) }
                            detail!!.earlyAction?.let { DetailInfoRow("Tindakan Awal", it) }
                        }
                    }

                    item {
                        DetailInfoCard("Equipment") {
                            detail!!.equipment?.let { eq ->
                                DetailInfoRow("Tag Number", eq.tagNumber)
                                eq.equipmentType?.let { DetailInfoRow("Tipe", it) }
                                eq.area?.let { DetailInfoRow("Area", it.name) }
                            }
                        }
                    }

                    item {
                        DetailInfoCard("Pelapor") {
                            detail!!.reportedBy?.let { DetailInfoRow("Nama", it.name) }
                            detail!!.createdAt?.let { DetailInfoRow("Tanggal Laporan", it.take(10)) }
                        }
                    }

                    if (detail!!.status == "Approved" || detail!!.status == "Rejected") {
                        item {
                            DetailInfoCard("Approval") {
                                detail!!.approvedBy?.let { DetailInfoRow("Disetujui Oleh", it.name) }
                                detail!!.approvalNotes?.let { DetailInfoRow("Catatan", it) }
                                detail!!.approvedAt?.let { DetailInfoRow("Tanggal Approval", it.take(10)) }
                            }
                        }
                    }

                    detail!!.photo?.let { photos ->
                        if (photos.isNotEmpty()) {
                            item {
                                DetailInfoCard("Dokumentasi Foto (${photos.size})") {
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        photos.forEach { photo ->
                                            AsyncImage(
                                                model = "${RetrofitInstance.getStorageUrl()}/$photo",
                                                contentDescription = "Foto",
                                                modifier = Modifier
                                                    .size(100.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable {
                                                        selectedPhoto = "${RetrofitInstance.getStorageUrl()}/$photo"
                                                    },
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    detail!!.workOrders?.let { workOrders ->
                        if (workOrders.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Work Orders Terkait",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF015E4E)
                                )
                            }

                            items(workOrders) { wo ->
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
                                        DetailInfoRow("Nomor WO", wo.workOrderNumber)
                                        DetailInfoRow("Status", wo.status)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus Request") },
            text = { Text("Apakah Anda yakin ingin menghapus request ini?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteRequest(requestId)
                    showDeleteDialog = false
                    onBack()
                }) {
                    Text("Hapus", color = Color(0xFFDC2626))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    selectedPhoto?.let { url ->
        Dialog(onDismissRequest = { selectedPhoto = null }) {
            AsyncImage(
                model = url,
                contentDescription = "Foto Full Size",
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
fun DetailStatusHeader(detail: MaintenanceRequestDetail) {
    val statusColor = when (detail.status) {
        "Open" -> Color(0xFF2563EB)
        "Approved" -> Color(0xFF16A34A)
        "Assigned" -> Color(0xFF7C3AED)
        "In Progress" -> Color(0xFFD97706)
        "Completed" -> Color(0xFF16A34A)
        "Rejected" -> Color(0xFFDC2626)
        else -> Color(0xFF6B7280)
    }

    val statusIcon = when (detail.status) {
        "Completed" -> Icons.Default.CheckCircle
        "In Progress" -> Icons.Default.PlayCircle
        else -> Icons.Default.HourglassEmpty
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = statusIcon,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = detail.status,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
                Text(
                    text = detail.equipment?.let { "${it.tagNumber} - ${it.equipmentType}" } ?: "-",
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280)
                )
            }
        }
    }
}

@Composable
fun DetailInfoCard(
    title: String,
    content: @Composable () -> Unit
) {
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
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF015E4E)
            )
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
fun DetailInfoRow(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFF9CA3AF)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF374151)
        )
    }
}
