package com.solu8i.cmms.data.model

import com.google.gson.annotations.SerializedName

data class WorkOrderListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<WorkOrderItem>,
    @SerializedName("meta") val meta: PaginationMeta?
)

data class WorkOrderItem(
    @SerializedName("id") val id: Int,
    @SerializedName("work_order_number") val workOrderNumber: String,
    @SerializedName("classification") val classification: String,
    @SerializedName("interval") val interval: String?,
    @SerializedName("start_at") val startAt: String?,
    @SerializedName("finish_at") val finishAt: String?,
    @SerializedName("note") val note: String?,
    @SerializedName("status") val status: String,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("equipment") val equipment: EquipmentBrief?
)

data class PaginationMeta(
    @SerializedName("current_page") val currentPage: Int,
    @SerializedName("last_page") val lastPage: Int,
    @SerializedName("per_page") val perPage: Int,
    @SerializedName("total") val total: Int
)
