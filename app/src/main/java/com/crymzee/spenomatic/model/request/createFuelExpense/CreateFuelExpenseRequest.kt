package com.crymzee.spenomatic.model.request.createFuelExpense


import androidx.annotation.Keep

@Keep
data class CreateFuelExpenseRequest(
    val fuel_voucher_details: FuelVoucherDetails,
    val type: String
)