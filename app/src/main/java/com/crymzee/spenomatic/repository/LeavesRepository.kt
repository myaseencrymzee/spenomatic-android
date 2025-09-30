package com.crymzee.spenomatic.repository


import com.crymzee.spenomatic.model.request.CreateLeaveRequest
import com.crymzee.spenomatic.retrofit.ApiServices
import com.crymzee.spenomatic.state.networkBoundResource

class LeavesRepository(private val apiServices: ApiServices) {

    fun executeGetAllLeaves(order: String, page: Int, perPage: Int) = networkBoundResource(
        fetch = {
            apiServices.getAllLeaves(order,"-id", page, perPage)
        }
    )

    fun executeCreateLeave(createLeaveRequest: CreateLeaveRequest) = networkBoundResource(
        fetch = {
            apiServices.createLeave(createLeaveRequest)
        }
    )
}