package com.solu8i.cmms.data.remote

import com.solu8i.cmms.data.model.CalendarResponse
import com.solu8i.cmms.data.model.DocumentDetailResponse
import com.solu8i.cmms.data.model.DocumentListResponse
import com.solu8i.cmms.data.model.DowntimeListResponse
import com.solu8i.cmms.data.model.DashboardResponse
import com.solu8i.cmms.data.model.EquipmentDetailResponse
import com.solu8i.cmms.data.model.EquipmentListResponse
import com.solu8i.cmms.data.model.LoginRequest
import com.solu8i.cmms.data.model.LoginResponse
import com.solu8i.cmms.data.model.MaintenanceRequestCreateResponse
import com.solu8i.cmms.data.model.MaintenanceRequestDetailResponse
import com.solu8i.cmms.data.model.MaintenanceRequestListResponse
import com.solu8i.cmms.data.model.MeterLogCreateResponse
import com.solu8i.cmms.data.model.MeterLogListResponse
import com.solu8i.cmms.data.model.NotificationListResponse
import com.solu8i.cmms.data.model.NotificationReadResponse
import com.solu8i.cmms.data.model.UnreadCountResponse
import com.solu8i.cmms.data.model.WorkOrderActivityResponse
import com.solu8i.cmms.data.model.WorkOrderDetailResponse
import com.solu8i.cmms.data.model.WorkOrderListResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("dashboard")
    suspend fun getDashboard(): Response<DashboardResponse>

    @GET("work-orders")
    suspend fun getWorkOrders(
        @Query("status") status: String? = null,
        @Query("search") search: String? = null,
        @Query("per_page") perPage: Int = 15,
        @Query("page") page: Int = 1
    ): Response<WorkOrderListResponse>

    @GET("work-orders/{id}")
    suspend fun getWorkOrderDetail(
        @Path("id") id: Int
    ): Response<WorkOrderDetailResponse>

    @PUT("work-orders/{id}")
    suspend fun updateWorkOrder(
        @Path("id") id: Int,
        @Body body: Map<String, @JvmSuppressWildcards Any?>
    ): Response<WorkOrderDetailResponse>

    @PUT("work-orders/{workOrderId}/activities/{activityId}")
    suspend fun updateWorkOrderActivity(
        @Path("workOrderId") workOrderId: Int,
        @Path("activityId") activityId: Int,
        @Body body: Map<String, @JvmSuppressWildcards Any?>
    ): Response<WorkOrderActivityResponse>

    @Multipart
    @POST("work-orders/{workOrderId}/activities/{activityId}/photos")
    suspend fun uploadActivityPhotos(
        @Path("workOrderId") workOrderId: Int,
        @Path("activityId") activityId: Int,
        @Part photos: List<MultipartBody.Part>
    ): Response<WorkOrderActivityResponse>

    @DELETE("work-orders/{workOrderId}/activities/{activityId}/photos")
    suspend fun deleteActivityPhoto(
        @Path("workOrderId") workOrderId: Int,
        @Path("activityId") activityId: Int,
        @Query("path") path: String
    ): Response<WorkOrderActivityResponse>

    @GET("equipment")
    suspend fun getEquipment(
        @Query("search") search: String? = null,
        @Query("per_page") perPage: Int = 50,
        @Query("page") page: Int = 1
    ): Response<EquipmentListResponse>

    @GET("equipment/{id}")
    suspend fun getEquipmentDetail(
        @Path("id") id: Int
    ): Response<EquipmentDetailResponse>

    @GET("meter-logs")
    suspend fun getMeterLogs(
        @Query("equipment_id") equipmentId: Int? = null,
        @Query("search") search: String? = null,
        @Query("per_page") perPage: Int = 50,
        @Query("page") page: Int = 1
    ): Response<MeterLogListResponse>

    @POST("meter-logs")
    suspend fun createMeterLog(
        @Body body: Map<String, @JvmSuppressWildcards Any?>
    ): Response<MeterLogCreateResponse>

    @GET("downtime")
    suspend fun getDowntime(
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null,
        @Query("equipment_id") equipmentId: Int? = null,
        @Query("per_page") perPage: Int = 50,
        @Query("page") page: Int = 1
    ): Response<DowntimeListResponse>

    @GET("documents")
    suspend fun getDocuments(
        @Query("search") search: String? = null,
        @Query("type") type: String? = null,
        @Query("expiry_status") expiryStatus: String? = null,
        @Query("per_page") perPage: Int = 50,
        @Query("page") page: Int = 1
    ): Response<DocumentListResponse>

    @GET("documents/{id}")
    suspend fun getDocumentDetail(
        @Path("id") id: Int
    ): Response<DocumentDetailResponse>

    @GET("notifications")
    suspend fun getNotifications(
        @Query("unread") unread: Boolean? = null,
        @Query("per_page") perPage: Int = 30,
        @Query("page") page: Int = 1
    ): Response<NotificationListResponse>

    @GET("notifications/unread-count")
    suspend fun getUnreadNotificationCount(): Response<UnreadCountResponse>

    @POST("notifications/{id}/read")
    suspend fun markNotificationRead(
        @Path("id") id: String
    ): Response<NotificationReadResponse>

    @POST("notifications/read-all")
    suspend fun markAllNotificationsRead(): Response<NotificationReadResponse>

    @GET("maintenance-calendar")
    suspend fun getMaintenanceCalendar(
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null
    ): Response<CalendarResponse>

    @GET("maintenance-requests")
    suspend fun getMaintenanceRequests(
        @Query("status") status: String? = null,
        @Query("search") search: String? = null,
        @Query("per_page") perPage: Int = 15,
        @Query("page") page: Int = 1
    ): Response<MaintenanceRequestListResponse>

    @GET("maintenance-requests/{id}")
    suspend fun getMaintenanceRequestDetail(
        @Path("id") id: Int
    ): Response<MaintenanceRequestDetailResponse>

    @Multipart
    @POST("maintenance-requests")
    suspend fun createMaintenanceRequest(
        @Part("equipment_id") equipmentId: RequestBody,
        @Part("operation_status") operationStatus: RequestBody,
        @Part("description") description: RequestBody,
        @Part("damage_date") damageDate: RequestBody,
        @Part("damage_time") damageTime: RequestBody?,
        @Part("equipment_condition") equipmentCondition: RequestBody?,
        @Part("impact") impact: RequestBody?,
        @Part("early_action") earlyAction: RequestBody?,
        @Part("status") status: RequestBody,
        @Part photos: List<MultipartBody.Part>?
    ): Response<MaintenanceRequestCreateResponse>

    @Multipart
    @PUT("maintenance-requests/{id}")
    suspend fun updateMaintenanceRequest(
        @Path("id") id: Int,
        @Part("equipment_id") equipmentId: RequestBody?,
        @Part("operation_status") operationStatus: RequestBody?,
        @Part("description") description: RequestBody?,
        @Part("damage_date") damageDate: RequestBody?,
        @Part("damage_time") damageTime: RequestBody?,
        @Part("equipment_condition") equipmentCondition: RequestBody?,
        @Part("impact") impact: RequestBody?,
        @Part("early_action") earlyAction: RequestBody?,
        @Part("status") status: RequestBody?,
        @Part photos: List<MultipartBody.Part>?
    ): Response<MaintenanceRequestCreateResponse>

    @DELETE("maintenance-requests/{id}")
    suspend fun deleteMaintenanceRequest(
        @Path("id") id: Int
    ): Response<MaintenanceRequestCreateResponse>
}
