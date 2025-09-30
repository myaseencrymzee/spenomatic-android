package com.crymzee.spenomatic.model.request


import androidx.annotation.Keep

@Keep
data class CheckInRequestBody(
    val check_in: String,
    val check_in_location: Location
)