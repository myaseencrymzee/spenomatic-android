package com.crymzee.spenomatic.model.response.expenses


import androidx.annotation.Keep

@Keep
data class AllExpensesResponseBody(
    val `data`: List<Data>,
    val pagination: Pagination
)