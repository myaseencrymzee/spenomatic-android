package com.crymzee.spenomatic.model.response


import androidx.annotation.Keep

@Keep
data class ForgotPasswordResponseBody(
    val message: String,
    val success: Int
)