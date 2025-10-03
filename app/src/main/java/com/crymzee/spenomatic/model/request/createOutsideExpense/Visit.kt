package com.crymzee.spenomatic.model.request.createOutsideExpense


import androidx.annotation.Keep
import com.crymzee.spenomatic.model.request.createLocalExpense.MiscellaneousExpense
import com.crymzee.spenomatic.model.request.createLocalExpense.TransportExpense

@Keep
data class Visit(
    val bus_train_expenses: List<BusTrainExpense>?,
    val lodging_boarding_expenses: List<LodgingBoardingExpense>?,
    val miscellaneous_expenses: List<MiscellaneousExpense>,
    val objective: String,
    val transport_expenses: List<TransportExpense>,
    val travel_allowances: List<TravelAllowance>?,
    val visit: Int
)