package com.crymzee.spenomatic.model.request


import androidx.annotation.Keep

@Keep
data class LoginRequestBody(
    val email: String,
    val password: String,
    val role: String
)