package com.crymzee.spenomatic.repository


import com.crymzee.spenomatic.model.request.CreateCustomerRequestBody
import com.crymzee.spenomatic.retrofit.ApiServices
import com.crymzee.spenomatic.state.networkBoundResource

class CustomersRepository(private val apiServices: ApiServices) {

    fun executeGetAllCustomers(page: Int, perPage: Int) = networkBoundResource(
        fetch = {
            apiServices.getAllCustomers("-id",page, perPage)
        }
    )
    fun executeAddCustomers( createCustomerRequestBody: CreateCustomerRequestBody) = networkBoundResource(
        fetch = {
            apiServices.addCustomer(createCustomerRequestBody)
        }
    )

    fun executeUpdateCustomers( customerId: Int,createCustomerRequestBody: CreateCustomerRequestBody) = networkBoundResource(
        fetch = {
            apiServices.updateCustomer(customerId,createCustomerRequestBody)
        }
    )

    fun executeDeleteCustomer(id: Int) = networkBoundResource(
        fetch = {
            apiServices.deleteCustomers(id)
        }
    )

    fun executeCustomerDetail(id: Int) = networkBoundResource(
        fetch = {
            apiServices.getSpecificCustomer(id)
        }
    )
}