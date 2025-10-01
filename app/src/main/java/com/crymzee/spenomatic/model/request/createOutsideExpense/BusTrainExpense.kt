package com.crymzee.spenomatic.model.request.createOutsideExpense


import androidx.annotation.Keep

@Keep
data class BusTrainExpense(
    val amount: String,
    val date: String,
    val time: String
)