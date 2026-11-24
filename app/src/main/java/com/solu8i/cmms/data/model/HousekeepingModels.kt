package com.solu8i.cmms.data.model

import com.google.gson.annotations.SerializedName

data class HousekeepingPlanListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<HousekeepingPlanItem>,
    @SerializedName("meta") val meta: PaginationMeta?
)

data class HousekeepingPlanItem(
    @SerializedName("id") val id: Int,
    @SerializedName("plan_number") val planNumber: String,
    @SerializedName("plan_date") val planDate: String?,
    @SerializedName("status") val status: String,
    @SerializedName("approval_status") val approvalStatus: String? = null,
    @SerializedName("approval_notes") val approvalNotes: String? = null,
    @SerializedName("note") val note: String?,
    @SerializedName("progress_percent") val progressPercent: Int? = null,
    @SerializedName("sla_days") val slaDays: Int? = null,
    @SerializedName("due_date") val dueDate: String? = null,
    @SerializedName("sla_status") val slaStatus: String? = null,
    @SerializedName("is_overdue") val isOverdue: Boolean? = null,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("area") val area: HousekeepingAreaBrief?,
    @SerializedName("supervisor") val supervisor: String?,
    @SerializedName("manpower_count") val manpowerCount: Int?,
    @SerializedName("task_count") val taskCount: Int?,
    @SerializedName("task_done_count") val taskDoneCount: Int?
)

data class HousekeepingAreaBrief(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("code") val code: String?,
    @SerializedName("company") val company: HousekeepingCompanyBrief?,
    @SerializedName("supervisor") val supervisor: String?,
    @SerializedName("area_size") val areaSize: Double?
)

data class HousekeepingCompanyBrief(
    @SerializedName("id") val id: Int?,
    @SerializedName("name") val name: String?
)

data class HousekeepingPlanDetailResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: HousekeepingPlanDetail?
)

data class HousekeepingPlanDetail(
    @SerializedName("id") val id: Int,
    @SerializedName("plan_number") val planNumber: String,
    @SerializedName("plan_date") val planDate: String?,
    @SerializedName("status") val status: String,
    @SerializedName("approval_status") val approvalStatus: String? = null,
    @SerializedName("approval_notes") val approvalNotes: String? = null,
    @SerializedName("approved_by") val approvedBy: String? = null,
    @SerializedName("note") val note: String?,
    @SerializedName("progress_percent") val progressPercent: Int? = null,
    @SerializedName("sla_days") val slaDays: Int? = null,
    @SerializedName("due_date") val dueDate: String? = null,
    @SerializedName("sla_status") val slaStatus: String? = null,
    @SerializedName("is_overdue") val isOverdue: Boolean? = null,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("created_by") val createdBy: String?,
    @SerializedName("area") val area: HousekeepingAreaDetail?,
    @SerializedName("manpowers") val manpowers: List<HousekeepingManpowerItem>?,
    @SerializedName("tasks") val tasks: List<HousekeepingTaskItem>?
)

data class HousekeepingAreaDetail(
    @SerializedName("id") val id: Int?,
    @SerializedName("name") val name: String?,
    @SerializedName("code") val code: String?,
    @SerializedName("company") val company: String?,
    @SerializedName("supervisor") val supervisor: String?,
    @SerializedName("area_size") val areaSize: Double?,
    @SerializedName("sla_days") val slaDays: Int? = null,
    /** Batas area dalam format [[lat, lng], ...]. */
    @SerializedName("polygon") val polygon: List<List<Double>>? = null
)

data class HousekeepingManpowerItem(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("employee_number") val employeeNumber: String?,
    @SerializedName("phone") val phone: String?,
    @SerializedName("note") val note: String?
)

/**
 * Satu item checklist. Foto before/after menempel pada item checklist ini,
 * bukan pada level fasilitas.
 */
data class HousekeepingTaskItem(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("status") val status: String,
    @SerializedName("note") val note: String?,
    @SerializedName("photo_before") val photoBefore: List<String>? = null,
    @SerializedName("photo_after") val photoAfter: List<String>? = null,
    @SerializedName("started_at") val startedAt: String?,
    @SerializedName("completed_at") val completedAt: String?,
    @SerializedName("facility") val facility: HousekeepingFacilityBrief?,
    @SerializedName("master_task") val masterTask: HousekeepingMasterTaskBrief?
)

data class HousekeepingFacilityBrief(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("code") val code: String?,
    /** Batas fasilitas dalam format [[lat, lng], ...]. */
    @SerializedName("polygon") val polygon: List<List<Double>>? = null,
    @SerializedName("latitude") val latitude: Double? = null,
    @SerializedName("longitude") val longitude: Double? = null
)

data class HousekeepingMasterTaskBrief(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)

data class HousekeepingTaskResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: HousekeepingTaskItem?
)

data class HousekeepingAreaListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<HousekeepingAreaSimple>
)

data class HousekeepingAreaSimple(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("code") val code: String?,
    @SerializedName("company") val company: String?,
    @SerializedName("area_size") val areaSize: Double?,
    @SerializedName("sla_days") val slaDays: Int? = null,
    @SerializedName("supervisor") val supervisor: String?,
    /** Batas area dalam format [[lat, lng], ...]. */
    @SerializedName("polygon") val polygon: List<List<Double>>? = null,
    @SerializedName("latitude") val latitude: Double? = null,
    @SerializedName("longitude") val longitude: Double? = null
)

data class HousekeepingManpowerListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<HousekeepingManpowerSimple>
)

data class HousekeepingManpowerSimple(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("employee_number") val employeeNumber: String?,
    @SerializedName("phone") val phone: String?
)

data class HousekeepingFacilityListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<HousekeepingFacilityWithTasks>
)

data class HousekeepingFacilityWithTasks(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("code") val code: String?,
    @SerializedName("tasks") val tasks: List<HousekeepingMasterTaskBrief>,
    /** Batas fasilitas dalam format [[lat, lng], ...]. */
    @SerializedName("polygon") val polygon: List<List<Double>>? = null,
    @SerializedName("area_size") val areaSize: Double? = null,
    @SerializedName("latitude") val latitude: Double? = null,
    @SerializedName("longitude") val longitude: Double? = null
)

data class SimpleActionResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?
)
