package com.crymzee.spenomatic.model.request.pendingVisits


import androidx.annotation.Keep

@Keep
data class Links(
    val next: String?,
    val previous: String?
)