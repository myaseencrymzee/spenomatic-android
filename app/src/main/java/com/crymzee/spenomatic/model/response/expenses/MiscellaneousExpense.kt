package com.crymzee.spenomatic.model.response.expenses


import androidx.annotation.Keep

@Keep
data class MiscellaneousExpense(
    val amount: Double,
    val description: String,
    val objective: String
)