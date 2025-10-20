package com.crymzee.spenomatic.model.request


import androidx.annotation.Keep

@Keep
data class UpdateDeliveryRequest(
    val delivery_datetime: String,
    val status: String
)