package com.crymzee.spenomatic.model.response.expenses


import androidx.annotation.Keep

@Keep
data class Visit(
    val bus_train_expenses: List<BusTrainExpense>?,
    val id: Int,
    val lodging_boarding_expenses: List<LodgingBoardingExpense>?,
    val miscellaneous_expenses: List<MiscellaneousExpense>,
    val objective: String,
    val transport_expenses: List<TransportExpense>,
    val travel_allowances: List<TravelAllowance>?,
    val visit: VisitX
)