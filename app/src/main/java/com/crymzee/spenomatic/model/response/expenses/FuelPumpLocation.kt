package com.crymzee.spenomatic.model.response.expenses


import androidx.annotation.Keep

@Keep
data class FuelPumpLocation(
    val coordinates: List<Double>,
    val type: String
)