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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
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

    fun validateLodgingInput(
        fromDate: String,
        toDate: String,
        nights: String,
        nightAmount: String,
        amount: String
    ): Pair<Boolean, Int> {
        return when {
            fromDate.isEmpty() -> Pair(false, R.string.error_select_start_date)
            toDate.isEmpty() -> Pair(false, R.string.error_select_end_date)
            nights.isEmpty() -> Pair(false, R.string.error_enter_nights)
            nightAmount.isEmpty() -> Pair(false, R.string.error_enter_amount)
            amount.isEmpty() -> Pair(false, R.string.error_enter_total)
            else -> {
                val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                sdf.isLenient = false
                try {
                    val from = sdf.parse(fromDate)
                    val to = sdf.parse(toDate)

                    if (from != null && to != null) {
                        // Normalize both to ignore time part
                        val fromCal = Calendar.getInstance().apply {
                            time = from
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }

                        val toCal = Calendar.getInstance().apply {
                            time = to
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }

                        when {
                            fromCal.after(toCal) -> {
                                // ❌ fromDate is after toDate
                                Pair(false, R.string.error_invalid_date_range)
                            }
                            fromCal.timeInMillis == toCal.timeInMillis -> {
                                // ❌ fromDate and toDate are the same day
                                Pair(false, R.string.error_same_date_not_allowed)
                            }
                            else -> {
                                // ✅ valid range
                                Pair(true, R.string.empty_string)
                            }
                        }
                    } else {
                        Pair(false, R.string.error_invalid_date_range)
                    }
                } catch (e: Exception) {
                    Pair(false, R.string.error_invalid_date_range)
                }
            }
        }
    }






    fun validateBusTrainExpenseInput(
        date: String,
        time: String,
        amount: String
    ): Pair<Boolean, Int> {
        return when {
            date.isEmpty() -> Pair(false, R.string.error_select_date)        // Date required
            time.isEmpty() -> Pair(false, R.string.error_select_time)        // Time required
            amount.isEmpty() -> Pair(false, R.string.error_enter_amount)     // Amount required
            amount.toDoubleOrNull()?.let { it <= 0 } == true ->
                Pair(false, R.string.error_invalid_amount)                   // Amount > 0
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

    fun validateAllowanceInput(
        allowanceType: String,
        amount: String,
        description: String
    ): Pair<Boolean, Int> {
        return when {
            allowanceType.isEmpty() -> Pair(false, R.string.error_enter_allowance) // Type required
            amount.isEmpty() -> Pair(false, R.string.error_enter_amount) // Amount required
            amount.toDoubleOrNull()?.let { it <= 0 } == true -> Pair(false, R.string.error_invalid_amount) // Must be > 0
            description.isEmpty() -> Pair(false, R.string.error_enter_description) // Description required
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
            amount.toDoubleOrNull()?.let { it <= 0 } == true -> Pair(false, R.string.error_invalid_amount)
            mpesaTillNo.isNullOrEmpty() -> Pair(false, R.string.error_enter_mpesa_till_no)
            startMeter.isNullOrEmpty() -> Pair(false, R.string.error_enter_start_meter)
            endMeter.isNullOrEmpty() -> Pair(false, R.string.error_enter_end_meter)

            // 🔹 New check: end meter must be greater than start meter
            startMeter.toDoubleOrNull() != null && endMeter.toDoubleOrNull() != null &&
                    endMeter.toDouble() <= startMeter.toDouble() ->
                Pair(false, R.string.error_end_meter_must_be_greater)

            placesVisited.isNullOrEmpty() -> Pair(false, R.string.error_enter_places_visited)
            kilometers.isNullOrEmpty() -> Pair(false, R.string.error_enter_kilometers)
            else -> Pair(true, R.string.empty_string)
        }
    }




}