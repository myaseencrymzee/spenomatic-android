package com.crymzee.spenomatic.model.response.attendenceList


import androidx.annotation.Keep

@Keep
data class Data(
    val check_in: String,
    val check_in_location: String,
    val check_out: String?,
    val check_out_location: String?,
    val date: String,
    val id: Int,
    val status: String,
    val work_duration: Int
)