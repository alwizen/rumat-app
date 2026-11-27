package com.solu8i.cmms.data.remote

import com.solu8i.cmms.data.model.CalendarResponse
import com.solu8i.cmms.data.model.DocumentDetailResponse
import com.solu8i.cmms.data.model.DocumentListResponse
import com.solu8i.cmms.data.model.DowntimeListResponse
import com.solu8i.cmms.data.model.DashboardResponse
import com.solu8i.cmms.data.model.EquipmentDetailResponse
import com.solu8i.cmms.data.model.EquipmentFilterOptionsResponse
import com.solu8i.cmms.data.model.EquipmentListResponse
import com.solu8i.cmms.data.model.FindingDetailResponse
import com.solu8i.cmms.data.model.FindingListResponse
import com.solu8i.cmms.data.model.MaintenanceHistoryListResponse
import com.solu8i.cmms.data.model.HousekeepingAreaListResponse
import com.solu8i.cmms.data.model.HousekeepingDashboardResponse
import com.solu8i.cmms.data.model.HousekeepingFacilityListResponse
import com.solu8i.cmms.data.model.HousekeepingManpowerListResponse
import com.solu8i.cmms.data.model.HousekeepingPlanDetailResponse
import com.solu8i.cmms.data.model.HousekeepingPlanListResponse
import com.solu8i.cmms.data.model.HousekeepingRequestActionResponse
import com.solu8i.cmms.data.model.HousekeepingRequestCreateResponse
import com.solu8i.cmms.data.model.HousekeepingRequestDetailResponse
import com.solu8i.cmms.data.model.HousekeepingRequestListResponse
import com.solu8i.cmms.data.model.HousekeepingTaskResponse
import com.solu8i.cmms.data.model.LoginRequest
import com.solu8i.cmms.data.model.LoginResponse
import com.solu8i.cmms.data.model.MaintenanceRequestCreateResponse
import com.solu8i.cmms.data.model.MaintenanceRequestDetailResponse
import com.solu8i.cmms.data.model.MaintenanceRequestListResponse
import com.solu8i.cmms.data.model.NotificationListResponse
import com.solu8i.cmms.data.model.NotificationReadResponse
import com.solu8i.cmms.data.model.UnreadCountResponse
import com.solu8i.cmms.data.model.WorkOrderActivityResponse
import com.solu8i.cmms.data.model.WorkOrderDetailResponse
import com.solu8i.cmms.data.model.WorkOrderListResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
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
import retrofit2.http.Streaming

interface ApiService {

    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("logout")
    suspend fun logout(): Response<Unit>

    @GET("dashboard")
    suspend fun getDashboard(): Response<DashboardResponse>

    /**
     * Ringkasan housekeeping (plan aktif, progres, SLA) untuk user
     * housekeeping / role akses penuh.
     */
    @GET("dashboard/housekeeping")
    suspend fun getHousekeepingDashboard(): Response<HousekeepingDashboardResponse>

    @GET("work-orders")
    suspend fun getWorkOrders(
        @Query("status") status: String? = null,
        @Query("classification") classification: String? = null,
        @Query("interval") interval: String? = null,
        @Query("search") search: String? = null,
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null,
        @Query("per_page") perPage: Int = 15,
        @Query("page") page: Int = 1
    ): Response<WorkOrderListResponse>

    @GET("work-orders/{id}")
    suspend fun getWorkOrderDetail(
        @Path("id") id: Int
    ): Response<WorkOrderDetailResponse>

    /**
     * Unduh laporan PDF satu work order. Hanya tersedia bila status Completed.
     */
    @Streaming
    @GET("work-orders/{id}/report")
    suspend fun downloadWorkOrderReport(
        @Path("id") id: Int,
    ): Response<ResponseBody>

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
        @Query("status") status: String? = null,
        @Query("area_id") areaId: Int? = null,
        @Query("equipment_type_id") equipmentTypeId: Int? = null,
        @Query("per_page") perPage: Int = 50,
        @Query("page") page: Int = 1
    ): Response<EquipmentListResponse>

    @GET("equipment-filter-options")
    suspend fun getEquipmentFilterOptions(): Response<EquipmentFilterOptionsResponse>

    @GET("equipment/{id}")
    suspend fun getEquipmentDetail(
        @Path("id") id: Int
    ): Response<EquipmentDetailResponse>

    @GET("equipment/{id}/maintenance-histories")
    suspend fun getEquipmentMaintenanceHistories(
        @Path("id") id: Int,
        @Query("per_page") perPage: Int = 50,
        @Query("page") page: Int = 1
    ): Response<MaintenanceHistoryListResponse>

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
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null,
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

    @GET("housekeeping-plans")
    suspend fun getHousekeepingPlans(
        @Query("status") status: String? = null,
        @Query("area_id") areaId: Int? = null,
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null,
        @Query("search") search: String? = null,
        @Query("per_page") perPage: Int = 15,
        @Query("page") page: Int = 1
    ): Response<HousekeepingPlanListResponse>

    @GET("housekeeping-plans/{id}")
    suspend fun getHousekeepingPlanDetail(
        @Path("id") id: Int
    ): Response<HousekeepingPlanDetailResponse>

    @PUT("housekeeping-plans/{planId}/tasks/{taskId}")
    suspend fun updateHousekeepingTask(
        @Path("planId") planId: Int,
        @Path("taskId") taskId: Int,
        @Body body: Map<String, @JvmSuppressWildcards Any?>
    ): Response<HousekeepingTaskResponse>

    @Multipart
    @POST("housekeeping-plans/{planId}/tasks/{taskId}/photos")
    suspend fun uploadHousekeepingTaskPhotos(
        @Path("planId") planId: Int,
        @Path("taskId") taskId: Int,
        @Part("type") type: RequestBody,
        @Part photos: List<MultipartBody.Part>
    ): Response<HousekeepingTaskResponse>

    @DELETE("housekeeping-plans/{planId}/tasks/{taskId}/photos")
    suspend fun deleteHousekeepingTaskPhoto(
        @Path("planId") planId: Int,
        @Path("taskId") taskId: Int,
        @Query("type") type: String,
        @Query("path") path: String
    ): Response<HousekeepingTaskResponse>

    /**
     * Unduh laporan harian housekeeping (PDF) untuk tanggal tertentu.
     * Hanya untuk user housekeeping / role akses penuh.
     */
    @Streaming
    @GET("reports/daily-housekeeping")
    suspend fun downloadDailyHousekeepingReport(
        @Query("date") date: String,
    ): Response<ResponseBody>

    @GET("housekeeping-areas")
    suspend fun getHousekeepingAreas(
        @Query("search") search: String? = null
    ): Response<HousekeepingAreaListResponse>

    @GET("housekeeping-areas/{id}/manpowers")
    suspend fun getHousekeepingAreaManpowers(
        @Path("id") id: Int
    ): Response<HousekeepingManpowerListResponse>

    @GET("housekeeping-areas/{id}/facilities")
    suspend fun getHousekeepingAreaFacilities(
        @Path("id") id: Int
    ): Response<HousekeepingFacilityListResponse>

    @GET("housekeeping-requests")
    suspend fun getHousekeepingRequests(
        @Query("status") status: String? = null,
        @Query("priority") priority: String? = null,
        @Query("area_id") areaId: Int? = null,
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null,
        @Query("search") search: String? = null,
        @Query("mine") mine: Boolean? = null,
        @Query("per_page") perPage: Int = 15,
        @Query("page") page: Int = 1
    ): Response<HousekeepingRequestListResponse>

    @GET("housekeeping-requests/{id}")
    suspend fun getHousekeepingRequestDetail(
        @Path("id") id: Int
    ): Response<HousekeepingRequestDetailResponse>

    @Multipart
    @POST("housekeeping-requests")
    suspend fun createHousekeepingRequest(
        @Part("housekeeping_area_id") areaId: RequestBody,
        @Part("housekeeping_facility_id") facilityId: RequestBody?,
        @Part("request_date") requestDate: RequestBody,
        @Part("request_time") requestTime: RequestBody?,
        @Part("description") description: RequestBody,
        @Part("priority") priority: RequestBody,
        @Part photos: List<MultipartBody.Part>?
    ): Response<HousekeepingRequestCreateResponse>

    @DELETE("housekeeping-requests/{id}")
    suspend fun deleteHousekeepingRequest(
        @Path("id") id: Int
    ): Response<HousekeepingRequestActionResponse>

    /**
     * Temuan hasil checklist work order (Poor/Not Working atau di luar batas).
     */
    @GET("findings")
    suspend fun getFindings(
        @Query("status") status: String? = null,
        @Query("severity") severity: String? = null,
        @Query("answer_type") answerType: String? = null,
        @Query("work_order_id") workOrderId: Int? = null,
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null,
        @Query("search") search: String? = null,
        @Query("per_page") perPage: Int = 15,
        @Query("page") page: Int = 1
    ): Response<FindingListResponse>

    @GET("findings/{id}")
    suspend fun getFindingDetail(
        @Path("id") id: Int
    ): Response<FindingDetailResponse>

    /**
     * Ajukan maintenance request perbaikan dari sebuah temuan.
     */
    @POST("findings/{id}/request")
    suspend fun submitFindingRequest(
        @Path("id") id: Int
    ): Response<FindingDetailResponse>
}
