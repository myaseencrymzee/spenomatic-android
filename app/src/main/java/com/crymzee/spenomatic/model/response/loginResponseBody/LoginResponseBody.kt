package com.crymzee.spenomatic.model.response.loginResponseBody


import androidx.annotation.Keep

@Keep
data class LoginResponseBody(
    val access: String,
    val refresh: String
)