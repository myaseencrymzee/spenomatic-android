package com.crymzee.drivetalk.model.response.verification

import androidx.annotation.Keep
import com.crymzee.spenomatic.model.response.verification.Data

@Keep
data class OTPVerifyResponseBody(
    val `data`: Data,
    val success: Int
)