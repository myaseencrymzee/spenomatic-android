package com.crymzee.spenomatic.model.request


import androidx.annotation.Keep

@Keep
data class CreateVisitRequestBody(
    val customer: Int,
    val remarks: String,
    val schedule_date: String,
    val type: String
)