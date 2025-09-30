package com.crymzee.spenomatic.model.response.attendenceList


import androidx.annotation.Keep

@Keep
data class AttendanceListResponse(
    val `data`: List<Data>,
    val pagination: Pagination
)