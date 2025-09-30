package com.crymzee.spenomatic.model.response.expenses


import androidx.annotation.Keep

@Keep
data class TransportExpense(
    val amount: Double,
    val from_location: String,
    val to_location: String
)