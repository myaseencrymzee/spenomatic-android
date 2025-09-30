package com.crymzee.spenomatic.model.response.allLeaves


import androidx.annotation.Keep

@Keep
data class Links(
    val next: String?,
    val previous: String?
)