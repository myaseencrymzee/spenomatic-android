package com.crymzee.spenomatic.model.response.dashboardData


import androidx.annotation.Keep

@Keep
data class TodayAttendance(
    val check_in: String,
    val check_in_location: String,
    val check_out: Any?,
    val check_out_location: Any?,
    val date: String,
    val id: Int,
    val status: String,
    val work_duration: Int
)