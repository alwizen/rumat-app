package com.solu8i.cmms.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solu8i.cmms.data.model.DocumentItem
import com.solu8i.cmms.ui.viewmodel.DocumentViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DocumentScreen(
    viewModel: DocumentViewModel,
    bottomPadding: Dp = 0.dp,
    onMenuClick: () -> Unit = {},
    onDocumentClick: (Int) -> Unit = {}
) {
    val documents by viewModel.documents.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val query by viewModel.query.collectAsState()
    val typeFilter by viewModel.typeFilter.collectAsState()
    val expiryFilter by viewModel.expiryFilter.collectAsState()

    var showFilterSheet by remember { mutableStateOf(false) }
    val hasActiveFilter = typeFilter.isNotBlank() || expiryFilter.isNotBlank()

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    if (showFilterSheet) {
        var tempType by remember { mutableStateOf(typeFilter) }
        var tempExpiry by remember { mutableStateOf(expiryFilter) }

        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp)
            ) {
                Text(
                    text = "Filter Dokumen",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF015E4E)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Tipe Dokumen",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF374151)
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    DocumentViewModel.TYPE_OPTIONS.forEach { (value, label) ->
                        FilterChip(
                            selected = tempType == value,
                            onClick = { tempType = value },
                            label = { Text(label, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Status Expiry",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF374151)
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    DocumentViewModel.EXPIRY_OPTIONS.forEach { (value, label) ->
                        FilterChip(
                            selected = tempExpiry == value,
                            onClick = { tempExpiry = value },
                            label = { Text(label, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            tempType = ""
                            tempExpiry = ""
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Reset", fontSize = 14.sp)
                    }

                    Button(
                        onClick = {
                            viewModel.applyFilters(tempType, tempExpiry)
                            showFilterSheet = false
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF015E4E))
                    ) {
                        Text("Terapkan", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
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

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Buka menu", tint = Color.White)
                    }
                },
                title = { Text("Dokumen", fontWeight = FontWeight.Bold) },
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = viewModel::onQueryChanged,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Cari judul, nomor, tag, dll", fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF9CA3AF))
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (hasActiveFilter) Color(0xFF015E4E) else Color.White)
                        .border(
                            width = 1.dp,
                            color = if (hasActiveFilter) Color(0xFF015E4E) else Color(0xFFD1D5DB),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { showFilterSheet = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter",
                        tint = if (hasActiveFilter) Color.White else Color(0xFF6B7280)
                    )

                    if (hasActiveFilter) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(7.dp)
                                .size(8.dp)
                                .background(Color(0xFFDC2626), CircleShape)
                        )
                    }
                }
            }

            when {
                isLoading && documents.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF015E4E))
                }

                documents.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada dokumen",
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
                        bottom = bottomPadding + 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(documents) { document ->
                        DocumentRow(document, onClick = { onDocumentClick(document.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentRow(document: DocumentItem, onClick: () -> Unit = {}) {
    val accent = when (document.expiryStatus) {
        "expired" -> Color(0xFFDC2626)
        "expiring_soon" -> Color(0xFFF59E0B)
        "valid" -> Color(0xFF15803D)
        else -> Color(0xFF6B7280)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = document.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Color(0xFF111827),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = document.expiryStatusLabel ?: "-",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = accent
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = listOfNotNull(
                    document.documentTypeLabel,
                    document.equipment?.tagNumber,
                    document.documentNumber
                ).joinToString(" • "),
                fontSize = 12.sp,
                color = Color(0xFF6B7280)
            )

            document.expiryDate?.let {
                Text(
                    text = "Expired: $it",
                    fontSize = 12.sp,
                    color = accent
                )
            }
        }
    }
}
