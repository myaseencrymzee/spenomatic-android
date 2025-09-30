package com.crymzee.spenomatic.model.request.createFuelExpense


import androidx.annotation.Keep

@Keep
data class FuelVoucherDetails(
    val amount: String,
    val driver_name: String,
    val fuel_in_liters: String,
    val fuel_pump_location: FuelPumpLocation,
    val fuel_pump_name: String,
    val fuel_type: String,
    val km_travelled: String,
    val places_visited: String,
    val start_meter_reading: String,
    val till_number: String,
    val vehicle_number: String
)