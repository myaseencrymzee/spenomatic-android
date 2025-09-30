package com.crymzee.spenomatic.model.request.createFuelExpense


import androidx.annotation.Keep

@Keep
data class FuelPumpLocation(
    val coordinates: List<Double>,
    val type: String
)