package com.crymzee.spenomatic.model.response.allCustomers


import androidx.annotation.Keep

@Keep
data class AllCustomersResponseBody(
    val `data`: List<Data>,
    val pagination: Pagination,
    val stats: Stats
)