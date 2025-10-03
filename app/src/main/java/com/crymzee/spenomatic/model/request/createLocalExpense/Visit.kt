package com.crymzee.spenomatic.model.request.createLocalExpense


import androidx.annotation.Keep

@Keep
data class Visit(
    val miscellaneous_expenses: List<MiscellaneousExpense>,
    val objective: String,
    val transport_expenses: List<TransportExpense>,
    val visit: Int
)