package com.crymzee.spenomatic.model.response.checkedInResponse


import androidx.annotation.Keep

@Keep
data class Data(
    val check_in_time: String,
    val check_out_time: Any?,
    val customer: Customer,
    val id: Int,
    val location: String,
    val location_picture: Any?,
    val remarks: String,
    val schedule_date: String,
    val staff: StaffX,
    val status: String,
    val visit_summary: String
)