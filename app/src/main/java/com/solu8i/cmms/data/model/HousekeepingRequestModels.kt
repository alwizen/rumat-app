package com.solu8i.cmms.data.model

import com.google.gson.annotations.SerializedName

data class HousekeepingRequestListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<HousekeepingRequestItem>,
    @SerializedName("meta") val meta: PaginationMeta?
)

data class HousekeepingRequestItem(
    @SerializedName("id") val id: Int,
    @SerializedName("request_number") val requestNumber: String,
    @SerializedName("request_date") val requestDate: String?,
    @SerializedName("request_time") val requestTime: String?,
    @SerializedName("description") val description: String,
    @SerializedName("priority") val priority: String,
    @SerializedName("status") val status: String,
    @SerializedName("photo") val photo: List<String>?,
    @SerializedName("approval_notes") val approvalNotes: String?,
    @SerializedName("approved_at") val approvedAt: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("area") val area: HousekeepingRequestArea?,
    @SerializedName("facility") val facility: HousekeepingRequestFacility?,
    @SerializedName("requested_by") val requestedBy: String?,
    @SerializedName("approved_by") val approvedBy: String?,
    @SerializedName("plan") val plan: HousekeepingRequestPlan?
)

data class HousekeepingRequestArea(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("code") val code: String?
)

data class HousekeepingRequestFacility(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("code") val code: String?
)

data class HousekeepingRequestPlan(
    @SerializedName("id") val id: Int,
    @SerializedName("plan_number") val planNumber: String,
    @SerializedName("plan_date") val planDate: String?,
    @SerializedName("status") val status: String
)

data class HousekeepingRequestDetailResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: HousekeepingRequestItem?
)

data class HousekeepingRequestCreateResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: HousekeepingRequestItem?
)

data class HousekeepingRequestActionResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: HousekeepingRequestItem?
)
