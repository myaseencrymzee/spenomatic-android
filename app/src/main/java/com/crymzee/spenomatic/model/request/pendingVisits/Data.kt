package com.crymzee.spenomatic.model.request.pendingVisits


import androidx.annotation.Keep

@Keep
data class Data(
    val check_in_time: String,
    val check_out_time: String?,
    val customer: Customer,
    val id: Int,
    val location: String,
    val location_picture: String?,
    val remarks: String,
    val schedule_date: String,
    val status: String,
    val type: String,
    val visit_summary: String
)