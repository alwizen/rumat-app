package com.solu8i.cmms.data.model

import com.google.gson.annotations.SerializedName

data class EquipmentListItem(
    @SerializedName("id") val id: Int,
    @SerializedName("tag_number") val tagNumber: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("equipment_type") val equipmentType: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("criticality") val criticality: String? = null,
    @SerializedName("area") val area: AreaBrief? = null,
    @SerializedName("latest_meter_log") val latestMeterLog: LatestMeterLog? = null
)

data class LatestMeterLog(
    @SerializedName("id") val id: Int,
    @SerializedName("reading_date") val readingDate: String? = null,
    @SerializedName("value") val value: Double = 0.0
)

data class EquipmentListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<EquipmentListItem>,
    @SerializedName("meta") val meta: PaginationMeta?
)

data class EquipmentDetailResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: EquipmentDetailData?
)

data class EquipmentDetailData(
    @SerializedName("id") val id: Int,
    @SerializedName("tag_number") val tagNumber: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("equipment_type") val equipmentType: String? = null,
    @SerializedName("manufacturer") val manufacturer: String? = null,
    @SerializedName("model") val model: String? = null,
    @SerializedName("serial_number") val serialNumber: String? = null,
    @SerializedName("installation_date") val installationDate: String? = null,
    @SerializedName("operational_unit") val operationalUnit: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("criticality") val criticality: String? = null,
    @SerializedName("area") val area: AreaBrief? = null
)
