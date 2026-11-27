package com.solu8i.cmms.data.model

import com.google.gson.annotations.SerializedName

data class FindingListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<FindingItem>,
    @SerializedName("meta") val meta: PaginationMeta?
)

data class FindingDetailResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: FindingItem?
)

data class FindingItem(
    @SerializedName("id") val id: Int,
    @SerializedName("finding_number") val findingNumber: String,
    @SerializedName("answer_type") val answerType: String,
    @SerializedName("result") val result: String?,
    @SerializedName("severity") val severity: String,
    @SerializedName("description") val description: String?,
    @SerializedName("status") val status: String,
    @SerializedName("notes") val notes: String?,
    @SerializedName("photos") val photos: List<String>?,
    @SerializedName("resolved_at") val resolvedAt: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("activity_name") val activityName: String?,
    @SerializedName("work_order") val workOrder: WorkOrderBrief?,
    @SerializedName("equipment") val equipment: FindingEquipment?,
    @SerializedName("maintenance_request") val maintenanceRequest: FindingMaintenanceRequest?
)

data class FindingEquipment(
    @SerializedName("id") val id: Int,
    @SerializedName("tag_number") val tagNumber: String,
    @SerializedName("equipment_type") val equipmentType: String?
)

data class FindingMaintenanceRequest(
    @SerializedName("id") val id: Int,
    @SerializedName("request_number") val requestNumber: String,
    @SerializedName("status") val status: String?
)
