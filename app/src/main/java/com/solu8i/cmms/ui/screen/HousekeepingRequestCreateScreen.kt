package com.solu8i.cmms.ui.screen

import android.Manifest
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
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import coil.compose.AsyncImage
import com.solu8i.cmms.ui.util.ImageFiles
import com.solu8i.cmms.ui.viewmodel.HousekeepingRequestViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val MAX_REQUEST_PHOTOS = 5

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HousekeepingRequestCreateScreen(
    viewModel: HousekeepingRequestViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val areas by viewModel.areas.collectAsState()
    val facilities by viewModel.facilities.collectAsState()
    val isLoadingFacilities by viewModel.isLoadingFacilities.collectAsState()
    val isCreating by viewModel.isCreating.collectAsState()
    val createSuccess by viewModel.createSuccess.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var selectedAreaId by remember { mutableStateOf<Int?>(null) }
    var selectedAreaName by remember { mutableStateOf("") }
    var selectedFacilityId by remember { mutableStateOf<Int?>(null) }
    var selectedFacilityName by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Normal") }
    var description by remember { mutableStateOf("") }
    var requestDate by remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()))
    }
    var requestTime by remember { mutableStateOf("") }
    var selectedPhotos by remember { mutableStateOf<List<Uri>>(emptyList()) }

    var areaExpanded by remember { mutableStateOf(false) }
    var facilityExpanded by remember { mutableStateOf(false) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = MAX_REQUEST_PHOTOS)
    ) { uris: List<Uri> ->
        selectedPhotos = (selectedPhotos + uris).take(MAX_REQUEST_PHOTOS)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            if (selectedPhotos.size < MAX_REQUEST_PHOTOS) {
                val uri = ImageFiles.bitmapToUri(context, it)
                if (uri != null) {
                    selectedPhotos = (selectedPhotos + uri).take(MAX_REQUEST_PHOTOS)
                }
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
        viewModel.loadAreas()
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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                        requestDate = String.format(
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
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    requestTime = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
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
            text = { Text("Apakah Anda yakin ingin mengirim housekeeping request ini?") },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    viewModel.createRequest(
                        areaId = selectedAreaId ?: 0,
                        facilityId = selectedFacilityId,
                        requestDate = requestDate,
                        requestTime = requestTime.ifBlank { null },
                        description = description,
                        priority = priority,
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

    val priorities = listOf("Low", "Normal", "High")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Buat Request Housekeeping",
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
                    text = "Lokasi",
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
                        label = { Text("Pilih Area *") },
                        placeholder = { Text("Area") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = areaExpanded)
                        },
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
                        areas.forEach { area ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        if (area.company != null) "${area.name} — ${area.company}"
                                        else area.name
                                    )
                                },
                                onClick = {
                                    selectedAreaId = area.id
                                    selectedAreaName = area.name
                                    selectedFacilityId = null
                                    selectedFacilityName = ""
                                    viewModel.clearFacilities()
                                    viewModel.loadFacilities(area.id)
                                    areaExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            item {
                ExposedDropdownMenuBox(
                    expanded = facilityExpanded,
                    onExpandedChange = {
                        if (selectedAreaId != null) facilityExpanded = it
                    }
                ) {
                    OutlinedTextField(
                        value = selectedFacilityName,
                        onValueChange = {},
                        readOnly = true,
                        enabled = selectedAreaId != null,
                        label = { Text("Fasilitas (opsional)") },
                        placeholder = {
                            Text(
                                when {
                                    selectedAreaId == null -> "Pilih area dulu"
                                    isLoadingFacilities -> "Memuat fasilitas..."
                                    facilities.isEmpty() -> "Tidak ada fasilitas"
                                    else -> "Seluruh area"
                                }
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = facilityExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = facilityExpanded,
                        onDismissRequest = { facilityExpanded = false }
                    ) {
                        facilities.forEach { facility ->
                            DropdownMenuItem(
                                text = { Text(facility.name) },
                                onClick = {
                                    selectedFacilityId = facility.id
                                    selectedFacilityName = facility.name
                                    facilityExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            if (selectedFacilityId != null) {
                item {
                    TextButton(onClick = {
                        selectedFacilityId = null
                        selectedFacilityName = ""
                    }) {
                        Text("Kosongkan fasilitas (berlaku seluruh area)", fontSize = 12.sp)
                    }
                }
            }

            item {
                Text(
                    text = "Prioritas",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF015E4E)
                )
            }

            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    priorities.forEach { value ->
                        FilterChipSimple(
                            text = value,
                            selected = priority == value,
                            onClick = { priority = value }
                        )
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Deskripsi Pekerjaan *") },
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
                    value = requestDate,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tanggal Request *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(4.dp)),
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
                    value = requestTime,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Waktu (opsional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(4.dp)),
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
                    text = "Foto (${selectedPhotos.size}/$MAX_REQUEST_PHOTOS)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF015E4E)
                )
            }

            item {
                Button(
                    onClick = {
                        if (selectedPhotos.size >= MAX_REQUEST_PHOTOS) {
                            android.widget.Toast.makeText(
                                context,
                                "Maksimal $MAX_REQUEST_PHOTOS foto",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            showImageSourceDialog = true
                        }
                    },
                    enabled = selectedPhotos.size < MAX_REQUEST_PHOTOS,
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
                                        selectedPhotos =
                                            selectedPhotos.toMutableList().also { it.removeAt(index) }
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
                    enabled = !isCreating && selectedAreaId != null && description.isNotBlank(),
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
