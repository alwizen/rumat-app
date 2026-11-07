package com.solu8i.cmms.ui.navigation

object Routes {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val HISTORY = "history"
    const val CALENDAR = "calendar"
    const val EQUIPMENT = "equipment"
    const val EQUIPMENT_DETAIL = "equipment/{equipmentId}"
    const val METER_LOG = "meter-log"
    const val DOWNTIME = "downtime"
    const val DOCUMENT = "document"
    const val MAINTENANCE_REQUEST = "maintenance-request"
    const val MY_TAB = "my-tab"
    const val NOTIFICATION = "notification"
    const val WORK_ORDER_DETAIL = "work-order/{workOrderId}"
    const val MAINTENANCE_REQUEST_DETAIL = "maintenance-request/{requestId}"
    const val MAINTENANCE_REQUEST_CREATE = "maintenance-request-create"

    fun workOrderDetail(id: Int) = "work-order/$id"
    fun equipmentDetail(id: Int) = "equipment/$id"
    fun maintenanceRequestDetail(id: Int) = "maintenance-request/$id"
}
