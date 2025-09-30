package com.crymzee.spenomatic.model.response.allLeaves


import androidx.annotation.Keep

@Keep
data class Data(
    val end_date: String,
    val id: Int,
    val reason: String,
    val rejection_reason: String,
    val start_date: String,
    val status: String,
    val type: String
)