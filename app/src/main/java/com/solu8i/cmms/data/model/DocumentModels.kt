package com.solu8i.cmms.data.model

import com.google.gson.annotations.SerializedName

data class DocumentListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<DocumentItem>,
    @SerializedName("meta") val meta: PaginationMeta?
)

data class DocumentDetailResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: DocumentItem?
)

data class DocumentItem(
    @SerializedName("id") val id: Int,
    @SerializedName("document_number") val documentNumber: String?,
    @SerializedName("title") val title: String,
    @SerializedName("document_type") val documentType: String?,
    @SerializedName("document_type_label") val documentTypeLabel: String?,
    @SerializedName("equipment") val equipment: DocumentEquipment?,
    @SerializedName("revision") val revision: String?,
    @SerializedName("issuer") val issuer: String?,
    @SerializedName("issue_date") val issueDate: String?,
    @SerializedName("expiry_date") val expiryDate: String?,
    @SerializedName("expiry_status") val expiryStatus: String?,
    @SerializedName("expiry_status_label") val expiryStatusLabel: String?,
    @SerializedName("file_url") val fileUrl: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("uploaded_by") val uploadedBy: String?
)

data class DocumentEquipment(
    @SerializedName("id") val id: Int,
    @SerializedName("tag_number") val tagNumber: String?
)
