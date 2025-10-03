package com.crymzee.spenomatic.model.response.expenses


import androidx.annotation.Keep

@Keep
data class VisitX(
    val check_in_time: String,
    val check_out_time: String,
    val customer: Customer,
    val id: Int,
    val location: String,
    val location_picture: String,
    val remarks: String,
    val schedule_date: String,
    val status: String,
    val type: String,
    val visit_summary: String
)