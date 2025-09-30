package com.crymzee.spenomatic.model.request


import androidx.annotation.Keep

@Keep
data class OtherExpensesRequest(
    val amount: Int,
    val description: String
)