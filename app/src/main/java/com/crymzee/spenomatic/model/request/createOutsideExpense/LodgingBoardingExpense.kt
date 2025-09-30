package com.crymzee.spenomatic.model.request.createOutsideExpense


import androidx.annotation.Keep

@Keep
data class LodgingBoardingExpense(
    val from_date: String,
    val nights_stayed: Int,
    val per_night_amount: Int,
    val to_date: String,
    val total_amount: Int
)