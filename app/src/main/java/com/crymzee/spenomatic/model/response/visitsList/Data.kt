package com.crymzee.spenomatic.model.response.visitsList


import androidx.annotation.Keep

@Keep
data class Data(
    val check_in_time: Any?,
    val check_out_time: Any?,
    val customer: Customer,
    val id: Int,
    val location: String?,
    val location_picture: Any?,
    val remarks: String,
    val schedule_date: String,
    val staff: StaffX,
    val status: String,
    val type: String,
    val visit_summary: String
)