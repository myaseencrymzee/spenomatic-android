package com.crymzee.spenomatic.model.response.expenses


import androidx.annotation.Keep

@Keep
data class Customer(
    val customer: CustomerX,
    val id: Int,
    val objective: String?
)