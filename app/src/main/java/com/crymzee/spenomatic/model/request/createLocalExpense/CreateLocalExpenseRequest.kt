package com.crymzee.spenomatic.model.request.createLocalExpense


import androidx.annotation.Keep

@Keep
data class CreateLocalExpenseRequest(
    val description: String,
    val type: String,
    val visits: List<Visit>
)