package com.crymzee.spenomatic.model.response.meResponse


import androidx.annotation.Keep

@Keep
data class Country(
    val code: String,
    val id: Int,
    val name: String,
    val currency: String? = null,
    val currency_symbol: String? = null
)