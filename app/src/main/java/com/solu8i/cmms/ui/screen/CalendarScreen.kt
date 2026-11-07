package com.solu8i.cmms.ui.screen

import androidx.compose.material.icons.filled.Menu
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solu8i.cmms.data.model.CalendarEvent
import com.solu8i.cmms.ui.util.CalendarGrid
import com.solu8i.cmms.ui.viewmodel.CalendarViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val PrimaryGreen = Color(0xFF015E4E)
private val BackgroundGray = Color(0xFFF5F6FA)
private val MutedText = Color(0xFF9CA3AF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp,
    onMenuClick: () -> Unit = {}
) {
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.reload()
    }

    val events by viewModel.events.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val visibleMonth by viewModel.visibleMonth.collectAsState()

    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))

    val eventsByDate = events
        .filter { it.date != null }
        .groupBy { it.date!! }

    val selectedEvents = selectedDate?.let { eventsByDate[it] }.orEmpty()

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

                title = { Text("Kalender Maintenance", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { viewModel.reload() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Muat ulang")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryGreen,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundGray)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                top = 12.dp,
                bottom = bottomPadding + 24.dp
            )
        ) {
            item {
                MonthHeader(
                    title = monthFormat.format(visibleMonth.time),
                    onPrevious = { viewModel.previousMonth() },
                    onNext = { viewModel.nextMonth() },
                    onToday = { viewModel.goToToday() }
                )
            }

            item {
                WeekdayHeader()
            }

            val cells = CalendarGrid.build(visibleMonth)

            items(cells.chunked(7)) { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    week.forEach { cell ->
                        DayCell(
                            modifier = Modifier.weight(1f),
                            dayNumber = cell.dayOfMonth,
                            inCurrentMonth = cell.inCurrentMonth,
                            isToday = cell.isToday,
                            isSelected = selectedDate == cell.key,
                            hasEvents = eventsByDate.containsKey(cell.key),
                            hasWorkOrder = eventsByDate[cell.key]
                                .orEmpty()
                                .any { it.isWorkOrder },
                            onClick = { viewModel.selectDate(cell.key) }
                        )
                    }

                    repeat(7 - week.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Agenda ${formatAgendaDate(selectedDate)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
            }

            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryGreen, strokeWidth = 2.dp)
                    }
                }
            } else if (errorMessage != null) {
                item {
                    EmptyCard(message = errorMessage ?: "Gagal memuat data")
                }
            } else if (selectedEvents.isEmpty()) {
                item {
                    EmptyCard(message = "Tidak ada jadwal pada tanggal ini")
                }
            } else {
                items(selectedEvents.sortedBy { it.time ?: "99:99" }) { event ->
                    EventCard(event)
                }
            }
        }
    }
}

@Composable
private fun MonthHeader(
    title: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevious) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Bulan sebelumnya",
                    tint = PrimaryGreen
                )
            }

            Text(
                text = title,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )

            IconButton(onClick = onNext) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Bulan berikutnya",
                    tint = PrimaryGreen
                )
            }

            TextButton(onClick = onToday) {
                Text("Hari ini", fontSize = 12.sp, color = PrimaryGreen)
            }
        }
    }
}

@Composable
private fun WeekdayHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min").forEach { label ->
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MutedText
            )
        }
    }
}

@Composable
private fun DayCell(
    modifier: Modifier = Modifier,
    dayNumber: Int,
    inCurrentMonth: Boolean,
    isToday: Boolean,
    isSelected: Boolean,
    hasEvents: Boolean,
    hasWorkOrder: Boolean,
    onClick: () -> Unit
) {
    val background = when {
        isSelected -> PrimaryGreen
        isToday -> PrimaryGreen.copy(alpha = 0.12f)
        else -> Color.White
    }

    val textColor = when {
        isSelected -> Color.White
        !inCurrentMonth -> Color(0xFFCBD5E1)
        else -> Color(0xFF1F2937)
    }

    Box(
        modifier = modifier
            .aspectRatio(0.85f)
            .background(background, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = dayNumber.toString(),
                fontSize = 13.sp,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = textColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                if (hasEvents) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .background(
                                if (isSelected) Color.White else Color(0xFFD97706),
                                CircleShape
                            )
                    )
                }

                if (hasWorkOrder) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .background(
                                if (isSelected) Color.White else Color(0xFF2563EB),
                                CircleShape
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun EventCard(event: CalendarEvent) {
    val statusColor = statusColor(event.status, event.isWorkOrder)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(80.dp)
                    .background(statusColor, RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp))
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = event.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1F2937),
                        modifier = Modifier.weight(1f)
                    )

                    if (!event.time.isNullOrBlank()) {
                        Text(
                            text = event.time,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MutedText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = listOfNotNull(event.equipmentTag, event.equipmentType).joinToString(" • "),
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Badge(
                        text = if (event.isWorkOrder) "Work Order" else "Jadwal",
                        color = if (event.isWorkOrder) Color(0xFF2563EB) else Color(0xFFD97706)
                    )

                    event.status?.let {
                        Badge(text = it, color = statusColor)
                    }
                }

                event.description?.takeIf { it.isNotBlank() }?.let {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = it,
                        fontSize = 11.sp,
                        color = MutedText,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

@Composable
private fun Badge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
private fun EmptyCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Event,
                contentDescription = null,
                tint = Color(0xFFCBD5E1),
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = message, fontSize = 12.sp, color = MutedText)
        }
    }
}

private fun statusColor(status: String?, isWorkOrder: Boolean): Color {
    if (!isWorkOrder) {
        return Color(0xFFD97706)
    }

    return when (status) {
        "Completed" -> Color(0xFF16A34A)
        "In Progress" -> Color(0xFF2563EB)
        "On Hold" -> Color(0xFFD97706)
        "Cancelled" -> Color(0xFFDC2626)
        else -> Color(0xFF6B7280)
    }
}

private fun formatAgendaDate(date: String?): String {
    if (date == null) return "-"
    return runCatching {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val formatter = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))
        formatter.format(parser.parse(date)!!)
    }.getOrDefault(date)
}
