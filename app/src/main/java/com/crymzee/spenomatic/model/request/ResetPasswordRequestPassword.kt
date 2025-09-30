package com.crymzee.spenomatic.model.request


import androidx.annotation.Keep

@Keep
data class ResetPasswordRequestPassword(
    val email: String,
    val password: String,
    val confirm_password: String,
    val verification_token: String,
    val role: String,
)