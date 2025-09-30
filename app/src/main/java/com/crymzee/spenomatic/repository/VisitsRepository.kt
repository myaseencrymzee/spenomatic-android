package com.crymzee.spenomatic.repository


import com.crymzee.spenomatic.model.request.CreateVisitRequestBody
import com.crymzee.spenomatic.model.request.checkInVisit.CheckInVisitRequest
import com.crymzee.spenomatic.retrofit.ApiServices
import com.crymzee.spenomatic.state.networkBoundResource
import okhttp3.MultipartBody

class VisitsRepository(private val apiServices: ApiServices) {

    fun executeGetAllVisits(order: String, page: Int, perPage: Int) = networkBoundResource(
        fetch = {
            apiServices.getAllVisits(order, "-id",page, perPage)
        }
    )


    fun executeGetRecentVisits(order: Int, page: Int, perPage: Int) = networkBoundResource(
        fetch = {
            apiServices.getRecentVisit(order,"-id", page, perPage)
        }
    )

    fun executeCreateVisit(createVisitRequestBody: CreateVisitRequestBody) = networkBoundResource(
        fetch = {
            apiServices.createVisit(createVisitRequestBody)
        }
    )

    fun executeGetVisitDetail(visitId: Int) = networkBoundResource(
        fetch = {
            apiServices.getVisitDetail(visitId)
        }
    )

    fun executeCheckInVisit(visitId: Int, checkInVisitRequest: CheckInVisitRequest) =
        networkBoundResource(
            fetch = {
                apiServices.checkInVisit(visitId, checkInVisitRequest)
            }
        )

    fun executeCheckOutVisit(
        visitId: Int,
        status: MultipartBody.Part?,
        location_picture: MultipartBody.Part?,
        visit_summary: MultipartBody.Part?,
        check_out_time: MultipartBody.Part?,
    ) = networkBoundResource(
        fetch = {
            apiServices.checkOutVisit(
                visitId,
                status,
                location_picture,
                visit_summary,
                check_out_time,

                )
        }
    )
}