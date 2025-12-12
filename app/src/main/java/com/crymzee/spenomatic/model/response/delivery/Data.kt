package com.crymzee.spenomatic.model.response.delivery


import androidx.annotation.Keep

@Keep
data class Data(
    val customer: Customer?,
    val delivery_datetime: Any?,
    val expected_date: String,
    val address: String,
    val id: Int,
    val location: String,
    val notes: String,
    val remarks: String,
    val staff: Staff,
    val status: String
)