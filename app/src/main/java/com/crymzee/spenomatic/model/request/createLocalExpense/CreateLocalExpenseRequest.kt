package com.crymzee.spenomatic.model.request.createLocalExpense


import androidx.annotation.Keep

@Keep
data class CreateLocalExpenseRequest(
    val customers: List<Customer>,
    val description: String,
    val miscellaneous_expenses: List<MiscellaneousExpense>,
    val transport_expenses: List<TransportExpense>,
    val type: String
)