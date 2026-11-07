package com.solu8i.cmms.data.model

import com.google.gson.annotations.SerializedName

data class DowntimeListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<DowntimeItem>,
    @SerializedName("meta") val meta: PaginationMeta?
)

data class DowntimeItem(
    @SerializedName("id") val id: Int,
    @SerializedName("request_number") val requestNumber: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("operation_status") val operationStatus: String?,
    @SerializedName("equipment") val equipment: DowntimeEquipment?,
    @SerializedName("downtime") val downtime: DowntimeInfo?
)

data class DowntimeEquipment(
    @SerializedName("id") val id: Int,
    @SerializedName("tag_number") val tagNumber: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("area") val area: String?
)

data class DowntimeInfo(
    @SerializedName("start") val start: String?,
    @SerializedName("end") val end: String?,
    @SerializedName("minutes") val minutes: Int?,
    @SerializedName("label") val label: String?,
    @SerializedName("ongoing") val ongoing: Boolean
)
