package com.crymzee.spenomatic.model.response.expenses


import androidx.annotation.Keep

@Keep
data class BusTrainExpense(
    val amount: Double,
    val date: String,
    val time: String
)