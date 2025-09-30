package com.crymzee.spenomatic.model.response.expenses


import androidx.annotation.Keep

@Keep
data class FuelVoucherDetails(
    val amount: Double,
    val driver_name: String,
    val end_meter_reading: Double,
    val fuel_in_liters: Double,
    val fuel_pump_location: FuelPumpLocation,
    val fuel_pump_name: String,
    val fuel_type: String,
    val km_travelled: Double,
    val places_visited: String,
    val start_meter_reading: Double,
    val till_number: String,
    val vehicle_number: String
)