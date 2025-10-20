package com.crymzee.spenomatic.model.response.updateDelivery


import androidx.annotation.Keep

@Keep
data class Contact(
    val designation: String,
    val email: String,
    val fullname: String,
    val id: Int,
    val phone: String
)