package com.crymzee.spenomatic.model.request


import androidx.annotation.Keep

@Keep
data class RefreshTokenRequestBody(
    val refresh: String
)