package com.solu8i.cmms.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

data class FilterOption(val value: String, val label: String)

data class FilterGroup(
    val title: String,
    val options: List<FilterOption>
)

/**
 * Bar pencarian + tombol filter (gaya Dokumen).
 * `hasActiveFilter` menentukan tombol menyala & titik merah.
 */
@Composable
fun SearchFilterBar(
    query: String,
    onQueryChange: (String) -> Unit,
    hasActiveFilter: Boolean,
    onClickFilter: () -> Unit,
    placeholder: String = "Cari..."
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (query.isEmpty()) {
                        Text(placeholder, fontSize = 12.sp, color = Color(0xFF9CA3AF), maxLines = 1)
                    }
                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 13.sp, color = Color(0xFF111827)),
                        cursorBrush = SolidColor(Color(0xFF015E4E)),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (hasActiveFilter) Color(0xFF015E4E) else Color.White)
                .border(
                    width = 1.dp,
                    color = if (hasActiveFilter) Color(0xFF015E4E) else Color(0xFFD1D5DB),
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable { onClickFilter() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "Filter",
                tint = if (hasActiveFilter) Color.White else Color(0xFF6B7280),
                modifier = Modifier.size(20.dp)
            )

            if (hasActiveFilter) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(8.dp)
                        .background(Color(0xFFDC2626), CircleShape)
                )
            }
        }
    }
}

/**
 * Bottom sheet filter generik dengan beberapa grup chip.
 * `current` berisi nilai terpilih per judul grup.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterBottomSheet(
    title: String,
    groups: List<FilterGroup>,
    current: Map<String, String>,
    onDismiss: () -> Unit,
    onApply: (Map<String, String>) -> Unit,
    showDateRange: Boolean = false,
    dateFrom: String = "",
    dateTo: String = "",
    onApplyWithRange: ((Map<String, String>, String, String) -> Unit)? = null
) {
    var temp by remember { mutableStateOf(current) }
    var tempFrom by remember { mutableStateOf(dateFrom) }
    var tempTo by remember { mutableStateOf(dateTo) }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color.White) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF015E4E)
            )

            if (showDateRange) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Rentang Tanggal",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF374151)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DateField(
                        label = "Dari",
                        value = tempFrom,
                        modifier = Modifier.weight(1f),
                        onChange = { tempFrom = it }
                    )
                    DateField(
                        label = "Sampai",
                        value = tempTo,
                        modifier = Modifier.weight(1f),
                        onChange = { tempTo = it }
                    )
                }
            }

            groups.forEach { group ->
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = group.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF374151)
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    group.options.forEach { option ->
                        FilterChip(
                            selected = (temp[group.title] ?: "") == option.value,
                            onClick = { temp = temp + (group.title to option.value) },
                            label = { Text(option.label, fontSize = 12.sp) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        temp = temp.mapValues { "" }
                        tempFrom = ""
                        tempTo = ""
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Reset", fontSize = 14.sp)
                }

                Button(
                    onClick = {
                        if (onApplyWithRange != null) {
                            onApplyWithRange(temp, tempFrom, tempTo)
                        } else {
                            onApply(temp)
                        }
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

/**
 * Field tanggal yang membuka DatePickerDialog. Nilai disimpan sebagai string ISO
 * `yyyy-MM-dd` (null/kosong bila belum dipilih).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onChange: (String) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(12.dp))
            .clickable { showPicker = true }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Column {
            Text(label, fontSize = 11.sp, color = Color(0xFF6B7280))
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = Color(0xFF015E4E),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = value.ifBlank { "Pilih tanggal" },
                    fontSize = 13.sp,
                    fontWeight = if (value.isBlank()) FontWeight.Normal else FontWeight.Medium,
                    color = if (value.isBlank()) Color(0xFF9CA3AF) else Color(0xFF111827)
                )
            }
        }
    }

    if (showPicker) {
        val initialMillis = isoToMillis(value)
        val state = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { onChange(millisToIso(it)) }
                    showPicker = false
                }) {
                    Text("Pilih", color = Color(0xFF015E4E))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("Batal", color = Color(0xFF6B7280))
                }
            }
        ) {
            DatePicker(state = state)
        }
    }
}

private fun isoToMillis(iso: String): Long? {
    if (iso.isBlank()) return null
    return try {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        fmt.parse(iso)?.time
    } catch (_: Exception) {
        null
    }
}

private fun millisToIso(millis: Long): String {
    val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    return fmt.format(java.util.Date(millis))
}
