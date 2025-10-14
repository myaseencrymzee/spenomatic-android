package com.crymzee.spenomatic.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.crymzee.spenomatic.R
import com.crymzee.spenomatic.model.request.CheckInRequestBody
import com.crymzee.spenomatic.model.request.CheckOutRequestBody
import com.crymzee.spenomatic.model.request.CreateLeaveRequest
import com.crymzee.spenomatic.model.response.allCustomers.AllCustomersResponseBody
import com.crymzee.spenomatic.model.response.allLeaves.AllLeavesResponseBody
import com.crymzee.spenomatic.model.response.attendenceList.AttendanceListResponse
import com.crymzee.spenomatic.model.response.createLeaveResponse.CreateLeaveResponseBody
import com.crymzee.spenomatic.model.response.createdVisitResponse.CreatedVisitResponse
import com.crymzee.spenomatic.model.response.dashboardData.DashboardDataResponse
import com.crymzee.spenomatic.repository.HomeRepository
import com.crymzee.spenomatic.repository.LeavesRepository
import com.crymzee.spenomatic.state.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(private val homeRepository: HomeRepository) :
    ViewModel() {

    var page = 1
    var perPage = 10


    private var _getAllAttendanceLiveData =
        MutableLiveData<Resource<out AttendanceListResponse>>()
    val getAllAttendanceLiveData: LiveData<Resource<out AttendanceListResponse>> =
        _getAllAttendanceLiveData



    private var _getDashboardDataLiveData =
        MutableLiveData<Resource<out DashboardDataResponse>>()
    val getDashboardDataLiveData: LiveData<Resource<out DashboardDataResponse>> =
        _getDashboardDataLiveData

    fun checkInUser(
        checkInRequestBody: CheckInRequestBody
    ): LiveData<Resource<out CreatedVisitResponse>> {
        return homeRepository.executeCheckInUser(checkInRequestBody).asLiveData()
    }


    fun checkOutUser(
        checkOutRequestBody: CheckOutRequestBody
    ): LiveData<Resource<out CreatedVisitResponse>> {
        return homeRepository.executeCheckOutUser(checkOutRequestBody).asLiveData()
    }

    fun getAllAttendance() {
        viewModelScope.launch {
            homeRepository.executeGetAttendance("","").collect {
                _getAllAttendanceLiveData.postValue(it)
            }
        }
    }


    fun getAllDashboardData() {
        viewModelScope.launch {
            homeRepository.executeGetDashBoardData().collect {
                _getDashboardDataLiveData.postValue(it)
            }
        }
    }


}