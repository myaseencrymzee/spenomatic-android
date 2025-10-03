package com.crymzee.spenomatic.model.request.createOutsideExpense


import androidx.annotation.Keep

@Keep
data class CreateOutsideExpenseRequest(
    val description: String,
    val type: String,
    val visits: List<Visit>
)