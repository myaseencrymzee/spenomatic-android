package com.crymzee.spenomatic.model.response.delivery


import androidx.annotation.Keep

@Keep
data class AllDeliveryResponseBody(
    val `data`: List<Data>,
    val pagination: Pagination
)