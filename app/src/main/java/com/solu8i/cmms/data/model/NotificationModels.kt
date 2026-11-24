package com.solu8i.cmms.data.model

import com.google.gson.annotations.SerializedName

data class NotificationListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<NotificationItem>,
    @SerializedName("meta") val meta: NotificationMeta?
)

data class NotificationMeta(
    @SerializedName("current_page") val currentPage: Int,
    @SerializedName("last_page") val lastPage: Int,
    @SerializedName("per_page") val perPage: Int,
    @SerializedName("total") val total: Int,
    @SerializedName("unread_count") val unreadCount: Int
)

data class NotificationItem(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("read") val read: Boolean,
    @SerializedName("read_at") val readAt: String?,
    @SerializedName("created_at") val createdAt: String?
)

data class UnreadCountResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: UnreadCountData
)

data class UnreadCountData(
    @SerializedName("count") val count: Int
)

data class NotificationReadResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null
)
