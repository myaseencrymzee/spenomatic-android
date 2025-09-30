package com.crymzee.spenomatic.model.response.expenses


import androidx.annotation.Keep

@Keep
data class Data(
    val amount: String,
    val bus_train_expenses: List<BusTrainExpense>?,
    val created_at: String,
    val customers: List<Customer>,
    val description: String,
    val fuel_voucher_details: FuelVoucherDetails?,
    val id: Int,
    val lodging_boarding_expenses: List<LodgingBoardingExpense>?,
    val miscellaneous_expenses: List<MiscellaneousExpense>?,
    val rejection_reason: String,
    val status: String,
    val transport_expenses: List<TransportExpense>?,
    val travel_allowances: List<TravelAllowance>?,
    val type: String,
    val updated_at: String
)