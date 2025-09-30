package com.crymzee.spenomatic.model.request.createLocalExpense


import androidx.annotation.Keep

@Keep
data class MiscellaneousExpense(
    val amount: String,
    val description: String,
    val objective: String
)