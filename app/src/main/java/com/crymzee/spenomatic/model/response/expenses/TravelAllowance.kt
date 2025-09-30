package com.crymzee.spenomatic.model.response.expenses


import androidx.annotation.Keep

@Keep
data class TravelAllowance(
    val allowance_type: String,
    val amount: Double,
    val description: String
)