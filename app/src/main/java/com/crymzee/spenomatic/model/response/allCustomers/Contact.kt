package com.crymzee.spenomatic.model.response.allCustomers


import androidx.annotation.Keep

@Keep
data class Contact(
    val designation: String,
    val email: String,
    val fullname: String,
    val id: Int,
    val phone: String
)