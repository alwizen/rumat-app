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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solu8i.cmms.data.model.EquipmentListItem
import com.solu8i.cmms.data.model.MeterLogItem
import com.solu8i.cmms.ui.viewmodel.EquipmentViewModel
import com.solu8i.cmms.ui.viewmodel.MeterLogViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeterLogScreen(
    viewModel: MeterLogViewModel,
    equipmentViewModel: EquipmentViewModel,
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp,
    onMenuClick: () -> Unit = {}
) {
    val logs by viewModel.logs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val query by viewModel.query.collectAsState()
    val message by viewModel.message.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val equipment by equipmentViewModel.equipment.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.resetFilter()
        equipmentViewModel.loadEquipment()
    }

    message?.let {
        AlertDialog(
            onDismissRequest = { viewModel.consumeMessage() },
            confirmButton = {
                TextButton(onClick = { viewModel.consumeMessage() }) {
                    Text("OK", color = Color(0xFF015E4E))
                }
            },
            title = { Text("Berhasil") },
            text = { Text(it) }
        )
    }

    errorMessage?.let {
        AlertDialog(
            onDismissRequest = { viewModel.consumeError() },
            confirmButton = {
                TextButton(onClick = { viewModel.consumeError() }) {
                    Text("OK", color = Color(0xFFDC2626))
                }
            },
            title = { Text("Gagal") },
            text = { Text(it) }
        )
    }

    if (showAddDialog) {
        AddMeterLogDialog(
            equipmentList = equipment,
            isSaving = isSaving,
            onDismiss = { showAddDialog = false },
            onSave = { equipmentId, date, value ->
                viewModel.createLog(equipmentId, date, value)
                showAddDialog = false
            }
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
                title = { Text("Meter Log", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF015E4E),
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF015E4E),
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Catat Meter") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F6FA))
                .padding(innerPadding)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onQueryChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                placeholder = { Text("Cari tag atau deskripsi equipment", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF9CA3AF))
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            when {
                isLoading && logs.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF015E4E))
                }

                logs.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada meter log",
                        color = Color(0xFF9CA3AF),
                        fontSize = 13.sp
                    )
                }

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 4.dp,
                        bottom = bottomPadding + 88.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(logs) { log ->
                        MeterLogRow(log)
                    }
                }
            }
        }
    }
}

@Composable
private fun MeterLogRow(log: MeterLogItem) {
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF015E4E).copy(alpha = 0.08f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = log.equipment?.tagNumber?.take(2) ?: "--",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF015E4E)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.equipment?.tagNumber ?: "Equipment #${log.equipmentId}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1F2937)
                )
                Text(
                    text = log.equipment?.description ?: "-",
                    fontSize = 11.sp,
                    color = Color(0xFF6B7280),
                    maxLines = 1
                )
                Text(
                    text = listOfNotNull(log.readingDate, log.recordedByName).joinToString(" • "),
                    fontSize = 10.sp,
                    color = Color(0xFF9CA3AF)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatMeterValue(log.value),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF015E4E)
                )
                Text(text = "reading", fontSize = 9.sp, color = Color(0xFF9CA3AF))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddMeterLogDialog(
    equipmentList: List<EquipmentListItem>,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (Int, String, String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedEquipment by remember { mutableStateOf<EquipmentListItem?>(null) }
    var date by remember { mutableStateOf(MeterLogViewModel.today()) }
    var value by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Catat Meter Log", fontSize = 15.sp, fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedEquipment?.let { "${it.tagNumber} — ${it.description ?: ""}" }
                            ?: "Pilih equipment",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Equipment") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        equipmentList.forEach { item ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "${item.tagNumber} — ${item.description ?: ""}",
                                        fontSize = 13.sp
                                    )
                                },
                                onClick = {
                                    selectedEquipment = item
                                    expanded = false
                                }
                            )
                        }
                    }
                }

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

                Text(
                    text = "Gunakan titik untuk desimal, mis. 1250.5",
                    fontSize = 10.sp,
                    color = Color(0xFF9CA3AF)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedEquipment?.let { onSave(it.id, date, value) }
                },
                enabled = selectedEquipment != null && value.isNotBlank() && !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF015E4E))
            ) {
                Text(if (isSaving) "Menyimpan..." else "Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text("Batal", color = Color(0xFF6B7280))
            }
        }
    )
}

private fun formatMeterValue(value: Double): String {
    return if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        String.format(java.util.Locale.US, "%.2f", value)
    }
}
