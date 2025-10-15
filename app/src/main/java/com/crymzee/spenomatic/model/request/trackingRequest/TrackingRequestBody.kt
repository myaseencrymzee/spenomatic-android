package com.crymzee.spenomatic.model.request.trackingRequest


import androidx.annotation.Keep

@Keep
data class TrackingRequestBody(
    val attendance: Int,
    val location: Location
)