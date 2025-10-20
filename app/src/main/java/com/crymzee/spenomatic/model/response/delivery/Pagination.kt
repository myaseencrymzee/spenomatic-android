package com.crymzee.spenomatic.model.response.delivery


import androidx.annotation.Keep

@Keep
data class Pagination(
    val count: Int,
    val currentPage: Int,
    val links: Links,
    val perPage: Int,
    val total: Int
)