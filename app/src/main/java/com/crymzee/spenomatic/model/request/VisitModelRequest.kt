package com.crymzee.spenomatic.model.request


import androidx.annotation.Keep

@Keep
data class VisitModelRequest(
    val id: Int,
    val name: String,
    val address: String,
    val objective: String?,
    val remark: String?,
    val date: String?,
)