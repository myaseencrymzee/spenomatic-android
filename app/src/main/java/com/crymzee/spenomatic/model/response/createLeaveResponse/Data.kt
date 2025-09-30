package com.crymzee.spenomatic.model.response.createLeaveResponse


import androidx.annotation.Keep

@Keep
data class Data(
    val end_date: String,
    val id: Int,
    val leaves_left: Double,
    val reason: String,
    val rejection_reason: String,
    val start_date: String,
    val status: String,
    val type: String
)