package com.solu8i.cmms.data.model

import com.google.gson.annotations.SerializedName

data class EquipmentListItem(
    @SerializedName("id") val id: Int,
    @SerializedName("tag_number") val tagNumber: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("equipment_type") val equipmentType: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("criticality") val criticality: String? = null,
    @SerializedName("area") val area: AreaBrief? = null
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

data class EquipmentTypeDetail(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("responsible_user") val responsibleUser: ResponsibleUser? = null
)

data class ResponsibleUser(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)

data class EquipmentDetailData(
    @SerializedName("id") val id: Int,
    @SerializedName("tag_number") val tagNumber: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("equipment_type") val equipmentType: String? = null,
    @SerializedName("equipment_type_detail") val equipmentTypeDetail: EquipmentTypeDetail? = null,
    @SerializedName("manufacturer") val manufacturer: String? = null,
    @SerializedName("model") val model: String? = null,
    @SerializedName("serial_number") val serialNumber: String? = null,
    @SerializedName("installation_date") val installationDate: String? = null,
    @SerializedName("operational_unit") val operationalUnit: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("criticality") val criticality: String? = null,
    @SerializedName("area") val area: AreaBrief? = null
)

data class FilterOptionItem(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)

data class EquipmentFilterOptions(
    @SerializedName("areas") val areas: List<FilterOptionItem> = emptyList(),
    @SerializedName("equipment_types") val equipmentTypes: List<FilterOptionItem> = emptyList()
)

data class EquipmentFilterOptionsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: EquipmentFilterOptions? = null
)

data class MaintenanceHistoryItem(
    @SerializedName("id") val id: Int,
    @SerializedName("work_order_number") val workOrderNumber: String? = null,
    @SerializedName("maintenance_type") val maintenanceType: String? = null,
    @SerializedName("classification") val classification: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("maintenance_date") val maintenanceDate: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("duration_minutes") val durationMinutes: Int? = null,
    @SerializedName("findings") val findings: String? = null,
    @SerializedName("actions_taken") val actionsTaken: String? = null,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("performed_by") val performedBy: ResponsibleUser? = null,
    @SerializedName("checklist") val checklist: List<MaintenanceHistoryChecklistItem>? = null
)

data class MaintenanceHistoryChecklistItem(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("executed") val executed: Boolean? = null,
    @SerializedName("unit") val unit: String? = null,
    @SerializedName("pre_inspection") val preInspection: String? = null,
    @SerializedName("final_result") val finalResult: String? = null,
    @SerializedName("note") val note: String? = null
)

data class MaintenanceHistoryListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<MaintenanceHistoryItem> = emptyList(),
    @SerializedName("meta") val meta: PaginationMeta? = null
)
