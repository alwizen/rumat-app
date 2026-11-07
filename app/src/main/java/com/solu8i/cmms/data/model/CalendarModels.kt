package com.solu8i.cmms.data.model

import com.google.gson.annotations.SerializedName

data class CalendarResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: CalendarData?
)

data class CalendarData(
    @SerializedName("from") val from: String?,
    @SerializedName("until") val until: String?,
    @SerializedName("events") val events: List<CalendarEvent>
)

data class CalendarEvent(
    @SerializedName("id") val id: String,
    @SerializedName("record_id") val recordId: Int,
    @SerializedName("type") val type: String,
    @SerializedName("date") val date: String?,
    @SerializedName("time") val time: String?,
    @SerializedName("title") val title: String,
    @SerializedName("status") val status: String?,
    @SerializedName("classification") val classification: String?,
    @SerializedName("equipment_tag") val equipmentTag: String?,
    @SerializedName("equipment_type") val equipmentType: String?,
    @SerializedName("plan_number") val planNumber: String?,
    @SerializedName("schedule_number") val scheduleNumber: String?,
    @SerializedName("work_order_number") val workOrderNumber: String?,
    @SerializedName("description") val description: String?
) {
    val isWorkOrder: Boolean get() = type == "work_order"
}
