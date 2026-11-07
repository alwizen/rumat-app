package com.solu8i.cmms.data.model

import com.google.gson.annotations.SerializedName

data class MeterLogListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<MeterLogItem>,
    @SerializedName("meta") val meta: PaginationMeta?
)

data class MeterLogItem(
    @SerializedName("id") val id: Int,
    @SerializedName("equipment_id") val equipmentId: Int,
    @SerializedName("reading_date") val readingDate: String?,
    @SerializedName("value") val value: Double,
    @SerializedName("recorded_by") val recordedBy: Int? = null,
    @SerializedName("recorded_by_name") val recordedByName: String? = null,
    @SerializedName("equipment") val equipment: MeterLogEquipment? = null
)

data class MeterLogEquipment(
    @SerializedName("id") val id: Int,
    @SerializedName("tag_number") val tagNumber: String,
    @SerializedName("description") val description: String? = null
)

data class MeterLogCreateResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: MeterLogItem? = null
)
