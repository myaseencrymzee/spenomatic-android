package com.crymzee.spenomatic.model.response.meResponse


import androidx.annotation.Keep

@Keep
data class Office(
    val buffer_zone: Int,
    val country: Country,
    val end_time: String,
    val id: Int,
    val location: String,
    val start_time: String
)