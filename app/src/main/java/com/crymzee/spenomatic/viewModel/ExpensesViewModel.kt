package com.crymzee.spenomatic.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.crymzee.spenomatic.R
import com.crymzee.spenomatic.model.request.CreateLeaveRequest
import com.crymzee.spenomatic.model.request.OtherExpensesRequest
import com.crymzee.spenomatic.model.request.createFuelExpense.CreateFuelExpenseRequest
import com.crymzee.spenomatic.model.request.createLocalExpense.CreateLocalExpenseRequest
import com.crymzee.spenomatic.model.request.createOutsideExpense.CreateOutsideExpenseRequest
import com.crymzee.spenomatic.model.response.allLeaves.AllLeavesResponseBody
import com.crymzee.spenomatic.model.response.createLeaveResponse.CreateLeaveResponseBody
import com.crymzee.spenomatic.model.response.expenses.AllExpensesResponseBody
import com.crymzee.spenomatic.model.response.expenses.Data
import com.crymzee.spenomatic.repository.ExpensesRepository
import com.crymzee.spenomatic.repository.LeavesRepository
import com.crymzee.spenomatic.state.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ExpensesViewModel @Inject constructor(private val expensesRepository: ExpensesRepository) :
    ViewModel() {

    var page = 1
    var perPage = 10
    var category = ""

    private var _getAllExpensesLiveData =
        MutableLiveData<Resource<out AllExpensesResponseBody>>()
    val getAllExpensesLiveData: LiveData<Resource<out AllExpensesResponseBody>> =
        _getAllExpensesLiveData


    fun getAllExpenses(order: String) {
        viewModelScope.launch {
            expensesRepository.executeGetAllExpenses(order, page, perPage).collect {
                _getAllExpensesLiveData.postValue(it)
            }
        }
    }

    fun createOtherExpenses(
        otherExpensesRequest: OtherExpensesRequest
    ): LiveData<Resource<out Data>> {
        return expensesRepository.executeCreateOtherExpenses(otherExpensesRequest).asLiveData()
    }


    fun createFuelExpenses(
        createFuelExpenseRequest: CreateFuelExpenseRequest
    ): LiveData<Resource<out Data>> {
        return expensesRepository.executeCreateFuelExpenses(createFuelExpenseRequest).asLiveData()
    }



    fun createLocalExpenses(
        createLocalExpenseRequest: CreateLocalExpenseRequest
    ): LiveData<Resource<out Data>> {
        return expensesRepository.executeCreateLocalExpenses(createLocalExpenseRequest).asLiveData()
    }



    fun createOutsideExpenses(
        createOutsideExpenseRequest: CreateOutsideExpenseRequest
    ): LiveData<Resource<out Data>> {
        return expensesRepository.executeCreateOutsideExpenses(createOutsideExpenseRequest).asLiveData()
    }


    fun validateUserInput(
        fromM: String,
        toM: String,
        amount: String
    ): Pair<Boolean, Int> {


        return when {
            fromM.isEmpty() -> Pair(false, R.string.error_enter_from) // From required
            toM.isEmpty() -> Pair(false, R.string.error_enter_to) // To required
            amount.isEmpty() -> Pair(false, R.string.error_enter_amount) // Amount required
            amount.toDoubleOrNull()?.let { it <= 0 } == true -> Pair(false, R.string.error_invalid_amount) // Amount > 0 check

            else -> Pair(true, R.string.empty_string)
        }
    }


    fun validateMiscellaneousInput(
        fromM: String,
        toM: String,
        amount: String
    ): Pair<Boolean, Int> {


        return when {
            fromM.isEmpty() -> Pair(false, R.string.error_enter_objective) // From required
            toM.isEmpty() -> Pair(false, R.string.error_enter_description) // To required
            amount.isEmpty() -> Pair(false, R.string.error_enter_amount) // Amount required
            amount.toDoubleOrNull()?.let { it <= 0 } == true -> Pair(false, R.string.error_invalid_amount) // Amount > 0 check

            else -> Pair(true, R.string.empty_string)
        }
    }

    fun validateFuelVoucherInput(
        driverName: String?,
        vehicleNo: String?,
        fuelType: String?,
        fuelPumpName: String?,
        fuelPumpLocation: String?,
        fuelFilled: String?,
        amount: String?,
        mpesaTillNo: String?,
        startMeter: String?,
        endMeter: String?,
        placesVisited: String?,
        kilometers: String?
    ): Pair<Boolean, Int> {
        return when {
            driverName.isNullOrEmpty() -> Pair(false, R.string.error_enter_driver_name)
            vehicleNo.isNullOrEmpty() -> Pair(false, R.string.error_enter_vehicle_no)
            fuelType.isNullOrEmpty() -> Pair(false, R.string.error_select_fuel_type)
            fuelFilled.isNullOrEmpty() -> Pair(false, R.string.error_enter_fuel_filled)
            fuelPumpName.isNullOrEmpty() -> Pair(false, R.string.error_enter_fuel_pump_name)
            fuelPumpLocation.isNullOrEmpty() -> Pair(false, R.string.error_select_fuel_pump_location)
            amount.isNullOrEmpty() -> Pair(false, R.string.error_enter_amount)
            amount.toDoubleOrNull()?.let { it <= 0 } == true -> Pair(false, R.string.error_invalid_amount) // Amount > 0 check
            mpesaTillNo.isNullOrEmpty() -> Pair(false, R.string.error_enter_mpesa_till_no)
            startMeter.isNullOrEmpty() -> Pair(false, R.string.error_enter_start_meter)
            endMeter.isNullOrEmpty() -> Pair(false, R.string.error_enter_end_meter)
            placesVisited.isNullOrEmpty() -> Pair(false, R.string.error_enter_places_visited)
            kilometers.isNullOrEmpty() -> Pair(false, R.string.error_enter_kilometers)
            else -> Pair(true, R.string.empty_string) // ✅ valid
        }
    }



}