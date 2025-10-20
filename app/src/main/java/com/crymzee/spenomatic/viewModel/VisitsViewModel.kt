package com.crymzee.spenomatic.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.crymzee.spenomatic.R
import com.crymzee.spenomatic.model.request.CreateVisitRequestBody
import com.crymzee.spenomatic.model.request.UpdateDeliveryRequest
import com.crymzee.spenomatic.model.request.checkInVisit.CheckInVisitRequest
import com.crymzee.spenomatic.model.response.checkOutResponse.CheckOutResponse
import com.crymzee.spenomatic.model.response.checkedInResponse.CheckInResponseBody
import com.crymzee.spenomatic.model.response.createdVisitResponse.CreatedVisitResponse
import com.crymzee.spenomatic.model.response.delivery.AllDeliveryResponseBody
import com.crymzee.spenomatic.model.response.updateDelivery.UpdateDeliveryResponse
import com.crymzee.spenomatic.model.response.updateProfile.UpdateProfileResponse
import com.crymzee.spenomatic.model.response.visitDetail.VisitDetailResponseBody
import com.crymzee.spenomatic.model.response.visitsList.AllVisitListResponse
import com.crymzee.spenomatic.repository.VisitsRepository
import com.crymzee.spenomatic.state.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject


@HiltViewModel
class VisitsViewModel @Inject constructor(private val visitsRepository: VisitsRepository) :
    ViewModel() {

    var page = 1
    var perPage = 10
    var filePathProfile: String? = null
    var selectedTab = "pending,checked_in"
    var status: String? = null
    var visitSummary: String? = null
    var checkOutTime: String? = null

    private var _getAllVisitLiveData =
        MutableLiveData<Resource<out AllVisitListResponse>>()
    val getAllVisitLiveData: LiveData<Resource<out AllVisitListResponse>> =
        _getAllVisitLiveData


    private var _getAllRecentVisitLiveData =
        MutableLiveData<Resource<out AllVisitListResponse>>()
    val getAllRecentVisitLiveData: LiveData<Resource<out AllVisitListResponse>> =
        _getAllRecentVisitLiveData



    private var _getAllDeliveryLiveData =
        MutableLiveData<Resource<out AllDeliveryResponseBody>>()
    val getAllDeliveryLiveData: LiveData<Resource<out AllDeliveryResponseBody>> =
        _getAllDeliveryLiveData


    fun getAllVisit(order: String) {
        viewModelScope.launch {
            visitsRepository.executeGetAllVisits(order, page, perPage).collect {
                _getAllVisitLiveData.postValue(it)
            }
        }
    }
    fun getRecentVisit(visitId: Int) {
        viewModelScope.launch {
            visitsRepository.executeGetRecentVisits(visitId, page, perPage).collect {
                _getAllRecentVisitLiveData.postValue(it)
            }
        }
    }

    fun getAllDelivery(status: String) {
        viewModelScope.launch {
            visitsRepository.executeGetAllDelivery(status, page, perPage).collect {
                _getAllDeliveryLiveData.postValue(it)
            }
        }
    }

    fun createVisit(
        createVisitRequestBody: CreateVisitRequestBody
    ): LiveData<Resource<out CreatedVisitResponse>> {
        return visitsRepository.executeCreateVisit(createVisitRequestBody).asLiveData()
    }



    fun updateDelivery(
        deliveryId: Int, updateDeliveryRequest: UpdateDeliveryRequest
    ): LiveData<Resource<out UpdateDeliveryResponse>> {
        return visitsRepository.executeUpdateDelivery(deliveryId,updateDeliveryRequest).asLiveData()
    }


    fun getVisitDetail(
        visitId: Int
    ): LiveData<Resource<out VisitDetailResponseBody>> {
        return visitsRepository.executeGetVisitDetail(visitId).asLiveData()
    }


    fun checkInVisit(
        visitId: Int,
        checkInVisitRequest: CheckInVisitRequest
    ): LiveData<Resource<out CheckInResponseBody>> {
        return visitsRepository.executeCheckInVisit(visitId,checkInVisitRequest).asLiveData()
    }


    fun checkOutVisit(visitId: Int): LiveData<Resource<out CheckOutResponse>> {
        var sendStatus: MultipartBody.Part? = null
        var send_visit_summary: MultipartBody.Part? = null
        var send_check_out_time: MultipartBody.Part? = null
        var sendProfile_picture: MultipartBody.Part? = null

        filePathProfile?.let {
            val file: File = org.apache.commons.io.FileUtils.getFile(it)
            val requestFile = file.asRequestBody("media/*".toMediaTypeOrNull())
            sendProfile_picture = MultipartBody.Part.createFormData(
                "location_picture",
                file.name,
                requestFile
            )
        }
        status?.let {
            sendStatus =
                MultipartBody.Part.createFormData("status", it)
        }
        visitSummary?.let {
            send_visit_summary =
                MultipartBody.Part.createFormData("visit_summary", it)
        }
        checkOutTime?.let {
            send_check_out_time =
                MultipartBody.Part.createFormData("check_out_time", it)
        }


        return visitsRepository.executeCheckOutVisit(
            visitId,
            sendStatus,
            sendProfile_picture,
            send_visit_summary,
            send_check_out_time

        )
            .asLiveData()
    }

    fun validateScheduleInput(
        customerId: Int?,
        scheduleDate: String?,
        visitType: String?,
        remarks: String?
    ): Pair<Boolean, Int> {

        return when {
            customerId == null || customerId <= 0 -> Pair(
                false,
                R.string.error_select_customer
            ) // Customer required
            scheduleDate.isNullOrEmpty() -> Pair(
                false,
                R.string.error_enter_schedule_date
            ) // Date required
            visitType.isNullOrEmpty() -> Pair(
                false,
                R.string.error_enter_visit_type
            ) // Visit type required
            remarks.isNullOrEmpty() -> Pair(
                false,
                R.string.error_enter_remarks
            ) // Remarks required
            else -> Pair(true, R.string.empty_string) // Valid
        }
    }


}