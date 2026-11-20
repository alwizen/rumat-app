package com.solu8i.cmms.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
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

data class DrawerItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

val primaryGreen = Color(0xFF015E4E)

val bottomNavItems = listOf(
    DrawerItem(Routes.DASHBOARD, "Home", Icons.Default.Home),
    DrawerItem(Routes.CALENDAR, "Kalender", Icons.Default.CalendarMonth),
    DrawerItem(Routes.HISTORY, "Work Order", Icons.Default.List),
    DrawerItem(Routes.MAINTENANCE_REQUEST, "Request", Icons.Default.Assignment)
)

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar(containerColor = Color.White) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route

            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = primaryGreen,
                    selectedTextColor = primaryGreen,
                    indicatorColor = primaryGreen.copy(alpha = 0.12f),
                    unselectedIconColor = Color(0xFF6B7280),
                    unselectedTextColor = Color(0xFF6B7280)
                )
            )
        }
    }
}

val drawerItems = listOf(
    DrawerItem(Routes.DASHBOARD, "Dashboard", Icons.Default.Home),
    DrawerItem(Routes.HISTORY, "Work Order", Icons.Default.List),
    DrawerItem(Routes.CALENDAR, "Kalender", Icons.Default.CalendarMonth),
    DrawerItem(Routes.EQUIPMENT, "Equipment", Icons.Default.Build),
    DrawerItem(Routes.MAINTENANCE_REQUEST, "Request", Icons.Default.Assignment),
    DrawerItem(Routes.DOWNTIME, "Downtime", Icons.Default.Timer),
    DrawerItem(Routes.DOCUMENT, "Dokumen", Icons.Default.Description),
    DrawerItem(Routes.MY_TAB, "Profil", Icons.Default.Person)
)

/**
 * Menu khusus untuk user dengan role housekeeping: petugas (`patra-jasa`)
 * dan supervisor housekeeping (`spv_gos`). Keduanya hanya melihat modul
 * housekeeping.
 */
val housekeepingDrawerItems = listOf(
    DrawerItem(Routes.HOUSEKEEPING_DASHBOARD, "Dashboard", Icons.Default.Home),
    DrawerItem(Routes.HOUSEKEEPING, "Housekeeping", Icons.Default.CleaningServices),
    DrawerItem(Routes.MY_TAB, "Profil", Icons.Default.Person)
)

const val HOUSEKEEPING_ROLE = "patra-jasa"

/** Supervisor housekeeping (General Office Services). */
const val HOUSEKEEPING_SUPERVISOR_ROLE = "spv_gos"

val HOUSEKEEPING_ROLES = listOf(HOUSEKEEPING_ROLE, HOUSEKEEPING_SUPERVISOR_ROLE)

fun isHousekeepingRole(userRole: String?): Boolean {
    return userRole
        ?.split(",")
        ?.map { it.trim() }
        ?.any { it in HOUSEKEEPING_ROLES } == true
}

/**
 * Hanya petugas housekeeping (`patra-jasa`) yang mengeksekusi checklist.
 * SPV GOS membuat plan dan memverifikasi hasil pekerjaan.
 */
fun canExecuteHousekeeping(userRole: String?): Boolean {
    return userRole
        ?.split(",")
        ?.map { it.trim() }
        ?.any { it == HOUSEKEEPING_ROLE } == true
}

/** SPV GOS: pembuat plan & penyetuju laporan harian. */
fun isHousekeepingSupervisor(userRole: String?): Boolean {
    return userRole
        ?.split(",")
        ?.map { it.trim() }
        ?.any { it == HOUSEKEEPING_SUPERVISOR_ROLE } == true
}

/**
 * Menu khusus untuk user dengan role pengawas.
 * Pengawas hanya memiliki 2 menu: Request Maintenance dan Request Housekeeping.
 */
val pengawasDrawerItems = listOf(
    DrawerItem(Routes.MAINTENANCE_REQUEST, "Request Maintenance", Icons.Default.Assignment),
    DrawerItem(Routes.HOUSEKEEPING_REQUEST, "Request Housekeeping", Icons.Default.CleaningServices)
)

const val PENGAWAS_ROLE = "pengawas"

fun isPengawasRole(userRole: String?): Boolean {
    return userRole
        ?.split(",")
        ?.map { it.trim() }
        ?.any { it == PENGAWAS_ROLE } == true
}

fun drawerItemsForRole(userRole: String?): List<DrawerItem> {
    return when {
        isHousekeepingRole(userRole) -> housekeepingDrawerItems
        isPengawasRole(userRole) -> pengawasDrawerItems
        else -> drawerItems
    }
}

@Composable
fun AppDrawerContent(
    currentRoute: String?,
    userName: String?,
    userRole: String?,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit = {}
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Keluar", fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin keluar dari aplikasi?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Keluar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Batal", color = Color(0xFF6B7280))
                }
            }
        )
    }

    ModalDrawerSheet(
        drawerContainerColor = Color.White,
        modifier = Modifier
            .width(290.dp)
            .fillMaxHeight()
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
        ) {
            DrawerHeader(userName = userName, userRole = userRole)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "MENU",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF9CA3AF),
                modifier = Modifier.padding(start = 24.dp, bottom = 6.dp)
            )

            drawerItemsForRole(userRole).forEach { item ->
                val selected = currentRoute == item.route

                NavigationDrawerItem(
                    label = {
                        Text(
                            text = item.label,
                            fontSize = 14.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    selected = selected,
                    onClick = { onNavigate(item.route) },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = primaryGreen.copy(alpha = 0.10f),
                        selectedIconColor = primaryGreen,
                        selectedTextColor = primaryGreen,
                        unselectedIconColor = Color(0xFF6B7280),
                        unselectedTextColor = Color(0xFF374151)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                color = Color(0xFFE5E7EB)
            )

            Spacer(modifier = Modifier.height(4.dp))

            NavigationDrawerItem(
                label = {
                    Text(
                        text = "Keluar",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFDC2626)
                    )
                },
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Keluar",
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(20.dp)
                    )
                },
                selected = false,
                onClick = {
                    showLogoutDialog = true
                },
                colors = NavigationDrawerItemDefaults.colors(
                    unselectedIconColor = Color(0xFFDC2626),
                    unselectedTextColor = Color(0xFFDC2626)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DrawerHeader(userName: String?, userRole: String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(primaryGreen)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = userName?.takeIf { it.isNotBlank() } ?: "Pengguna",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(2.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = userRole?.replace(",", ", ")?.takeIf { it.isNotBlank() } ?: "—",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.75f)
                )
            }
        }
    }
}
