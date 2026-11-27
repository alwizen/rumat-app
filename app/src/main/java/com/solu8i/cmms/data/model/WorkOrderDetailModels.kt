package com.solu8i.cmms.data.model

import com.google.gson.annotations.SerializedName

data class WorkOrderDetailResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: WorkOrderDetail?
)

data class WorkOrderDetail(
    @SerializedName("id") val id: Int,
    @SerializedName("work_order_number") val workOrderNumber: String,
    @SerializedName("classification") val classification: String,
    @SerializedName("interval") val interval: String?,
    @SerializedName("start_at") val startAt: String?,
    @SerializedName("finish_at") val finishAt: String?,
    @SerializedName("note") val note: String?,
    @SerializedName("status") val status: String,
    @SerializedName("can_execute") val canExecute: Boolean = true,
    @SerializedName("can_fill_checklist") val canFillChecklist: Boolean = false,
    @SerializedName("is_locked") val isLocked: Boolean = false,
    @SerializedName("lock_message") val lockMessage: String? = null,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("equipment") val equipment: EquipmentDetail?,
    @SerializedName("issued_by") val issuedBy: UserBrief?,
    @SerializedName("technician_coordinator") val technicianCoordinator: UserBrief?,
    @SerializedName("maintenance_request") val maintenanceRequest: MaintenanceRequestBrief? = null,
    @SerializedName("activities") val activities: List<WorkOrderActivityItem>?,
    @SerializedName("workers") val workers: List<WorkOrderWorkerItem>?
)

data class MaintenanceRequestBrief(
    @SerializedName("id") val id: Int,
    @SerializedName("request_number") val requestNumber: String?,
    @SerializedName("status") val status: String?
)

data class EquipmentDetail(
    @SerializedName("id") val id: Int,
    @SerializedName("tag_number") val tagNumber: String,
    @SerializedName("equipment_type") val equipmentType: String?,
    @SerializedName("area") val area: AreaBrief?
)

data class AreaBrief(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)

data class UserBrief(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)

data class WorkOrderActivityItem(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String?,
    @SerializedName("reference") val reference: String?,
    @SerializedName("pre_inspection") val preInspection: String?,
    @SerializedName("follow_up") val followUp: String?,
    @SerializedName("final_result") val finalResult: String?,
    @SerializedName("unit") val unit: String?,
    @SerializedName("executed") val executed: Boolean?,
    @SerializedName("note") val note: String?,
    @SerializedName("photos") val photos: List<String>? = null,
    @SerializedName("is_qualitative") val isQualitative: Boolean = false,
    @SerializedName("result_options") val resultOptions: List<ResultOption>? = null,
    @SerializedName("activity") val activity: ActivityBrief?
)

data class ResultOption(
    @SerializedName("value") val value: String,
    @SerializedName("label") val label: String
)

data class ActivityBrief(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("type") val type: String?,
    @SerializedName("reference") val reference: String?,
    @SerializedName("optimum") val optimum: String?,
    @SerializedName("minimum") val minimum: String?,
    @SerializedName("maximum") val maximum: String?,
    @SerializedName("unit") val unit: String?
)

data class WorkOrderWorkerItem(
    @SerializedName("id") val id: Int,
    @SerializedName("role") val role: String?,
    @SerializedName("started_at") val startedAt: String?,
    @SerializedName("finished_at") val finishedAt: String?,
    @SerializedName("note") val note: String?,
    @SerializedName("user") val user: UserBrief?
)

data class WorkOrderActivityResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: WorkOrderActivityItem?
)
