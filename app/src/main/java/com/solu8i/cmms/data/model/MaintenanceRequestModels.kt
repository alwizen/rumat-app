package com.solu8i.cmms.data.model

import com.google.gson.annotations.SerializedName

data class MaintenanceRequestListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<MaintenanceRequestItem>,
    @SerializedName("meta") val meta: PaginationMeta?
)

data class MaintenanceRequestItem(
    @SerializedName("id") val id: Int,
    @SerializedName("request_number") val requestNumber: String,
    @SerializedName("operation_status") val operationStatus: String,
    @SerializedName("description") val description: String,
    @SerializedName("damage_date") val damageDate: String?,
    @SerializedName("damage_time") val damageTime: String?,
    @SerializedName("equipment_condition") val equipmentCondition: String?,
    @SerializedName("impact") val impact: String?,
    @SerializedName("early_action") val earlyAction: String?,
    @SerializedName("status") val status: String,
    @SerializedName("photo") val photo: List<String>?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("equipment") val equipment: EquipmentBrief?,
    @SerializedName("reported_by") val reportedBy: UserBrief?
)

data class MaintenanceRequestDetailResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: MaintenanceRequestDetail?
)

data class MaintenanceRequestDetail(
    @SerializedName("id") val id: Int,
    @SerializedName("request_number") val requestNumber: String,
    @SerializedName("operation_status") val operationStatus: String,
    @SerializedName("description") val description: String,
    @SerializedName("damage_date") val damageDate: String?,
    @SerializedName("damage_time") val damageTime: String?,
    @SerializedName("equipment_condition") val equipmentCondition: String?,
    @SerializedName("impact") val impact: String?,
    @SerializedName("early_action") val earlyAction: String?,
    @SerializedName("status") val status: String,
    @SerializedName("photo") val photo: List<String>?,
    @SerializedName("approval_notes") val approvalNotes: String?,
    @SerializedName("approved_at") val approvedAt: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?,
    @SerializedName("equipment") val equipment: EquipmentDetail?,
    @SerializedName("reported_by") val reportedBy: UserBrief?,
    @SerializedName("approved_by") val approvedBy: UserBrief?,
    @SerializedName("work_orders") val workOrders: List<WorkOrderBrief>?
)

data class WorkOrderBrief(
    @SerializedName("id") val id: Int,
    @SerializedName("work_order_number") val workOrderNumber: String,
    @SerializedName("status") val status: String
)

data class MaintenanceRequestCreateResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: MaintenanceRequestDetail?
)
