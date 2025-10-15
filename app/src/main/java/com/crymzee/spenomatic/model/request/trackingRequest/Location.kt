package com.crymzee.spenomatic.model.request.trackingRequest


import androidx.annotation.Keep

@Keep
data class Location(
    val coordinates: List<Double>,
    val type: String
)