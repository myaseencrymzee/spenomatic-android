package com.crymzee.spenomatic.model.request


import androidx.annotation.Keep

@Keep
data class CreateLeaveRequest(
    val end_date: String,
    val reason: String,
    val start_date: String,
    val type: String
)