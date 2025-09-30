package com.crymzee.spenomatic.model.response.updateProfile


import androidx.annotation.Keep

@Keep
data class UpdateProfileResponse(
    val attendance: Any?,
    val email: String,
    val fullname: String,
    val id: Int,
    val join_date: String,
    val office: Office,
    val phone: String,
    val profile_picture: String,
    val status: String,
    val updated_at: String
)