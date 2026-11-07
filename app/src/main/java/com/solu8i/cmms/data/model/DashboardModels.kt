package com.solu8i.cmms.data.model

import com.google.gson.annotations.SerializedName

data class DashboardResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: DashboardData?
)

data class DashboardData(
    @SerializedName("stats") val stats: DashboardStats,
    @SerializedName("today_work_orders") val todayWorkOrders: List<RecentWorkOrder>? = null,
    @SerializedName("recent_work_orders") val recentWorkOrders: List<RecentWorkOrder>
)

data class DashboardStats(
    @SerializedName("total_work_orders") val totalWorkOrders: Int,
    @SerializedName("in_progress") val inProgress: Int,
    @SerializedName("completed") val completed: Int,
    @SerializedName("pending") val pending: Int,
    @SerializedName("due_today") val dueToday: Int = 0,
    @SerializedName("overdue") val overdue: Int = 0
)

data class RecentWorkOrder(
    @SerializedName("id") val id: Int,
    @SerializedName("work_order_number") val workOrderNumber: String,
    @SerializedName("status") val status: String,
    @SerializedName("classification") val classification: String,
    @SerializedName("start_at") val startAt: String?,
    @SerializedName("start_time") val startTime: String? = null,
    @SerializedName("equipment") val equipment: EquipmentBrief?
)

data class EquipmentBrief(
    @SerializedName("tag_number") val tagNumber: String,
    @SerializedName("equipment_type") val equipmentType: String?
)
