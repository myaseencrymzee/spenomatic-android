package com.crymzee.spenomatic.model.request.createLocalExpense


import androidx.annotation.Keep

@Keep
data class Customer(
    val customer: Int,
    val objective: String?,
    val name: String?,
    val email: String?
)