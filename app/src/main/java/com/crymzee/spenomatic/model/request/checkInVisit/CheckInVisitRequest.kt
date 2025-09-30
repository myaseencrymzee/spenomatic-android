package com.crymzee.spenomatic.model.request.checkInVisit


import androidx.annotation.Keep

@Keep
data class CheckInVisitRequest(
    val check_in_time: String,
    val location: Location,
    val schedule_date: String,
    val status: String
)