package com.crymzee.spenomatic.model.request.createOutsideExpense


import androidx.annotation.Keep

@Keep
data class LodgingBoardingExpense(
    val from_date: String,
    val nights_stayed: String,
    val per_night_amount: String,
    val to_date: String,
    val total_amount: String
)