package com.crymzee.spenomatic.model.response.visitsList


import androidx.annotation.Keep

@Keep
data class Stats(
    val avg_visit_time: String,
    val total_visits_month: Int,
    val total_visits_today: Int
)