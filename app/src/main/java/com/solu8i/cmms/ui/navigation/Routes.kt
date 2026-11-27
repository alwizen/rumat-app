package com.solu8i.cmms.ui.navigation

object Routes {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val HISTORY = "history"
    const val CALENDAR = "calendar"
    const val EQUIPMENT = "equipment"
    const val EQUIPMENT_DETAIL = "equipment/{equipmentId}"
    const val DOWNTIME = "downtime"
    const val DOCUMENT = "document"
    const val DOCUMENT_DETAIL = "document/{documentId}"
    const val MAINTENANCE_REQUEST = "maintenance-request"
    const val FINDING = "finding"
    const val MY_TAB = "my-tab"
    const val NOTIFICATION = "notification"
    const val WORK_ORDER_DETAIL = "work-order/{workOrderId}"
    const val MAINTENANCE_REQUEST_DETAIL = "maintenance-request/{requestId}"
    const val MAINTENANCE_REQUEST_CREATE = "maintenance-request-create"

    const val HOUSEKEEPING_DASHBOARD = "housekeeping-dashboard"
    const val HOUSEKEEPING = "housekeeping"
    const val HOUSEKEEPING_DETAIL = "housekeeping/{planId}"

    const val HOUSEKEEPING_REQUEST = "housekeeping-request"
    const val HOUSEKEEPING_REQUEST_CREATE = "housekeeping-request-create"
    const val HOUSEKEEPING_REQUEST_DETAIL = "housekeeping-request/{requestId}"

    fun workOrderDetail(id: Int) = "work-order/$id"
    fun equipmentDetail(id: Int) = "equipment/$id"
    fun maintenanceRequestDetail(id: Int) = "maintenance-request/$id"
    fun documentDetail(id: Int) = "document/$id"
    fun housekeepingDetail(id: Int) = "housekeeping/$id"
    fun housekeepingRequestDetail(id: Int) = "housekeeping-request/$id"
}
