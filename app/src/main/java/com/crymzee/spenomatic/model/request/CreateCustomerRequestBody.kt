package com.crymzee.spenomatic.model.request


import androidx.annotation.Keep
import com.crymzee.spenomatic.model.response.customerDetail.Contact

@Keep
data class CreateCustomerRequestBody(
    val address: String,
    val contacts: List<Contact>,
    val fullname: String,
    val industry_type: String,
    val location: Location,
    val number_of_visits: Int,
    val visit_frequency: String
)