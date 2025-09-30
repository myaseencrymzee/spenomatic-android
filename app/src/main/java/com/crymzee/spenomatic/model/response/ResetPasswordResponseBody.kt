package com.crymzee.spenomatic.model.response


import androidx.annotation.Keep

@Keep
data class ResetPasswordResponseBody(
    val user: List<String>
)