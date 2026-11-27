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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.solu8i.cmms.ui.viewmodel.HousekeepingRequestViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HousekeepingRequestDetailScreen(
    requestId: Int,
    viewModel: HousekeepingRequestViewModel,
    onBack: () -> Unit
) {
    val detail by viewModel.detail.collectAsState()
    val isLoading by viewModel.isDetailLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val message by viewModel.message.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedPhoto by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(requestId) {
        viewModel.loadDetail(requestId)
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus Request") },
            text = { Text("Request yang dihapus tidak dapat dikembalikan. Lanjutkan?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteRequest(requestId)
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

    message?.let { text ->
        AlertDialog(
            onDismissRequest = { viewModel.consumeMessage() },
            confirmButton = {
                TextButton(onClick = { viewModel.consumeMessage() }) {
                    Text("OK", color = Color(0xFF015E4E))
                }
            },
            title = { Text("Berhasil") },
            text = { Text(text) }
        )
    }

    selectedPhoto?.let { photo ->
        Dialog(onDismissRequest = { selectedPhoto = null }) {
            AsyncImage(
                model = photo,
                contentDescription = "Foto",
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Fit
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detail Request Housekeeping",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
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
                actions = {
                    if (detail?.status == "Open") {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Hapus",
                                tint = Color.White
                            )
                        }
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
            isLoading && detail == null -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5F6FA))
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF015E4E))
            }

            detail == null -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5F6FA))
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = errorMessage ?: "Data tidak ditemukan",
                    color = Color(0xFF9CA3AF),
                    fontSize = 13.sp
                )
            }

            else -> {
                val data = detail!!

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF5F6FA))
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
                ) {
                    item {
                        DetailInfoCard("Informasi Request") {
                            DetailInfoRow("Nomor Request", data.requestNumber)
                            DetailInfoRow("Status", data.status)
                            DetailInfoRow("Prioritas", data.priority)
                            data.requestDate?.let { DetailInfoRow("Tanggal", it) }
                            data.requestTime?.let { DetailInfoRow("Waktu", it) }
                            data.requestedBy?.let { DetailInfoRow("Diminta Oleh", it) }
                        }
                    }

                    item {
                        DetailInfoCard("Lokasi") {
                            DetailInfoRow("Area", data.area?.name ?: "-")
                            DetailInfoRow("Fasilitas", data.facility?.name ?: "Seluruh area")
                        }
                    }

                    item {
                        DetailInfoCard("Deskripsi") {
                            Text(
                                text = data.description,
                                fontSize = 13.sp,
                                color = Color(0xFF374151)
                            )
                        }
                    }

                    data.photo?.let { photos ->
                        if (photos.isNotEmpty()) {
                            item {
                                DetailInfoCard("Dokumentasi Foto (${photos.size})") {
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        photos.forEach { photo ->
                                            val url = RetrofitInstance.resolveMediaUrl(photo)
                                            AsyncImage(
                                                model = url,
                                                contentDescription = "Foto",
                                                modifier = Modifier
                                                    .size(100.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable { selectedPhoto = url },
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (data.status == "Approved" || data.status == "Rejected") {
                        item {
                            DetailInfoCard("Approval") {
                                data.approvedBy?.let { DetailInfoRow("Diproses Oleh", it) }
                                data.approvalNotes?.let { DetailInfoRow("Catatan", it) }
                                data.approvedAt?.let { DetailInfoRow("Tanggal Approval", it.take(10)) }
                            }
                        }
                    }

                    data.plan?.let { plan ->
                        item {
                            DetailInfoCard("Plan Harian Terkait") {
                                DetailInfoRow("Nomor Plan", plan.planNumber)
                                plan.planDate?.let { DetailInfoRow("Tanggal Plan", it) }
                                DetailInfoRow("Status Plan", plan.status)
                            }
                        }
                    }

                    if (data.status == "Open") {
                        item {
                            Button(
                                onClick = { showDeleteDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.size(6.dp))
                                Text("Hapus Request")
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }
        }
    }
}
