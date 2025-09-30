package com.crymzee.spenomatic.model.response.visitsList


import androidx.annotation.Keep

@Keep
data class AllVisitListResponse(
    val `data`: List<Data>,
    val pagination: Pagination,
    val stats: Stats
)