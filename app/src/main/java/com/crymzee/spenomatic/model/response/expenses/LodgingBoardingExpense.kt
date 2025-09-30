package com.crymzee.spenomatic.model.response.expenses


import androidx.annotation.Keep

@Keep
data class LodgingBoardingExpense(
    val from_date: String,
    val nights_stayed: Int,
    val per_night_amount: Double,
    val to_date: String,
    val total_amount: Double
)