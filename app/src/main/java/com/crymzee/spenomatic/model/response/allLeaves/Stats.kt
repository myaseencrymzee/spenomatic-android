package com.crymzee.spenomatic.model.response.allLeaves


import androidx.annotation.Keep

@Keep
data class Stats(
    val annual_leaves: Double,
    val approved_leaves: Double,
    val leaves_left: Double,
    val pending_leaves: Int,
    val rejected_leaves: Int,
    val total_leaves: Int
)