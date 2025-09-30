package com.crymzee.spenomatic.model.request.createOutsideExpense


import androidx.annotation.Keep

@Keep
data class TravelAllowance(
    val allowance_type: String,
    val amount: Int,
    val description: String
)