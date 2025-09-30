package com.crymzee.spenomatic.model.response.checkedInResponse


import androidx.annotation.Keep

@Keep
data class StaffX(
    val email: String,
    val fullname: String,
    val id: Int,
    val phone: String,
    val profile_picture: String,
    val role: String
)