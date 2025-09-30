package com.crymzee.spenomatic.model.response.customerDetail


import androidx.annotation.Keep

@Keep
data class Data(
    val address: String,
    val contacts: List<Contact>,
    val fullname: String,
    val id: Int,
    val industry_type: String,
    val location: String,
    val number_of_visits: Int,
    val rejection_reason: String,
    val staff: Staff,
    val status: String,
    val visit_frequency: String
)