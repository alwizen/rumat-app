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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solu8i.cmms.data.model.EquipmentListItem
import com.solu8i.cmms.ui.viewmodel.EquipmentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentListScreen(
    viewModel: EquipmentViewModel,
    onEquipmentClick: (Int) -> Unit = {},
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp,
    onMenuClick: () -> Unit = {}
) {
    val equipment by viewModel.equipment.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val query by viewModel.query.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadEquipment()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Buka menu", tint = Color.White)
                    }
                },
                title = { Text("Daftar Equipment", fontWeight = FontWeight.Bold) },
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
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onQueryChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                placeholder = { Text("Cari tag, deskripsi, atau tipe", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF9CA3AF))
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            when {
                isLoading && equipment.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF015E4E))
                }

                errorMessage != null && equipment.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(errorMessage ?: "Gagal memuat data", color = Color(0xFF9CA3AF), fontSize = 13.sp)
                }

                equipment.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Tidak ada equipment", color = Color(0xFF9CA3AF), fontSize = 13.sp)
                }

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 4.dp,
                        bottom = bottomPadding + 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(equipment) { item ->
                        EquipmentRow(item = item, onClick = { onEquipmentClick(item.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun EquipmentRow(item: EquipmentListItem, onClick: () -> Unit) {
    val statusColor = when (item.status) {
        "active" -> Color(0xFF16A34A)
        "inactive" -> Color(0xFFD97706)
        "retired" -> Color(0xFFDC2626)
        else -> Color(0xFF6B7280)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onClick
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
                    text = item.tagNumber.take(2),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF015E4E)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.tagNumber,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1F2937)
                )
                Text(
                    text = listOfNotNull(item.equipmentType, item.area?.name).joinToString(" • "),
                    fontSize = 11.sp,
                    color = Color(0xFF6B7280)
                )
                item.description?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        fontSize = 11.sp,
                        color = Color(0xFF9CA3AF),
                        maxLines = 1
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = item.status ?: "-",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = statusColor
                    )
                }

                item.criticality?.let {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(text = it, fontSize = 10.sp, color = Color(0xFF9CA3AF))
                }

                item.latestMeterLog?.let { meter ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = Color(0xFF015E4E),
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = formatMeter(meter.value),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF015E4E)
                        )
                    }
                }
            }
        }
    }
}

private fun formatMeter(value: Double): String {
    return if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        String.format(java.util.Locale.US, "%.2f", value)
    }
}
