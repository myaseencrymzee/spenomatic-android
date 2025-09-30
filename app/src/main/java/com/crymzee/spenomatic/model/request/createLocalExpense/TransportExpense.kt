package com.crymzee.spenomatic.model.request.createLocalExpense


import androidx.annotation.Keep

@Keep
data class TransportExpense(
    val amount: String,
    val from_location: String,
    val to_location: String
)