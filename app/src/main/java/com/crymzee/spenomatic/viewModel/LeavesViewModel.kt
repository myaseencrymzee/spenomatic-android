package com.crymzee.spenomatic.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.crymzee.spenomatic.R
import com.crymzee.spenomatic.model.request.CreateLeaveRequest
import com.crymzee.spenomatic.model.response.allLeaves.AllLeavesResponseBody
import com.crymzee.spenomatic.model.response.createLeaveResponse.CreateLeaveResponseBody
import com.crymzee.spenomatic.repository.LeavesRepository
import com.crymzee.spenomatic.state.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class LeavesViewModel @Inject constructor(private val leavesRepository: LeavesRepository) :
    ViewModel() {

    var page = 1
    var perPage = 10
    var category = ""

    private var _getAllLeavesLiveData =
        MutableLiveData<Resource<out AllLeavesResponseBody>>()
    val getAllLeavesLiveData: LiveData<Resource<out AllLeavesResponseBody>> =
        _getAllLeavesLiveData


    fun getAllLeaves(order: String) {
        viewModelScope.launch {
            leavesRepository.executeGetAllLeaves(order, page, perPage).collect {
                _getAllLeavesLiveData.postValue(it)
            }
        }
    }

    fun createLeaves(
        createLeaveRequest: CreateLeaveRequest
    ): LiveData<Resource<out CreateLeaveResponseBody>> {
        return leavesRepository.executeCreateLeave(createLeaveRequest).asLiveData()
    }

    fun validateLeaveInput(
        leaveType: String?,
        startDate: String?,
        endDate: String?,
        reason: String?
    ): Pair<Boolean, Int> {

        return when {
            leaveType.isNullOrEmpty() -> Pair(
                false,
                R.string.error_select_leave_type
            ) // Leave type required
            startDate.isNullOrEmpty() -> Pair(
                false,
                R.string.error_select_start_date
            ) // Start date required
            endDate.isNullOrEmpty() -> Pair(
                false,
                R.string.error_select_end_date
            ) // End date required
            reason.isNullOrEmpty() -> Pair(
                false,
                R.string.error_enter_reason
            ) // Reason required
            else -> Pair(true, R.string.empty_string) // Valid
        }
    }


}