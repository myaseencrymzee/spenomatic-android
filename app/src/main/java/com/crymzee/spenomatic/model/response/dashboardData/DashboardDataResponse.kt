package com.crymzee.spenomatic.model.response.dashboardData


import androidx.annotation.Keep

@Keep
data class DashboardDataResponse(
    val active_time: Int,
    val approved_expenses: Double,
    val approved_leaves: Double,
    val full_day_leaves: Int,
    val half_day_leaves: Int,
    val idle_time: Int,
    val leaves_left: Double,
    val pending_expenses: Double,
    val rejected_expenses: Double,
    val today_attendance: TodayAttendance,
    val today_distance: Int,
    val total_absent: Int,
    val total_distance: Int,
    val total_present: Int
)