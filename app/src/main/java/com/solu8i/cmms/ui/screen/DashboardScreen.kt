package com.solu8i.cmms.ui.screen

import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solu8i.cmms.data.model.RecentWorkOrder
import com.solu8i.cmms.ui.navigation.Routes
import com.solu8i.cmms.ui.navigation.drawerItems
import com.solu8i.cmms.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    loginViewModel: com.solu8i.cmms.ui.viewmodel.LoginViewModel,
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp,
    onWorkOrderClick: (Int) -> Unit = {},
    onMenuClick: () -> Unit = {},
    unreadNotificationCount: Int = 0,
    onNotificationClick: () -> Unit = {},
    onLogout: () -> Unit = {},
    onNavigate: (String) -> Unit = {}
) {
    val dashboardData by viewModel.dashboardData.collectAsState()
    val isLoading by viewModel.isDashboardLoading.collectAsState()
    val userName by loginViewModel.tokenManager.userName.collectAsState(initial = null)
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadDashboard()
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = Color(0xFFDC2626)
                )
            },
            title = { Text("Keluar Akun") },
            text = { Text("Apakah Anda yakin ingin keluar dari akun ini?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogout()
                }) {
                    Text("Keluar", color = Color(0xFFDC2626), fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Batal", color = Color(0xFF6B7280))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Buka menu",
                            tint = Color.White
                        )
                    }
                },

                title = {
                    Text(
                        text = "Dashboard",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    Box(
                        modifier = Modifier.padding(end = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = onNotificationClick) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifikasi",
                                tint = Color.White
                            )
                        }

                        if (unreadNotificationCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 2.dp, y = 4.dp)
                                    .defaultMinSize(minWidth = 18.dp, minHeight = 18.dp)
                                    .background(Color(0xFFDC2626), CircleShape)
                                    .padding(horizontal = 5.dp, vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (unreadNotificationCount > 99) "99+" else unreadNotificationCount.toString(),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    lineHeight = 12.sp
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Logout",
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
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF015E4E))
            }
        } else if (dashboardData == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Gagal memuat data", color = Color(0xFF9CA3AF))
            }
        } else {
            val data = dashboardData!!
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5F6FA))
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = bottomPadding + 16.dp)
            ) {
                item {
                    val completion = if (data.stats.totalWorkOrders > 0) {
                        (data.stats.completed * 100) / data.stats.totalWorkOrders
                    } else {
                        0
                    }

                    SummaryCard(
                        userName = userName,
                        total = data.stats.totalWorkOrders,
                        completed = data.stats.completed,
                        completionPercent = completion
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatChip(
                            modifier = Modifier.weight(1f),
                            label = "Berjalan",
                            value = data.stats.inProgress,
                            tint = Color(0xFF2563EB),
                            icon = Icons.Default.PlayCircle
                        )
                        StatChip(
                            modifier = Modifier.weight(1f),
                            label = "Pending",
                            value = data.stats.pending,
                            tint = Color(0xFFD97706),
                            icon = Icons.Default.HourglassEmpty
                        )
                        StatChip(
                            modifier = Modifier.weight(1f),
                            label = "Terlambat",
                            value = data.stats.overdue,
                            tint = Color(0xFFDC2626),
                            icon = Icons.Default.Warning
                        )
                    }
                }

                item {
                    Text(
                        text = "Menu",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF015E4E)
                    )
                }

                item {
                    MenuGrid(onNavigate = onNavigate)
                }

                val todayWorkOrders = data.todayWorkOrders.orEmpty()

                if (todayWorkOrders.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Agenda Hari Ini",
                            subtitle = "${todayWorkOrders.size} work order dijadwalkan"
                        )
                    }

                    items(todayWorkOrders) { wo ->
                        WorkOrderCard(
                            wo = wo,
                            onClick = { onWorkOrderClick(wo.id) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Work Order Terbaru",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF015E4E)
                    )
                }

                if (data.recentWorkOrders.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Text(
                                text = "Belum ada work order",
                                modifier = Modifier.padding(20.dp),
                                color = Color(0xFF9CA3AF),
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    items(data.recentWorkOrders) { wo ->
                        WorkOrderCard(
                            wo = wo,
                            onClick = { onWorkOrderClick(wo.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MenuGrid(onNavigate: (String) -> Unit) {
    val menus = drawerItems.filter { it.route != Routes.DASHBOARD }
    val columns = 4

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        menus.chunked(columns).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { menu ->
                    MenuGridItem(
                        label = menu.label,
                        icon = menu.icon,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(menu.route) }
                    )
                }

                repeat(columns - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MenuGridItem(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(Color(0xFF015E4E).copy(alpha = 0.10f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFF015E4E),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFF374151),
            maxLines = 1
        )
    }
}

@Composable
fun SummaryCard(
    userName: String?,
    total: Int,
    completed: Int,
    completionPercent: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF015E4E), Color(0xFF0B7A63))
                    )
                )
                .padding(18.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Ringkasan",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = userName?.takeIf { it.isNotBlank() }?.let { "Halo, $it" } ?: "Halo",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color.White.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$total",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "total work order",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.75f),
                        modifier = Modifier.padding(bottom = 5.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(3.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = (completionPercent / 100f).coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .background(Color(0xFF9CB080), RoundedCornerShape(3.dp))
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$completed selesai",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                    Text(
                        text = "$completionPercent%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun StatChip(
    modifier: Modifier = Modifier,
    label: String,
    value: Int,
    tint: Color,
    icon: ImageVector
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = tint.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .background(tint.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(15.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "$value",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = tint
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = label,
                fontSize = 11.sp,
                color = Color(0xFF6B7280)
            )
        }
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String? = null) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF015E4E)
        )
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Color(0xFF9CA3AF)
            )
        }
    }
}

@Composable
fun WorkOrderCard(wo: RecentWorkOrder, onClick: () -> Unit = {}) {
    val statusColor = when (wo.status) {
        "Completed" -> Color(0xFF16A34A)
        "In Progress" -> Color(0xFF2563EB)
        "On Hold" -> Color(0xFFD97706)
        "Cancelled" -> Color(0xFFDC2626)
        else -> Color(0xFF6B7280)
    }

    Card(
        onClick = onClick,
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
                    .size(8.dp)
                    .background(statusColor, RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = wo.workOrderNumber,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1F2937)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = wo.equipment?.let { "${it.tagNumber} - ${it.equipmentType}" } ?: "-",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .background(
                            statusColor.copy(alpha = 0.1f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = wo.status,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = statusColor
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = wo.classification,
                    fontSize = 11.sp,
                    color = Color(0xFF9CA3AF)
                )
            }
        }
    }
}
