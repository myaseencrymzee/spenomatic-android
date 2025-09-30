package com.crymzee.spenomatic.model.response.allLeaves


import androidx.annotation.Keep

@Keep
data class AllLeavesResponseBody(
    val `data`: List<Data>,
    val pagination: Pagination,
    val stats: Stats
)