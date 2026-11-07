package com.solu8i.cmms.ui.screen

import android.Manifest
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.solu8i.cmms.data.remote.RetrofitInstance
import com.solu8i.cmms.ui.util.ImageFiles
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Text
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
import com.solu8i.cmms.data.model.WorkOrderActivityItem
import com.solu8i.cmms.data.model.WorkOrderDetail
import com.solu8i.cmms.ui.viewmodel.WorkOrderDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkOrderDetailScreen(
    workOrderId: Int,
    viewModel: WorkOrderDetailViewModel,
    onBack: () -> Unit,
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp
) {
    val detail by viewModel.detail.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val message by viewModel.message.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var editingActivity by remember { mutableStateOf<WorkOrderActivityItem?>(null) }
    var confirmStatus by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(workOrderId) {
        viewModel.loadDetail(workOrderId)
    }

    editingActivity?.let { activity ->
        ActivityResultDialog(
            activity = activity,
            isSubmitting = isSubmitting,
            onDismiss = { editingActivity = null },
            onSave = { pre, final, follow, executed, note, newPhotos ->
                viewModel.saveActivity(
                    workOrderId = workOrderId,
                    activity = activity,
                    preInspection = pre,
                    finalResult = final,
                    followUp = follow,
                    executed = executed,
                    note = note
                )

                if (newPhotos.isNotEmpty()) {
                    viewModel.uploadActivityPhotos(workOrderId, activity.id, newPhotos)
                }

                editingActivity = null
            },
            onDeletePhoto = { path ->
                viewModel.deleteActivityPhoto(workOrderId, activity.id, path)
                editingActivity = null
            }
        )
    }

    confirmStatus?.let { status ->
        AlertDialog(
            onDismissRequest = { confirmStatus = null },
            title = { Text("Ubah Status") },
            text = { Text("Ubah status work order menjadi \"$status\"?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateStatus(workOrderId, status)
                    confirmStatus = null
                }) {
                    Text("Ya, ubah", color = Color(0xFF015E4E))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmStatus = null }) {
                    Text("Batal", color = Color(0xFF6B7280))
                }
            }
        )
    }

    (message ?: errorMessage)?.let { text ->
        val isError = errorMessage != null
        AlertDialog(
            onDismissRequest = {
                viewModel.consumeMessage()
                viewModel.consumeError()
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.consumeMessage()
                    viewModel.consumeError()
                }) {
                    Text("OK", color = if (isError) Color(0xFFDC2626) else Color(0xFF015E4E))
                }
            },
            title = { Text(if (isError) "Gagal" else "Berhasil") },
            text = { Text(text) }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F6FA))) {
        TopAppBar(
            title = {
                Text(
                    text = detail?.workOrderNumber ?: "Detail WO",
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

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF015E4E))
                }
            }
            detail == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Gagal memuat data", color = Color(0xFF9CA3AF))
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                        bottom = bottomPadding + 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { StatusHeader(detail!!) }

                    item {
                        WorkOrderActions(
                            status = detail!!.status,
                            isSubmitting = isSubmitting,
                            onChangeStatus = { confirmStatus = it }
                        )
                    }

                    item {
                        InfoCard("Informasi Utama") {
                            InfoRow("Nomor WO", detail!!.workOrderNumber)
                            InfoRow("Status", detail!!.status)
                            InfoRow("Klasifikasi", detail!!.classification)
                            detail!!.interval?.let { InfoRow("Interval", it) }
                            detail!!.startAt?.let { InfoRow("Tanggal Mulai", it.take(10)) }
                            detail!!.finishAt?.let { InfoRow("Tanggal Selesai", it.take(10)) }
                            detail!!.note?.let { InfoRow("Catatan", it) }
                        }
                    }

                    item {
                        InfoCard("Equipment") {
                            detail!!.equipment?.let { eq ->
                                InfoRow("Tag Number", eq.tagNumber)
                                eq.equipmentType?.let { InfoRow("Tipe", it) }
                                eq.area?.let { InfoRow("Area", it.name) }
                            }
                        }
                    }

                    item {
                        InfoCard("Personel") {
                            detail!!.issuedBy?.let { InfoRow("Diterbitkan Oleh", it.name) }
                            detail!!.technicianCoordinator?.let { InfoRow("Koordinator", it.name) }
                        }
                    }

                    

                    detail!!.activities?.let { activities ->
                        if (activities.isEmpty()) {
                            item {
                                InfoCard("Aktivitas") {
                                    Text(
                                        text = "Work order ini belum memiliki checklist aktivitas. " +
                                            "Hubungi supervisor untuk menambahkan item pemeriksaan.",
                                        fontSize = 12.sp,
                                        color = Color(0xFF9CA3AF)
                                    )
                                }
                            }
                        }

                        if (activities.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Aktivitas",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF015E4E)
                                )
                            }

                            items(activities) { act ->
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
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = act.activity?.name ?: "Aktivitas #${act.id}",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF1F2937)
                                            )
                                            val executed = act.executed == true
                                            Icon(
                                                imageVector = if (executed) Icons.Default.CheckCircle
                                                else Icons.Default.HourglassEmpty,
                                                contentDescription = null,
                                                tint = if (executed) Color(0xFF16A34A) else Color(0xFFD97706),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        act.activity?.type?.let {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = it,
                                                fontSize = 12.sp,
                                                color = Color(0xFF9CA3AF)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        act.reference?.let { InfoRow("Referensi", it) }
                                        act.preInspection?.let { InfoRow("Pre Inspection", it) }
                                        act.followUp?.let { InfoRow("Follow Up", it) }
                                        act.finalResult?.let { InfoRow("Final Result", it) }
                                        act.note?.let { InfoRow("Catatan", it) }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        OutlinedButton(
                                            onClick = { editingActivity = act },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = if (act.executed == true) "Edit Hasil" else "Isi Hasil Inspeksi",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium
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
    }
}

@Composable
fun StatusHeader(wo: WorkOrderDetail) {
    val statusColor = when (wo.status) {
        "Completed" -> Color(0xFF16A34A)
        "In Progress" -> Color(0xFF2563EB)
        "On Hold" -> Color(0xFFD97706)
        "Cancelled" -> Color(0xFFDC2626)
        else -> Color(0xFF6B7280)
    }

    val statusIcon = when (wo.status) {
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
                    text = wo.status,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
                Text(
                    text = wo.equipment?.let { "${it.tagNumber} - ${it.equipmentType}" } ?: "-",
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280)
                )
            }
        }
    }
}

@Composable
fun InfoCard(
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
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF9CA3AF),
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF374151),
            modifier = Modifier.weight(0.6f)
        )
    }
}

@Composable
fun WorkOrderActions(
    status: String,
    isSubmitting: Boolean,
    onChangeStatus: (String) -> Unit
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
                text = "Aksi Pekerjaan",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF015E4E)
            )

            Spacer(modifier = Modifier.height(10.dp))

            when (status) {
                "Pending" -> Button(
                    onClick = { onChangeStatus("In Progress") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSubmitting,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Icon(Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mulai Pekerjaan")
                }

                "In Progress" -> Column {
                    Button(
                        onClick = { onChangeStatus("Completed") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSubmitting,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tandai Selesai")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { onChangeStatus("On Hold") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSubmitting,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Tunda Pekerjaan", color = Color(0xFFD97706))
                    }
                }

                "On Hold" -> Button(
                    onClick = { onChangeStatus("In Progress") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSubmitting,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Icon(Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Lanjutkan Pekerjaan")
                }

                "Completed" -> Text(
                    text = "Work order sudah selesai. Data hasil eksekusi dapat dilihat di bawah.",
                    fontSize = 12.sp,
                    color = Color(0xFF16A34A)
                )

                else -> Text(
                    text = "Work order berstatus $status dan tidak dapat dieksekusi.",
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Isi hasil inspeksi pada tiap aktivitas di bawah.",
                fontSize = 11.sp,
                color = Color(0xFF9CA3AF)
            )
        }
    }
}

@Composable
fun ActivityResultDialog(
    activity: WorkOrderActivityItem,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSave: (String?, String?, String?, Boolean, String?, List<Uri>) -> Unit,
    onDeletePhoto: (String) -> Unit = {}
) {
    val context = LocalContext.current

    var preInspection by remember(activity.id) { mutableStateOf(activity.preInspection ?: "") }
    var finalResult by remember(activity.id) { mutableStateOf(activity.finalResult ?: "") }
    var followUp by remember(activity.id) { mutableStateOf(activity.followUp ?: "") }
    var note by remember(activity.id) { mutableStateOf(activity.note ?: "") }
    var executed by remember(activity.id) { mutableStateOf(activity.executed == true) }

    val existingPhotos = activity.photos.orEmpty()
    var newPhotos by remember(activity.id) { mutableStateOf<List<Uri>>(emptyList()) }
    var previewPath by remember(activity.id) { mutableStateOf<String?>(null) }

    val remainingSlots = (3 - existingPhotos.size - newPhotos.size).coerceAtLeast(0)

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 3)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            newPhotos = newPhotos + uris.take(remainingSlots)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            ImageFiles.bitmapToUri(context, it)?.let { uri ->
                newPhotos = newPhotos + uri
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

    var showSourceDialog by remember(activity.id) { mutableStateOf(false) }

    if (showSourceDialog) {
        AlertDialog(
            onDismissRequest = { showSourceDialog = false },
            title = { Text("Tambah Foto") },
            text = { Text("Ambil foto dari kamera atau pilih dari galeri?") },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {
                        showSourceDialog = false
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Kamera")
                    }
                    Button(
                        onClick = {
                            showSourceDialog = false
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF015E4E))
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Galeri")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showSourceDialog = false }) { Text("Batal") }
            }
        )
    }

    previewPath?.let { path ->
        val model = if (path.startsWith("http")) path else "${RetrofitInstance.getStorageUrl()}/$path"

        AlertDialog(
            onDismissRequest = { previewPath = null },
            confirmButton = {
                TextButton(onClick = { previewPath = null }) { Text("Tutup", color = Color(0xFF015E4E)) }
            },
            text = {
                AsyncImage(
                    model = model,
                    contentDescription = "Foto dokumentasi",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Fit
                )
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = activity.activity?.name ?: "Aktivitas #${activity.id}",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                activity.activity?.let { info ->
                    val reference = listOfNotNull(info.type, info.unit).joinToString(" • ")
                    if (reference.isNotBlank()) {
                        Text(text = reference, fontSize = 11.sp, color = Color(0xFF9CA3AF))
                    }
                }

                OutlinedTextField(
                    value = preInspection,
                    onValueChange = { if (it.length <= 100) preInspection = it },
                    label = { Text("Pre Inspection") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = finalResult,
                    onValueChange = { if (it.length <= 100) finalResult = it },
                    label = {
                        Text(
                            activity.activity?.unit?.let { "Final Result ($it)" } ?: "Final Result"
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = followUp,
                    onValueChange = { if (it.length <= 100) followUp = it },
                    label = { Text("Follow Up") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Catatan") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = executed,
                        onCheckedChange = { executed = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tandai sudah dieksekusi",
                        fontSize = 12.sp,
                        color = Color(0xFF374151)
                    )
                }

                HorizontalDivider(color = Color(0xFFF3F4F6))

                Text(
                    text = "Foto Dokumentasi (opsional, maks. 3)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF015E4E)
                )

                if (existingPhotos.isEmpty() && newPhotos.isEmpty()) {
                    Text(
                        text = "Belum ada foto.",
                        fontSize = 11.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    existingPhotos.forEach { path ->
                        PhotoThumbnail(
                            model = "${RetrofitInstance.getStorageUrl()}/$path",
                            onPreview = { previewPath = path },
                            onRemove = { onDeletePhoto(path) }
                        )
                    }

                    newPhotos.forEachIndexed { index, uri ->
                        PhotoThumbnail(
                            model = uri,
                            onPreview = {
                                previewPath = uri.toString()
                            },
                            onRemove = {
                                newPhotos = newPhotos.filterIndexed { i, _ -> i != index }
                            }
                        )
                    }
                }

                if (remainingSlots > 0) {
                    OutlinedButton(
                        onClick = { showSourceDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tambah Foto ($remainingSlots tersisa)", fontSize = 13.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        preInspection.takeIf { it.isNotBlank() },
                        finalResult.takeIf { it.isNotBlank() },
                        followUp.takeIf { it.isNotBlank() },
                        executed,
                        note.takeIf { it.isNotBlank() },
                        newPhotos
                    )
                },
                enabled = !isSubmitting
            ) {
                Text(if (isSubmitting) "Menyimpan..." else "Simpan", color = Color(0xFF015E4E))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                Text("Batal", color = Color(0xFF6B7280))
            }
        }
    )
}

@Composable
fun PhotoThumbnail(
    model: Any?,
    onPreview: () -> Unit,
    onRemove: () -> Unit
) {
    Box(modifier = Modifier.size(72.dp)) {
        AsyncImage(
            model = model,
            contentDescription = "Foto dokumentasi",
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onPreview),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(20.dp)
                .background(Color(0xFFDC2626), CircleShape)
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Hapus foto",
                tint = Color.White,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}
