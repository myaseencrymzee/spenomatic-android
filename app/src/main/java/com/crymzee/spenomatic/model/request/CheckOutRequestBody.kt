package com.crymzee.spenomatic.model.request


import androidx.annotation.Keep

@Keep
data class CheckOutRequestBody(
    val check_out: String,
    val check_out_location: Location
)