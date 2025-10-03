package com.crymzee.spenomatic.model.request.pendingVisits


import androidx.annotation.Keep

@Keep
data class AllPendingVisitResponse(
    val `data`: List<Data>,
    val pagination: Pagination
)