package com.crymzee.spenomatic.model.request


import androidx.annotation.Keep

@Keep
data class ForgotRequestBody(
    val email: String,
    val otp_type: String,
    val role: String,
)