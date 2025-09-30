package com.crymzee.spenomatic.model.response.createCustomer


import androidx.annotation.Keep

@Keep
data class Staff(
    val email: String,
    val fullname: String,
    val id: Int,
    val phone: String,
    val profile_picture: String,
    val role: String
)