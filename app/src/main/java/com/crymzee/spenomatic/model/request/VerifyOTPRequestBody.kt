package com.crymzee.spenomatic.model.request


import androidx.annotation.Keep

@Keep
data class VerifyOTPRequestBody(
    val email: String,
    val otp_code: String,
    val otp_type: String,
    val role: String,
)