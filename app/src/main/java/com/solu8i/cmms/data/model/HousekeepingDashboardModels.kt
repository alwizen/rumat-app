package com.solu8i.cmms.data.model

import com.google.gson.annotations.SerializedName

data class HousekeepingDashboardResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: HousekeepingDashboardData?
)

data class HousekeepingDashboardData(
    @SerializedName("stats") val stats: HousekeepingDashboardStats,
    @SerializedName("active_plans") val activePlans: List<HousekeepingActivePlan>? = null
)

data class HousekeepingDashboardStats(
    @SerializedName("plans_today") val plansToday: Int = 0,
    @SerializedName("plans_active") val plansActive: Int = 0,
    @SerializedName("plans_overdue") val plansOverdue: Int = 0,
    @SerializedName("plans_completed_today") val plansCompletedToday: Int = 0,
    @SerializedName("tasks_open") val tasksOpen: Int = 0,
    @SerializedName("tasks_done_today") val tasksDoneToday: Int = 0,
    @SerializedName("tasks_total") val tasksTotal: Int = 0,
    @SerializedName("tasks_done") val tasksDone: Int = 0,
    @SerializedName("completion_rate") val completionRate: Int = 0
)

data class HousekeepingActivePlan(
    @SerializedName("id") val id: Int,
    @SerializedName("plan_number") val planNumber: String,
    @SerializedName("plan_date") val planDate: String? = null,
    @SerializedName("status") val status: String,
    @SerializedName("approval_status") val approvalStatus: String? = null,
    @SerializedName("progress_percent") val progressPercent: Int? = null,
    @SerializedName("sla_days") val slaDays: Int? = null,
    @SerializedName("due_date") val dueDate: String? = null,
    @SerializedName("sla_status") val slaStatus: String? = null,
    @SerializedName("is_overdue") val isOverdue: Boolean? = null,
    @SerializedName("area") val area: HousekeepingAreaBrief? = null,
    @SerializedName("task_count") val taskCount: Int? = null,
    @SerializedName("task_done_count") val taskDoneCount: Int? = null
)
