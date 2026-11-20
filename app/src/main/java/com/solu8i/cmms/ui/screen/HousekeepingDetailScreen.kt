package com.solu8i.cmms.ui.screen

import android.Manifest
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.solu8i.cmms.data.model.HousekeepingPlanDetail
import com.solu8i.cmms.data.model.HousekeepingPlanFacilityItem
import com.solu8i.cmms.data.model.HousekeepingTaskItem
import com.solu8i.cmms.data.remote.RetrofitInstance
import com.solu8i.cmms.ui.util.HousekeepingFacilityTaskGroup
import com.solu8i.cmms.ui.util.HousekeepingGrouping
import com.solu8i.cmms.ui.util.HousekeepingProgress
import com.solu8i.cmms.ui.util.HousekeepingReport
import com.solu8i.cmms.ui.util.ImageFiles
import com.solu8i.cmms.ui.viewmodel.HousekeepingDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HousekeepingDetailScreen(
    planId: Int,
    viewModel: HousekeepingDetailViewModel,
    onBack: () -> Unit,
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp,
    canExecute: Boolean = true,
) {
    val detail by viewModel.detail.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val message by viewModel.message.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var photoDialog by remember { mutableStateOf<Pair<HousekeepingPlanFacilityItem, String>?>(null) }

    LaunchedEffect(planId) {
        viewModel.loadDetail(planId)
    }

    photoDialog?.let { (planFacility, type) ->
        PhotoUploadDialog(
            title = if (type == "before") "Upload Foto Before" else "Upload Foto After",
            existingPhotos = (if (type == "before") planFacility.photoBefore else planFacility.photoAfter).orEmpty(),
            isSubmitting = isSubmitting,
            onDismiss = { photoDialog = null },
            onUpload = { uris ->
                viewModel.uploadFacilityPhotos(planId, planFacility, type, uris)
                photoDialog = null
            },
            onDelete = { path ->
                viewModel.deleteFacilityPhoto(planId, planFacility, type, path)
                photoDialog = null
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
                    text = detail?.planNumber ?: "Detail Housekeeping",
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
            isLoading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF015E4E))
            }

            detail == null -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Gagal memuat data", color = Color(0xFF9CA3AF))
            }

            else -> {
                val data = detail!!
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
                    item { HousekeepingStatusHeader(data) }

                    item { HousekeepingProgressCard(data) }

                    item {
                        InfoCard("Informasi Plan") {
                            InfoRow("Nomor Plan", data.planNumber)
                            InfoRow("Tanggal", data.planDate ?: "-")
                            InfoRow("Status", data.status)
                            InfoRow("Verifikasi SPV GOS", HousekeepingReport.verificationLabel(data.status, data.approvalStatus))
                            data.approvedBy?.let { InfoRow("Disetujui Oleh", it) }
                            data.approvalNotes?.takeIf { it.isNotBlank() }?.let {
                                InfoRow("Catatan Approval", it)
                            }
                            InfoRow("SLA", HousekeepingProgress.slaLabel(data.area?.slaDays ?: data.slaDays))
                            HousekeepingProgress.dueLabel(data.dueDate)?.let {
                                InfoRow("Batas Waktu", it.removePrefix("Batas: "))
                            }
                            data.slaStatus?.let {
                                InfoRow("Status SLA", HousekeepingProgress.slaStatusLabel(it))
                            }
                            data.area?.let { area ->
                                InfoRow("Area", area.name ?: "-")
                                area.company?.let { InfoRow("Company", it) }
                                area.supervisor?.let { InfoRow("Penanggung Jawab", it) }
                                area.areaSize?.let { InfoRow("Luas Area", "%.2f m²".format(it)) }
                            }
                            data.createdBy?.let { InfoRow("Dibuat Oleh", it) }
                            data.note?.let { InfoRow("Catatan", it) }
                        }
                    }

                    val manpowers = data.manpowers.orEmpty()
                    item {
                        InfoCard("Manpower (Petugas)") {
                            if (manpowers.isEmpty()) {
                                Text(
                                    "Belum ada manpower ditugaskan.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF9CA3AF)
                                )
                            } else {
                                manpowers.forEach { manpower ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = null,
                                            tint = Color(0xFF015E4E),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                manpower.name,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF374151)
                                            )
                                            manpower.employeeNumber?.let {
                                                Text(it, fontSize = 11.sp, color = Color(0xFF9CA3AF))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Satu card per fasilitas: checklist + dokumentasi foto jadi satu.
                    val grouping = HousekeepingGrouping.group(
                        tasks = data.tasks.orEmpty(),
                        facilities = data.facilities.orEmpty(),
                    )
                    val groups = grouping.groups
                    val looseTasks = grouping.looseTasks

                    item {
                        Text(
                            text = "Checklist & Dokumentasi",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF015E4E)
                        )
                    }

                    if (groups.isEmpty() && looseTasks.isEmpty()) {
                        item {
                            InfoCard("Checklist") {
                                Text(
                                    "Belum ada item checklist pada plan ini.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF9CA3AF)
                                )
                            }
                        }
                    }

                    items(groups, key = { "facility-${it.key}" }) { group ->
                        HousekeepingFacilityTaskCard(
                            group = group,
                            isSubmitting = isSubmitting,
                            canExecute = canExecute,
                            onUploadPhoto = { type ->
                                group.planFacility?.let { photoDialog = it to type }
                            },
                            onStartAll = { tasks ->
                                viewModel.startTasks(planId, tasks)
                            },
                            onFinishChecked = { tasks ->
                                viewModel.finishTasks(planId, tasks)
                            }
                        )
                    }

                    if (looseTasks.isNotEmpty()) {
                        item {
                            HousekeepingLooseTasksCard(
                                tasks = looseTasks,
                                isSubmitting = isSubmitting,
                                canExecute = canExecute,
                                onStartAll = { tasks ->
                                    viewModel.startTasks(planId, tasks)
                                },
                                onFinishChecked = { tasks ->
                                    viewModel.finishTasks(planId, tasks)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HousekeepingStatusHeader(plan: HousekeepingPlanDetail) {
    val statusColor = when (plan.status) {
        "Completed" -> Color(0xFF16A34A)
        "In Progress" -> Color(0xFF2563EB)
        else -> Color(0xFF6B7280)
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
                imageVector = Icons.Default.CleaningServices,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(plan.status, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = statusColor)
                Text(
                    text = plan.area?.name ?: "-",
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280)
                )
            }

            val approvalColor = when {
                HousekeepingReport.isApproved(plan.approvalStatus) -> Color(0xFF166534)
                HousekeepingReport.isRejected(plan.approvalStatus) -> Color(0xFFB91C1C)
                !HousekeepingReport.isWorkCompleted(plan.status) -> Color(0xFF6B7280)
                else -> Color(0xFF92400E)
            }

            val approvalBg = when {
                HousekeepingReport.isApproved(plan.approvalStatus) -> Color(0xFFDCFCE7)
                HousekeepingReport.isRejected(plan.approvalStatus) -> Color(0xFFFEE2E2)
                !HousekeepingReport.isWorkCompleted(plan.status) -> Color(0xFFF3F4F6)
                else -> Color(0xFFFEF3C7)
            }

            Box(
                modifier = Modifier
                    .background(approvalBg, RoundedCornerShape(999.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = HousekeepingReport.verificationLabel(plan.status, plan.approvalStatus),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = approvalColor
                )
            }
        }
    }
}

@Composable
private fun HousekeepingProgressCard(plan: HousekeepingPlanDetail) {
    val progress = HousekeepingProgress.percent(
        plan.progressPercent,
        plan.tasks?.count { it.status == "Done" },
        plan.tasks?.size,
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Progress Pekerjaan",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF015E4E)
                )
                Text(
                    "$progress%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        progress >= 100 -> Color(0xFF16A34A)
                        progress > 0 -> Color(0xFFD97706)
                        else -> Color(0xFF6B7280)
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = when {
                    progress >= 100 -> Color(0xFF16A34A)
                    progress > 0 -> Color(0xFFD97706)
                    else -> Color(0xFF9CA3AF)
                },
                trackColor = Color(0xFFE5E7EB)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Dihitung otomatis: ${plan.tasks?.count { it.status == "Done" } ?: 0}/${plan.tasks?.size ?: 0} item checklist selesai",
                fontSize = 11.sp,
                color = Color(0xFF6B7280)
            )

            val overdue = HousekeepingProgress.isOverdue(plan.isOverdue, plan.slaStatus)
            val slaColor = if (overdue) Color(0xFFB91C1C) else Color(0xFF166534)
            val slaBg = if (overdue) Color(0xFFFEE2E2) else Color(0xFFDCFCE7)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .background(slaBg, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = HousekeepingProgress.slaLabel(plan.area?.slaDays ?: plan.slaDays),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = slaColor
                )
                HousekeepingProgress.dueLabel(plan.dueDate)?.let {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(it, fontSize = 11.sp, color = slaColor)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = HousekeepingProgress.slaStatusLabel(plan.slaStatus),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = slaColor
                )
            }
        }
    }
}

/**
 * Satu card per fasilitas dengan alur kerja:
 *
 *   1. Upload foto before fasilitas.
 *   2. Tekan "Mulai Kerjakan" → seluruh checklist Pending jadi In Progress.
 *   3. Centang checklist yang sudah dikerjakan (belum dikirim ke server).
 *   4. Upload foto after fasilitas.
 *   5. Tekan "Selesaikan" → checklist yang dicentang dikirim sebagai Done.
 *
 * Checklist berstatus Done diletakkan paling bawah.
 */
@Composable
private fun HousekeepingFacilityTaskCard(
    group: HousekeepingFacilityTaskGroup,
    isSubmitting: Boolean,
    canExecute: Boolean,
    onUploadPhoto: (String) -> Unit,
    onStartAll: (List<HousekeepingTaskItem>) -> Unit,
    onFinishChecked: (List<HousekeepingTaskItem>) -> Unit
) {
    val done = group.tasks.count { it.status == "Done" }
    val total = group.tasks.size
    val hasBeforePhoto = group.planFacility?.photoBefore.orEmpty().isNotEmpty()
    val hasAfterPhoto = group.planFacility?.photoAfter.orEmpty().isNotEmpty()
    val hasPending = group.tasks.any { it.status == "Pending" }
    val hasInProgress = group.tasks.any { it.status == "In Progress" }

    // Checklist yang dicentang pengguna, menunggu tombol "Selesaikan".
    var checkedIds by remember(group.tasks) { mutableStateOf<Set<Int>>(emptySet()) }

    val orderedTasks = group.tasks.sortedBy { if (it.status == "Done") 1 else 0 }
    val checkedInProgress = orderedTasks.filter { it.status == "In Progress" && it.id in checkedIds }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CleaningServices,
                        contentDescription = null,
                        tint = Color(0xFF015E4E),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = group.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1F2937)
                    )
                }

                if (total > 0) {
                    Text(
                        text = "$done/$total",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (done == total) Color(0xFF16A34A) else Color(0xFF6B7280)
                    )
                }
            }

            if (group.tasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color(0xFFE5E7EB))

                orderedTasks.forEach { task ->
                    HousekeepingTaskCheckboxRow(
                        task = task,
                        isSubmitting = isSubmitting,
                        canExecute = canExecute,
                        checked = task.id in checkedIds,
                        onCheckedChange = { checked ->
                            checkedIds = if (checked) checkedIds + task.id else checkedIds - task.id
                        }
                    )
                }

                // Langkah 2: mulai seluruh checklist sekaligus.
                if (hasPending) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { onStartAll(group.tasks) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = canExecute && HousekeepingProgress.canStartAll(
                            hasBeforePhoto = group.planFacility == null || hasBeforePhoto,
                            hasPending = hasPending,
                            isSubmitting = isSubmitting,
                        ),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        Icon(Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mulai Kerjakan")
                    }

                    if (group.planFacility != null && !hasBeforePhoto) {
                        Text(
                            text = "Upload foto before fasilitas di bawah terlebih dahulu.",
                            fontSize = 11.sp,
                            color = Color(0xFFB91C1C),
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }

                // Langkah 5: selesaikan checklist yang dicentang.
                if (hasInProgress) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            onFinishChecked(checkedInProgress)
                            checkedIds = emptySet()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = canExecute && HousekeepingProgress.canFinishAll(
                            hasAfterPhoto = group.planFacility == null || hasAfterPhoto,
                            checkedCount = checkedInProgress.size,
                            isSubmitting = isSubmitting,
                        ),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (checkedInProgress.isEmpty()) {
                                "Selesaikan"
                            } else {
                                "Selesaikan (${checkedInProgress.size})"
                            }
                        )
                    }

                    if (checkedInProgress.isNotEmpty() && group.planFacility != null && !hasAfterPhoto) {
                        Text(
                            text = "Upload foto after fasilitas di bawah terlebih dahulu.",
                            fontSize = 11.sp,
                            color = Color(0xFFB91C1C),
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFFE5E7EB))
            Spacer(modifier = Modifier.height(10.dp))

            if (group.planFacility == null) {
                Text(
                    "Dokumentasi foto belum tersedia untuk fasilitas ini.",
                    fontSize = 11.sp,
                    color = Color(0xFF9CA3AF)
                )
            } else {
                PhotoSection(
                    label = "Foto Before",
                    photos = group.planFacility.photoBefore.orEmpty(),
                    onAdd = { onUploadPhoto("before") },
                    addEnabled = !isSubmitting && canExecute
                )

                Spacer(modifier = Modifier.height(8.dp))

                PhotoSection(
                    label = "Foto After",
                    photos = group.planFacility.photoAfter.orEmpty(),
                    onAdd = { onUploadPhoto("after") },
                    addEnabled = !isSubmitting && canExecute
                )
            }
        }
    }
}

/**
 * Card untuk checklist yang tidak terhubung ke fasilitas tertentu.
 * Tidak ada dokumentasi foto pada card ini (foto hanya di level fasilitas).
 */
@Composable
private fun HousekeepingLooseTasksCard(
    tasks: List<HousekeepingTaskItem>,
    isSubmitting: Boolean,
    canExecute: Boolean,
    onStartAll: (List<HousekeepingTaskItem>) -> Unit,
    onFinishChecked: (List<HousekeepingTaskItem>) -> Unit
) {
    val hasPending = tasks.any { it.status == "Pending" }
    val hasInProgress = tasks.any { it.status == "In Progress" }

    var checkedIds by remember(tasks) { mutableStateOf<Set<Int>>(emptySet()) }

    val orderedTasks = tasks.sortedBy { if (it.status == "Done") 1 else 0 }
    val checkedInProgress = orderedTasks.filter { it.status == "In Progress" && it.id in checkedIds }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Text(
                text = "Pekerjaan Tanpa Fasilitas",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1F2937)
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFFE5E7EB))

            orderedTasks.forEach { task ->
                HousekeepingTaskCheckboxRow(
                    task = task,
                    isSubmitting = isSubmitting,
                    canExecute = canExecute,
                    checked = task.id in checkedIds,
                    onCheckedChange = { checked ->
                        checkedIds = if (checked) checkedIds + task.id else checkedIds - task.id
                    }
                )
            }

            if (hasPending) {
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { onStartAll(tasks) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = canExecute && !isSubmitting,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Icon(Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mulai Kerjakan")
                }
            }

            if (hasInProgress) {
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        onFinishChecked(checkedInProgress)
                        checkedIds = emptySet()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = canExecute && !isSubmitting && checkedInProgress.isNotEmpty(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (checkedInProgress.isEmpty()) {
                            "Selesaikan"
                        } else {
                            "Selesaikan (${checkedInProgress.size})"
                        }
                    )
                }
            }
        }
    }
}

/**
 * Baris checklist dengan checkbox:
 * - Pending   : checkbox nonaktif (pekerjaan belum dimulai).
 * - In Progress: checkbox bisa dicentang, lalu tekan "Selesaikan".
 * - Done      : sudah final, tercentang, dan dipindahkan ke bawah.
 */
@Composable
private fun HousekeepingTaskCheckboxRow(
    task: HousekeepingTaskItem,
    isSubmitting: Boolean,
    canExecute: Boolean,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val isDone = task.status == "Done"
    val statusColor = when (task.status) {
        "Done" -> Color(0xFF16A34A)
        "In Progress" -> Color(0xFFD97706)
        else -> Color(0xFF6B7280)
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isDone || checked,
            enabled = canExecute && !isSubmitting && HousekeepingProgress.canCheckTask(task.status),
            onCheckedChange = { value -> onCheckedChange(value) },
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDone) Color(0xFF9CA3AF) else Color(0xFF1F2937)
            )
            Text(
                text = task.status,
                fontSize = 11.sp,
                color = statusColor
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PhotoSection(
    label: String,
    photos: List<String>,
    onAdd: () -> Unit,
    addEnabled: Boolean = true
) {
    var previewPath by remember { mutableStateOf<String?>(null) }

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
                    contentDescription = label,
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Fit
                )
            }
        )
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (photos.isEmpty()) label else "$label (${photos.size})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF374151)
            )
            if (photos.size < 5) {
                TextButton(onClick = onAdd, enabled = addEnabled) {
                    Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tambah", fontSize = 12.sp)
                }
            }
        }

        if (photos.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                photos.forEach { path ->
                    val model = if (path.startsWith("http")) path else "${RetrofitInstance.getStorageUrl()}/$path"
                    AsyncImage(
                        model = model,
                        contentDescription = label,
                        modifier = Modifier
                            .size(72.dp)
                            .clickable { previewPath = path }
                            .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        } else {
            Text("Belum ada foto", fontSize = 11.sp, color = Color(0xFF9CA3AF))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PhotoUploadDialog(
    title: String,
    existingPhotos: List<String>,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onUpload: (List<Uri>) -> Unit,
    onDelete: (String) -> Unit
) {
    val context = LocalContext.current
    var newPhotos by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var showSource by remember { mutableStateOf(false) }
    var previewPath by remember { mutableStateOf<String?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            newPhotos = newPhotos + uris
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            ImageFiles.bitmapToUri(context, it)?.let { uri -> newPhotos = newPhotos + uri }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted: Boolean ->
        if (granted) cameraLauncher.launch(null)
    }

    if (showSource) {
        AlertDialog(
            onDismissRequest = { showSource = false },
            title = { Text("Tambah Foto") },
            text = { Text("Ambil dari kamera atau pilih dari galeri?") },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {
                        showSource = false
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Kamera")
                    }
                    Button(
                        onClick = {
                            showSource = false
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
                TextButton(onClick = { showSource = false }) { Text("Batal") }
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
                    contentDescription = title,
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Fit
                )
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (existingPhotos.isNotEmpty()) {
                    Text("Foto tersimpan (${existingPhotos.size})", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF374151))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        existingPhotos.forEach { path ->
                            val model = if (path.startsWith("http")) path else "${RetrofitInstance.getStorageUrl()}/$path"
                            Box {
                                AsyncImage(
                                    model = model,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
                                        .clickable { previewPath = path },
                                    contentScale = ContentScale.Crop
                                )
                                IconButton(
                                    onClick = { onDelete(path) },
                                    modifier = Modifier
                                        .size(20.dp)
                                        .align(Alignment.TopEnd)
                                        .background(Color(0xFFDC2626), RoundedCornerShape(999.dp))
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Hapus",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                if (newPhotos.isNotEmpty()) {
                    Text("Foto baru (${newPhotos.size})", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF374151))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        newPhotos.forEach { uri ->
                            Box {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                IconButton(
                                    onClick = { newPhotos = newPhotos - uri },
                                    modifier = Modifier
                                        .size(20.dp)
                                        .align(Alignment.TopEnd)
                                        .background(Color(0xFFDC2626), RoundedCornerShape(999.dp))
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Hapus",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                OutlinedButton(onClick = { showSource = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Pilih Foto")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onUpload(newPhotos) },
                enabled = newPhotos.isNotEmpty() && !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF015E4E))
            ) {
                Text(if (isSubmitting) "Mengunggah..." else "Unggah")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal", color = Color(0xFF6B7280)) }
        }
    )
}
