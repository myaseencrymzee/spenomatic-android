package com.crymzee.spenomatic.repository


import com.crymzee.spenomatic.model.request.CheckInRequestBody
import com.crymzee.spenomatic.model.request.CheckOutRequestBody
import com.crymzee.spenomatic.model.request.CreateLeaveRequest
import com.crymzee.spenomatic.retrofit.ApiServices
import com.crymzee.spenomatic.state.networkBoundResource

class HomeRepository(private val apiServices: ApiServices) {


    fun executeCheckInUser(checkInRequestBody: CheckInRequestBody) = networkBoundResource(
        fetch = {
            apiServices.checkInApp(checkInRequestBody)
        }
    )
    fun executeCheckOutUser(checkOutRequestBody: CheckOutRequestBody) = networkBoundResource(
        fetch = {
            apiServices.checkOutApp(checkOutRequestBody)
        }
    )
    fun executeGetAttendance() = networkBoundResource(
        fetch = {
            apiServices.getAllLeaves()
        }
    )
}