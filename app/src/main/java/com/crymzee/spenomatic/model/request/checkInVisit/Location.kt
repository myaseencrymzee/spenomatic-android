package com.crymzee.spenomatic.model.request.checkInVisit


import androidx.annotation.Keep

@Keep
data class Location(
    val coordinates: List<Double>,
    val type: String
)