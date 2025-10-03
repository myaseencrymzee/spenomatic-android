package com.crymzee.spenomatic.model.response.expenses


import androidx.annotation.Keep

@Keep
data class Data(
    val amount: String,
    val created_at: String,
    val description: String,
    val fuel_voucher_details: FuelVoucherDetails?,
    val id: Int,
    val rejection_reason: String,
    val status: String,
    val type: String,
    val updated_at: String,
    val visits: List<Visit>
)