package com.solu8i.cmms.ui.screen

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.solu8i.cmms.data.model.DocumentItem
import com.solu8i.cmms.ui.viewmodel.DocumentDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentDetailScreen(
    documentId: Int,
    viewModel: DocumentDetailViewModel,
    bottomPadding: Dp = 0.dp,
    onBack: () -> Unit = {}
) {
    val document by viewModel.document.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(documentId) {
        viewModel.loadDetail(documentId)
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
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }
                },
                title = {
                    Text(
                        text = "Detail Dokumen",
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF015E4E),
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F6FA))
                .padding(innerPadding)
        ) {
            val current = document

            when {
                isLoading && current == null -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF015E4E))
                }

                current == null -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Dokumen tidak ditemukan", color = Color(0xFF9CA3AF), fontSize = 13.sp)
                }

                else -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DocumentPreview(document = current)

                    DocumentInfoCard(document = current)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                openFile(context, current.fileUrl)
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !current.fileUrl.isNullOrBlank(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Default.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Color(0xFF015E4E)
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("Buka", color = Color(0xFF015E4E), fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                val url = current.fileUrl
                                if (url.isNullOrBlank()) {
                                    Toast.makeText(context, "File tidak tersedia", Toast.LENGTH_SHORT).show()
                                } else {
                                    downloadFile(context, url, current.title)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !current.fileUrl.isNullOrBlank(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF015E4E))
                        ) {
                            Icon(
                                Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("Download", fontSize = 13.sp, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(bottomPadding))
                }
            }
        }
    }
}

@Composable
private fun DocumentPreview(document: DocumentItem) {
    val url = document.fileUrl

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Pratinjau",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF015E4E)
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (url.isNullOrBlank()) {
                EmptyPreview()
            } else if (isImage(url)) {
                AsyncImage(
                    model = url,
                    contentDescription = document.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(Color(0xFFF3F4F6), RoundedCornerShape(10.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(Color(0xFFF3F4F6), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            modifier = Modifier.size(42.dp),
                            tint = Color(0xFFDC2626)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Pratinjau PDF tidak tersedia di aplikasi.\nGunakan tombol Buka atau Download.",
                            fontSize = 11.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyPreview() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(Color(0xFFF3F4F6), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text("File tidak tersedia", fontSize = 12.sp, color = Color(0xFF9CA3AF))
    }
}

@Composable
private fun DocumentInfoCard(document: DocumentItem) {
    val accent = when (document.expiryStatus) {
        "expired" -> Color(0xFFDC2626)
        "expiring_soon" -> Color(0xFFF59E0B)
        "valid" -> Color(0xFF15803D)
        else -> Color(0xFF6B7280)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = document.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = document.documentTypeLabel ?: "-",
                fontSize = 12.sp,
                color = Color(0xFF6B7280)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF3F4F6))

            DocumentInfoRow("Nomor", document.documentNumber ?: "-")
            DocumentInfoRow("Equipment", document.equipment?.tagNumber ?: "-")
            DocumentInfoRow("Revisi", document.revision ?: "-")
            DocumentInfoRow("Issuer", document.issuer ?: "-")
            DocumentInfoRow("Tanggal Terbit", document.issueDate ?: "-")
            DocumentInfoRow("Tanggal Expired", document.expiryDate ?: "-")

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Status Expiry",
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = document.expiryStatusLabel ?: "-",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = accent
                )
            }

            if (!document.description.isNullOrBlank()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF3F4F6))
                Text(
                    text = "Deskripsi",
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF)
                )
                Text(
                    text = document.description,
                    fontSize = 13.sp,
                    color = Color(0xFF374151)
                )
            }
        }
    }
}

@Composable
private fun DocumentInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF9CA3AF),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            color = Color(0xFF374151),
            fontWeight = FontWeight.Medium
        )
    }
}

private fun isImage(url: String): Boolean {
    val path = url.substringBefore('?').lowercase()
    return path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".jpeg") || path.endsWith(".webp")
}

private fun openFile(context: Context, url: String?) {
    if (url.isNullOrBlank()) {
        Toast.makeText(context, "File tidak tersedia", Toast.LENGTH_SHORT).show()
        return
    }

    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Tidak ada aplikasi untuk membuka file ini", Toast.LENGTH_SHORT).show()
    }
}

private fun downloadFile(context: Context, url: String, title: String) {
    try {
        val extension = url.substringBefore('?').substringAfterLast('.', "pdf")
        val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "application/octet-stream"

        val request = android.app.DownloadManager.Request(Uri.parse(url))
            .setTitle(title)
            .setDescription("Mengunduh dokumen")
            .setNotificationVisibility(
                android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            )
            .setDestinationInExternalPublicDir(
                android.os.Environment.DIRECTORY_DOWNLOADS,
                "$title.$extension"
            )
            .setMimeType(mime)

        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as android.app.DownloadManager
        manager.enqueue(request)

        Toast.makeText(context, "Download dimulai", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Gagal memulai download", Toast.LENGTH_SHORT).show()
    }
}
