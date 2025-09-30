package com.crymzee.spenomatic.model.response.allCustomers


import androidx.annotation.Keep

@Keep
data class Stats(
    val today_visits: Int,
    val weekly_visits: Int
)