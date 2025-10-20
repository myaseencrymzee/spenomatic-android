package com.crymzee.spenomatic.model.response.updateDelivery


import androidx.annotation.Keep

@Keep
data class Data(
    val customer: Customer,
    val delivery_datetime: String,
    val expected_date: String,
    val id: Int,
    val location: String,
    val notes: String,
    val remarks: String,
    val staff: Staff,
    val status: String
)