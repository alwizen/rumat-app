package com.solu8i.cmms

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.solu8i.cmms.ui.navigation.AppDrawerContent
import com.solu8i.cmms.ui.navigation.Routes
import com.solu8i.cmms.ui.navigation.canExecuteHousekeeping
import com.solu8i.cmms.ui.navigation.isHousekeepingRole
import com.solu8i.cmms.ui.navigation.PENGAWAS_ROLE
import com.solu8i.cmms.ui.screen.CalendarScreen
import com.solu8i.cmms.ui.screen.EquipmentDetailScreen
import com.solu8i.cmms.ui.screen.EquipmentListScreen
import com.solu8i.cmms.ui.screen.DashboardScreen
import com.solu8i.cmms.ui.screen.DocumentDetailScreen
import com.solu8i.cmms.ui.screen.DocumentScreen
import com.solu8i.cmms.ui.screen.NotificationScreen
import com.solu8i.cmms.ui.viewmodel.NotificationViewModel
import com.solu8i.cmms.ui.screen.DowntimeScreen
import com.solu8i.cmms.ui.viewmodel.DocumentDetailViewModel
import com.solu8i.cmms.ui.viewmodel.DocumentViewModel
import com.solu8i.cmms.ui.viewmodel.DowntimeViewModel
import com.solu8i.cmms.ui.screen.HistoryScreen
import com.solu8i.cmms.ui.screen.HousekeepingDetailScreen
import com.solu8i.cmms.ui.screen.HousekeepingDashboardScreen
import com.solu8i.cmms.ui.screen.HousekeepingListScreen
import com.solu8i.cmms.ui.screen.HousekeepingRequestCreateScreen
import com.solu8i.cmms.ui.screen.HousekeepingRequestDetailScreen
import com.solu8i.cmms.ui.screen.HousekeepingRequestListScreen
import com.solu8i.cmms.ui.screen.LoginScreen
import com.solu8i.cmms.ui.screen.MaintenanceRequestCreateScreen
import com.solu8i.cmms.ui.screen.MaintenanceRequestDetailScreen
import com.solu8i.cmms.ui.screen.MaintenanceRequestListScreen
import com.solu8i.cmms.ui.screen.MyTabScreen
import com.solu8i.cmms.ui.screen.WorkOrderDetailScreen
import com.solu8i.cmms.ui.theme.CMMSTheme
import com.solu8i.cmms.ui.viewmodel.CalendarViewModel
import com.solu8i.cmms.ui.viewmodel.EquipmentViewModel
import com.solu8i.cmms.ui.viewmodel.HousekeepingDetailViewModel
import com.solu8i.cmms.ui.viewmodel.HousekeepingRequestViewModel
import com.solu8i.cmms.ui.viewmodel.HousekeepingDashboardViewModel
import com.solu8i.cmms.ui.viewmodel.HousekeepingViewModel
import com.solu8i.cmms.ui.viewmodel.LoginViewModel
import com.solu8i.cmms.ui.viewmodel.MaintenanceRequestViewModel
import com.solu8i.cmms.ui.viewmodel.MainViewModel
import com.solu8i.cmms.ui.viewmodel.WorkOrderDetailViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CMMSTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val loginViewModel: LoginViewModel = viewModel()
    val mainViewModel: MainViewModel = viewModel()
    val maintenanceRequestViewModel: MaintenanceRequestViewModel = viewModel()
    val calendarViewModel: CalendarViewModel = viewModel()
    val equipmentViewModel: EquipmentViewModel = viewModel()
    val downtimeViewModel: DowntimeViewModel = viewModel()
    val notificationViewModel: NotificationViewModel = viewModel()
    val documentViewModel: DocumentViewModel = viewModel()
    val documentDetailViewModel: DocumentDetailViewModel = viewModel()
    val housekeepingViewModel: HousekeepingViewModel = viewModel()
    val housekeepingDashboardViewModel: HousekeepingDashboardViewModel = viewModel()
    val housekeepingRequestViewModel: HousekeepingRequestViewModel = viewModel()
    val uiState by loginViewModel.uiState.collectAsState()
    val navController = rememberNavController()

    val drawerUserName by loginViewModel.tokenManager.userName.collectAsState(initial = null)
    val drawerUserRole by loginViewModel.tokenManager.userRoles.collectAsState(initial = null)

    val startDestination = remember(uiState.isSessionLoaded) {
        when {
            !uiState.isLoggedIn -> Routes.LOGIN
            uiState.userRoles.any { isHousekeepingRole(it) } -> Routes.HOUSEKEEPING_DASHBOARD
            uiState.userRoles.any { it.equals(PENGAWAS_ROLE, ignoreCase = true) } -> Routes.MAINTENANCE_REQUEST
            else -> Routes.DASHBOARD
        }
    }

    if (!uiState.isSessionLoaded) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF015E4E))
        }

        return
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.isLoggedIn) {
        if (!uiState.isLoggedIn) {
            navController.navigate(Routes.LOGIN) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    val openDrawer: () -> Unit = {
        scope.launch { drawerState.open() }
    }

    val closeDrawer: () -> Unit = {
        scope.launch { drawerState.close() }
    }

    val performLogout: () -> Unit = {
        mainViewModel.reset()
        maintenanceRequestViewModel.reset()
        calendarViewModel.reset()
        equipmentViewModel.reset()
        downtimeViewModel.reset()
        notificationViewModel.reset()
        documentViewModel.reset()
        documentDetailViewModel.reset()
        housekeepingViewModel.reset()
        housekeepingDashboardViewModel.reset()
        housekeepingRequestViewModel.reset()
        loginViewModel.logout()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                currentRoute = currentRoute,
                userName = drawerUserName,
                userRole = drawerUserRole,
                onLogout = {
                    closeDrawer()
                    performLogout()
                },
                onNavigate = { route ->
                    closeDrawer()
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = false
                            saveState = false
                        }
                        launchSingleTop = true
                        restoreState = false
                    }
                }
            )
        }
    ) {
        Scaffold { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = startDestination
            ) {
            composable(Routes.LOGIN) {
                LoginScreen(
                    viewModel = loginViewModel,
                    onLoginSuccess = {
                        val destination = when {
                            uiState.userRoles.any { isHousekeepingRole(it) } -> Routes.HOUSEKEEPING_DASHBOARD
                            uiState.userRoles.any {
                                it.equals(PENGAWAS_ROLE, ignoreCase = true)
                            } -> Routes.MAINTENANCE_REQUEST
                            else -> Routes.DASHBOARD
                        }
                        navController.navigate(destination) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.DASHBOARD) {
                val unreadCount by notificationViewModel.unreadCount.collectAsState()

                LaunchedEffect(Unit) {
                    notificationViewModel.refreshUnreadCount()
                }

                DashboardScreen(
                    viewModel = mainViewModel,
                    loginViewModel = loginViewModel,
                    bottomPadding = innerPadding.calculateBottomPadding(),
                    onWorkOrderClick = { id ->
                        navController.navigate(Routes.workOrderDetail(id))
                    },
                    unreadNotificationCount = unreadCount,
                    onNotificationClick = {
                        navController.navigate(Routes.NOTIFICATION)
                    },
                    onLogout = performLogout,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            launchSingleTop = true
                        }
                    },
                    onMenuClick = openDrawer
                )
            }

            composable(Routes.HISTORY) {
                HistoryScreen(
                    viewModel = mainViewModel,
                    onWorkOrderClick = { id ->
                        navController.navigate(Routes.workOrderDetail(id))
                    },
                    bottomPadding = innerPadding.calculateBottomPadding(),
                    onMenuClick = openDrawer
                )
            }

            composable(Routes.CALENDAR) {
                CalendarScreen(
                    viewModel = calendarViewModel,
                    bottomPadding = innerPadding.calculateBottomPadding(),
                    onMenuClick = openDrawer
                )
            }

            composable(Routes.EQUIPMENT) {
                EquipmentListScreen(
                    viewModel = equipmentViewModel,
                    onEquipmentClick = { id ->
                        navController.navigate(Routes.equipmentDetail(id))
                    },
                    bottomPadding = innerPadding.calculateBottomPadding(),
                    onMenuClick = openDrawer
                )
            }

            composable(Routes.EQUIPMENT_DETAIL) { backStackEntry ->
                val equipmentId = backStackEntry.arguments?.getString("equipmentId")?.toIntOrNull() ?: 0

                EquipmentDetailScreen(
                    equipmentId = equipmentId,
                    viewModel = equipmentViewModel,
                    onBack = { navController.popBackStack() },
                    bottomPadding = innerPadding.calculateBottomPadding()
                )
            }

            composable(Routes.WORK_ORDER_DETAIL) { backStackEntry ->
                val workOrderId = backStackEntry.arguments?.getString("workOrderId")?.toIntOrNull() ?: 0
                val detailViewModel: WorkOrderDetailViewModel = viewModel()
                WorkOrderDetailScreen(
                    workOrderId = workOrderId,
                    viewModel = detailViewModel,
                    onBack = { navController.popBackStack() },
                    bottomPadding = innerPadding.calculateBottomPadding()
                )
            }

            composable(Routes.MAINTENANCE_REQUEST) {
                MaintenanceRequestListScreen(
                    viewModel = maintenanceRequestViewModel,
                    onRequestClick = { id ->
                        navController.navigate(Routes.maintenanceRequestDetail(id))
                    },
                    onCreateClick = {
                        navController.navigate(Routes.MAINTENANCE_REQUEST_CREATE)
                    },
                    bottomPadding = innerPadding.calculateBottomPadding(),
                    onMenuClick = openDrawer
                )
            }

            composable(Routes.MAINTENANCE_REQUEST_CREATE) {
                MaintenanceRequestCreateScreen(
                    viewModel = maintenanceRequestViewModel,
                    onBack = { navController.popBackStack() },
                    onSuccess = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Routes.MAINTENANCE_REQUEST_DETAIL) { backStackEntry ->
                val requestId = backStackEntry.arguments?.getString("requestId")?.toIntOrNull() ?: 0
                MaintenanceRequestDetailScreen(
                    requestId = requestId,
                    viewModel = maintenanceRequestViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.NOTIFICATION) {
                NotificationScreen(
                    viewModel = notificationViewModel,
                    bottomPadding = innerPadding.calculateBottomPadding(),
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.DOWNTIME) {
                DowntimeScreen(
                    viewModel = downtimeViewModel,
                    bottomPadding = innerPadding.calculateBottomPadding(),
                    onMenuClick = openDrawer
                )
            }

            composable(Routes.DOCUMENT) {
                DocumentScreen(
                    viewModel = documentViewModel,
                    bottomPadding = innerPadding.calculateBottomPadding(),
                    onMenuClick = openDrawer,
                    onDocumentClick = { id ->
                        navController.navigate(Routes.documentDetail(id))
                    }
                )
            }

            composable(Routes.DOCUMENT_DETAIL) { entry ->
                val documentId = entry.arguments?.getString("documentId")?.toIntOrNull() ?: 0

                DocumentDetailScreen(
                    documentId = documentId,
                    viewModel = documentDetailViewModel,
                    bottomPadding = innerPadding.calculateBottomPadding(),
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.MY_TAB) {
                MyTabScreen(
                    loginViewModel = loginViewModel,
                    onMenuClick = openDrawer
                )
            }

            composable(Routes.HOUSEKEEPING_DASHBOARD) {
                HousekeepingDashboardScreen(
                    dashboardViewModel = housekeepingDashboardViewModel,
                    housekeepingViewModel = housekeepingViewModel,
                    userName = drawerUserName,
                    onPlanClick = { id ->
                        navController.navigate(Routes.housekeepingDetail(id))
                    },
                    onOpenPlans = { navController.navigate(Routes.HOUSEKEEPING) },
                    onOpenProfile = { navController.navigate(Routes.MY_TAB) },
                    onMenuClick = openDrawer,
                    bottomPadding = innerPadding.calculateBottomPadding(),
                )
            }

            composable(Routes.HOUSEKEEPING) {
                HousekeepingListScreen(
                    viewModel = housekeepingViewModel,
                    onPlanClick = { id ->
                        navController.navigate(Routes.housekeepingDetail(id))
                    },
                    bottomPadding = innerPadding.calculateBottomPadding(),
                    onMenuClick = openDrawer
                )
            }

            composable(Routes.HOUSEKEEPING_DETAIL) { backStackEntry ->
                val planId = backStackEntry.arguments?.getString("planId")?.toIntOrNull() ?: 0
                val detailViewModel: HousekeepingDetailViewModel = viewModel()
                HousekeepingDetailScreen(
                    planId = planId,
                    viewModel = detailViewModel,
                    onBack = { navController.popBackStack() },
                    bottomPadding = innerPadding.calculateBottomPadding(),
                    // SPV GOS hanya membuat plan & menyetujui laporan;
                    // eksekusi checklist milik patra-jasa.
                    canExecute = canExecuteHousekeeping(uiState.userRoles.joinToString(",")),
                )
            }

            composable(Routes.HOUSEKEEPING_REQUEST) {
                HousekeepingRequestListScreen(
                    viewModel = housekeepingRequestViewModel,
                    onRequestClick = { id ->
                        navController.navigate(Routes.housekeepingRequestDetail(id))
                    },
                    onCreateClick = {
                        navController.navigate(Routes.HOUSEKEEPING_REQUEST_CREATE)
                    },
                    bottomPadding = innerPadding.calculateBottomPadding(),
                    onMenuClick = openDrawer
                )
            }

            composable(Routes.HOUSEKEEPING_REQUEST_CREATE) {
                HousekeepingRequestCreateScreen(
                    viewModel = housekeepingRequestViewModel,
                    onBack = { navController.popBackStack() },
                    onSuccess = { navController.popBackStack() }
                )
            }

            composable(Routes.HOUSEKEEPING_REQUEST_DETAIL) { backStackEntry ->
                val requestId = backStackEntry.arguments?.getString("requestId")?.toIntOrNull() ?: 0
                HousekeepingRequestDetailScreen(
                    requestId = requestId,
                    viewModel = housekeepingRequestViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
    }
}
