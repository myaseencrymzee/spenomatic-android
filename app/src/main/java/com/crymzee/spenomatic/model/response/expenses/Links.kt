package com.crymzee.spenomatic.model.response.expenses


import androidx.annotation.Keep

@Keep
data class Links(
    val next: String?,
    val previous: String?
)