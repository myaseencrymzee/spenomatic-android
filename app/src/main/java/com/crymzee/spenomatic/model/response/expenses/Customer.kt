package com.crymzee.spenomatic.model.response.expenses


import androidx.annotation.Keep

@Keep
data class Customer(
    val address: String,
    val avg_visit_time: Double,
    val contacts: List<Contact>,
    val fullname: String,
    val id: Int,
    val industry_type: String,
    val latest_visit_date: String,
    val location: String,
    val number_of_visits: Int,
    val rejection_reason: String,
    val status: String,
    val visit_frequency: String
)