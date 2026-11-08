package com.solu8i.cmms.ui.screen

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.solu8i.cmms.ui.viewmodel.MaintenanceRequestViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MaintenanceRequestCreateScreen(
    viewModel: MaintenanceRequestViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val equipmentList by viewModel.equipmentList.collectAsState()
    val isCreating by viewModel.isCreating.collectAsState()
    val createSuccess by viewModel.createSuccess.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var selectedAreaId by remember { mutableStateOf<Int?>(null) }
    var selectedAreaName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("") }
    var selectedEquipmentId by remember { mutableStateOf<Int?>(null) }
    var selectedEquipmentName by remember { mutableStateOf("") }
    var operationStatus by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var damageDate by remember { mutableStateOf("") }
    var damageTime by remember { mutableStateOf("") }
    var equipmentCondition by remember { mutableStateOf("") }
    var impact by remember { mutableStateOf("") }
    var earlyAction by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Open") }
    var selectedPhotos by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var areaExpanded by remember { mutableStateOf(false) }
    var typeExpanded by remember { mutableStateOf(false) }
    var equipmentExpanded by remember { mutableStateOf(false) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris: List<Uri> ->
        selectedPhotos = selectedPhotos + uris
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            val uri = bitmapToUri(context, it)
            if (uri != null) {
                selectedPhotos = selectedPhotos + uri
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted: Boolean ->
        if (granted) {
            cameraLauncher.launch(null)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadEquipment()
        viewModel.resetCreateState()
    }

    LaunchedEffect(createSuccess) {
        if (createSuccess) {
            onSuccess()
        }
    }

    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Pilih Sumber Foto") },
            text = { Text("Ambil foto dari kamera atau pilih dari galeri?") },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            showImageSourceDialog = false
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Kamera")
                    }
                    Button(
                        onClick = {
                            showImageSourceDialog = false
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF015E4E))
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Galeri")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showImageSourceDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val cal = java.util.Calendar.getInstance().apply { timeInMillis = millis }
                        damageDate = String.format(
                            "%04d-%02d-%02d",
                            cal.get(java.util.Calendar.YEAR),
                            cal.get(java.util.Calendar.MONTH) + 1,
                            cal.get(java.util.Calendar.DAY_OF_MONTH)
                        )
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Batal")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = 0,
            initialMinute = 0,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Pilih Waktu") },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(onClick = {
                    damageTime = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Batal")
                }
            }
        )
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Kirim Request") },
            text = { Text("Apakah Anda yakin ingin mengirim maintenance request ini?") },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    viewModel.createRequest(
                        equipmentId = selectedEquipmentId ?: 0,
                        operationStatus = operationStatus,
                        description = description,
                        damageDate = damageDate,
                        damageTime = damageTime.ifBlank { null },
                        equipmentCondition = equipmentCondition.ifBlank { null },
                        impact = impact.ifBlank { null },
                        earlyAction = earlyAction.ifBlank { null },
                        status = status,
                        photos = selectedPhotos
                    )
                }) {
                    Text("Kirim", color = Color(0xFF015E4E))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    val operationStatuses = listOf("Running", "Stopped", "Standby", "Faulty")
    val conditions = listOf("Good", "Fair", "Poor", "Critical")
    val impacts = listOf("Low", "Medium", "High", "Critical")

    val areaOptions = remember(equipmentList) {
        equipmentList
            .mapNotNull { eq -> eq.area?.let { it.id to it.name } }
            .distinctBy { it.first }
            .sortedBy { it.second }
    }

    val equipmentInArea = remember(equipmentList, selectedAreaId) {
        if (selectedAreaId == null) emptyList()
        else equipmentList.filter { it.area?.id == selectedAreaId }
    }

    val typeOptions = remember(equipmentInArea) {
        equipmentInArea.mapNotNull { it.equipmentType }.distinct().sorted()
    }

    val equipmentInType = remember(equipmentInArea, selectedType) {
        if (selectedType.isBlank()) emptyList()
        else equipmentInArea.filter { it.equipmentType == selectedType }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Buat Request",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F6FA))
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            item {
                Text(
                    text = "Equipment",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF015E4E)
                )
            }

            item {
                ExposedDropdownMenuBox(
                    expanded = areaExpanded,
                    onExpandedChange = { areaExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedAreaName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pilih Area") },
                        placeholder = { Text("Area") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = areaExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = areaExpanded,
                        onDismissRequest = { areaExpanded = false }
                    ) {
                        areaOptions.forEach { (id, name) ->
                            DropdownMenuItem(
                                text = { Text(name) },
                                onClick = {
                                    selectedAreaId = id
                                    selectedAreaName = name
                                    selectedType = ""
                                    selectedEquipmentId = null
                                    selectedEquipmentName = ""
                                    areaExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            item {
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = {
                        if (selectedAreaId != null) typeExpanded = it
                    }
                ) {
                    OutlinedTextField(
                        value = selectedType,
                        onValueChange = {},
                        readOnly = true,
                        enabled = selectedAreaId != null,
                        label = { Text("Pilih Tipe Equipment") },
                        placeholder = { Text(if (selectedAreaId == null) "Pilih area dulu" else "Tipe") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        typeOptions.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    selectedType = type
                                    selectedEquipmentId = null
                                    selectedEquipmentName = ""
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            item {
                ExposedDropdownMenuBox(
                    expanded = equipmentExpanded,
                    onExpandedChange = {
                        if (selectedType.isNotBlank()) equipmentExpanded = it
                    }
                ) {
                    OutlinedTextField(
                        value = selectedEquipmentName,
                        onValueChange = {},
                        readOnly = true,
                        enabled = selectedType.isNotBlank(),
                        label = { Text("Pilih Equipment") },
                        placeholder = { Text(if (selectedType.isBlank()) "Pilih tipe dulu" else "Equipment") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = equipmentExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = equipmentExpanded,
                        onDismissRequest = { equipmentExpanded = false }
                    ) {
                        equipmentInType.forEach { eq ->
                            DropdownMenuItem(
                                text = { Text("${eq.tagNumber} - ${eq.description ?: eq.equipmentType ?: ""}") },
                                onClick = {
                                    selectedEquipmentId = eq.id
                                    selectedEquipmentName = "${eq.tagNumber} - ${eq.description ?: eq.equipmentType ?: ""}"
                                    equipmentExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Status Operasi",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF015E4E)
                )
            }

            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    operationStatuses.forEach { os ->
                        FilterChipSimple(
                            text = os,
                            selected = operationStatus == os,
                            onClick = { operationStatus = os }
                        )
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Deskripsi Kerusakan *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }

            item {
                OutlinedTextField(
                    value = damageDate,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tanggal Kerusakan *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .let { mod ->
                            mod.then(Modifier.background(Color.White, RoundedCornerShape(4.dp)))
                        },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Pilih Tanggal",
                                tint = Color(0xFF015E4E)
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }

            item {
                OutlinedTextField(
                    value = damageTime,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Waktu") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .let { mod ->
                            mod.then(Modifier.background(Color.White, RoundedCornerShape(4.dp)))
                        },
                    trailingIcon = {
                        IconButton(onClick = { showTimePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Pilih Waktu",
                                tint = Color(0xFF015E4E)
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }

            item {
                Text(
                    text = "Kondisi Equipment",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF015E4E)
                )
            }

            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    conditions.forEach { c ->
                        FilterChipSimple(
                            text = c,
                            selected = equipmentCondition == c,
                            onClick = { equipmentCondition = c }
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Dampak",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF015E4E)
                )
            }

            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    impacts.forEach { i ->
                        FilterChipSimple(
                            text = i,
                            selected = impact == i,
                            onClick = { impact = i }
                        )
                    }
                }
            }

          
            item {
                Text(
                    text = "Foto (${selectedPhotos.size}/5)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF015E4E)
                )
            }

            item {
                Button(
                    onClick = { showImageSourceDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF015E4E)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tambah Foto")
                }
            }

            if (selectedPhotos.isNotEmpty()) {
                item {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        selectedPhotos.forEachIndexed { index, uri ->
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(8.dp))
                            ) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = "Foto ${index + 1}",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                IconButton(
                                    onClick = {
                                        selectedPhotos = selectedPhotos.toMutableList().also { it.removeAt(index) }
                                    },
                                    modifier = Modifier.align(Alignment.TopEnd)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Hapus",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (errorMessage != null) {
                item {
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFDC2626),
                        fontSize = 13.sp
                    )
                }
            }

            item {
                Button(
                    onClick = { showConfirmDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isCreating && selectedEquipmentId != null && operationStatus.isNotBlank() && description.isNotBlank() && damageDate.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF015E4E)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isCreating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Kirim Request")
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

private fun bitmapToUri(context: Context, bitmap: Bitmap): Uri? {
    return try {
        val file = File(context.cacheDir, "camera_${System.currentTimeMillis()}.jpg")
        file.outputStream().use { out ->
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
        }
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    } catch (_: Exception) {
        null
    }
}

@Composable
fun FilterChipSimple(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    androidx.compose.material3.FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = text,
                fontSize = 12.sp
            )
        },
        colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
            selectedContainerColor = Color(0xFF015E4E),
            selectedLabelColor = Color.White
        ),
        shape = RoundedCornerShape(20.dp)
    )
}
