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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import com.solu8i.cmms.ui.viewmodel.MeterLogViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentDetailScreen(
    equipmentId: Int,
    viewModel: EquipmentViewModel,
    meterLogViewModel: MeterLogViewModel,
    onBack: () -> Unit,
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp
) {
    val detail by viewModel.detail.collectAsState()
    val isLoading by viewModel.isDetailLoading.collectAsState()
    val logs by meterLogViewModel.logs.collectAsState()
    val isLogLoading by meterLogViewModel.isLoading.collectAsState()
    val message by meterLogViewModel.message.collectAsState()
    val logError by meterLogViewModel.errorMessage.collectAsState()
    val isSaving by meterLogViewModel.isSaving.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(equipmentId) {
        viewModel.loadDetail(equipmentId)
        meterLogViewModel.filterByEquipment(equipmentId)
    }

    message?.let {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { meterLogViewModel.consumeMessage() },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { meterLogViewModel.consumeMessage() }) {
                    Text("OK", color = Color(0xFF015E4E))
                }
            },
            title = { Text("Berhasil") },
            text = { Text(it) }
        )
    }

    logError?.let {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { meterLogViewModel.consumeError() },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { meterLogViewModel.consumeError() }) {
                    Text("OK", color = Color(0xFFDC2626))
                }
            },
            title = { Text("Gagal") },
            text = { Text(it) }
        )
    }

    if (showAddDialog) {
        AddMeterLogForEquipmentDialog(
            isSaving = isSaving,
            onDismiss = { showAddDialog = false },
            onSave = { date, value ->
                meterLogViewModel.createLog(equipmentId, date, value)
                showAddDialog = false
            }
        )
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Meter Log",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF015E4E)
                                )
                                Text(
                                    text = "${logs.size} pembacaan",
                                    fontSize = 11.sp,
                                    color = Color(0xFF9CA3AF)
                                )
                            }

                            Button(
                                onClick = { showAddDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF015E4E))
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.size(6.dp))
                                Text("Catat", fontSize = 13.sp)
                            }
                        }
                    }

                    if (isLogLoading && logs.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF015E4E)
                                )
                            }
                        }
                    } else if (logs.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Text(
                                    text = "Belum ada pembacaan meter untuk equipment ini",
                                    modifier = Modifier.padding(16.dp),
                                    fontSize = 12.sp,
                                    color = Color(0xFF9CA3AF)
                                )
                            }
                        }
                    } else {
                        items(logs) { log ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = log.readingDate ?: "-",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF1F2937)
                                        )
                                        Text(
                                            text = log.recordedByName ?: "-",
                                            fontSize = 11.sp,
                                            color = Color(0xFF9CA3AF)
                                        )
                                    }

                                    Text(
                                        text = formatValue(log.value),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF015E4E)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddMeterLogForEquipmentDialog(
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var date by remember { mutableStateOf(MeterLogViewModel.today()) }
    var value by remember { mutableStateOf("") }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Catat Meter", fontSize = 15.sp, fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Tanggal (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text("Nilai Meter") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("mis. 1250.5") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(date, value) },
                enabled = value.isNotBlank() && !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF015E4E))
            ) {
                Text(if (isSaving) "Menyimpan..." else "Simpan")
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text("Batal", color = Color(0xFF6B7280))
            }
        }
    )
}

private fun formatValue(value: Double): String {
    return if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        String.format(java.util.Locale.US, "%.2f", value)
    }
}
