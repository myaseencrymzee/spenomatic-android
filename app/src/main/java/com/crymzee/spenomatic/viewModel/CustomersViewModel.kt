package com.crymzee.spenomatic.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.crymzee.spenomatic.R
import com.crymzee.spenomatic.model.request.CreateCustomerRequestBody
import com.crymzee.spenomatic.model.request.pendingVisits.AllPendingVisitResponse
import com.crymzee.spenomatic.model.response.allCustomers.AllCustomersResponseBody
import com.crymzee.spenomatic.model.response.createCustomer.CreateCustomerResponse
import com.crymzee.spenomatic.model.response.customerDetail.Contact
import com.crymzee.spenomatic.model.response.customerDetail.CustomerDetailResponse
import com.crymzee.spenomatic.model.response.updateCustomer.UpdateCustomerResponse
import com.crymzee.spenomatic.repository.CustomersRepository
import com.crymzee.spenomatic.state.Resource
import com.crymzee.spenomatic.utils.isValidPhoneNumberOnly
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class CustomersViewModel @Inject constructor(private val customersRepository: CustomersRepository) :
    ViewModel() {

    var page = 1
    var perPage = 10
    var customerName = ""
    var lat = 0.0
    var lng = 0.0
    var industryType = ""
    var address = ""
    var visitFrequency = ""
    var noOfVisit = ""
    val contactList: MutableList<Contact> = mutableListOf()

    private var _getAllCustomersLiveData =
        MutableLiveData<Resource<out AllCustomersResponseBody>>()
    val getAllCustomersLiveData: LiveData<Resource<out AllCustomersResponseBody>> =
        _getAllCustomersLiveData


    private var _getAllCustomersVisitLiveData =
        MutableLiveData<Resource<out AllCustomersResponseBody>>()
    val getAllCustomersVisitLiveData: LiveData<Resource<out AllCustomersResponseBody>> =
        _getAllCustomersVisitLiveData



    private var _getAllPendingVisitLiveData =
        MutableLiveData<Resource<out AllPendingVisitResponse>>()
    val getAllPendingVisitLiveData: LiveData<Resource<out AllPendingVisitResponse>> =
        _getAllPendingVisitLiveData


    fun getAllCustomers() {
        viewModelScope.launch {
            customersRepository.executeGetAllCustomers(page, perPage).collect {
                _getAllCustomersLiveData.postValue(it)
            }
        }
    }


    fun getAllCustomersVisit() {
        viewModelScope.launch {
            customersRepository.executeGetAllCustomers(page, 999999).collect {
                _getAllCustomersVisitLiveData.postValue(it)
            }
        }
    }


    fun getAllPendingVisits() {
        viewModelScope.launch {
            customersRepository.executeGetAllPendingVisit(page, 999999).collect {
                _getAllPendingVisitLiveData.postValue(it)
            }
        }
    }


    fun getCustomerDetail(
        visitId: Int
    ): LiveData<Resource<out CustomerDetailResponse>> {
        return customersRepository.executeCustomerDetail(visitId).asLiveData()
    }

    fun addCustomer(
        createCustomerRequestBody: CreateCustomerRequestBody
    ): LiveData<Resource<out CreateCustomerResponse>> {
        return customersRepository.executeAddCustomers(createCustomerRequestBody).asLiveData()
    }

    fun updateCustomer(
        customerId: Int,
        createCustomerRequestBody: CreateCustomerRequestBody
    ): LiveData<Resource<out UpdateCustomerResponse>> {
        return customersRepository.executeUpdateCustomers(customerId, createCustomerRequestBody)
            .asLiveData()
    }


    fun deleteCustomer(id: Int): LiveData<Resource<Unit>> {
        return customersRepository.executeDeleteCustomer(id)
            .map { Resource.Success(Unit) } // Map response to Unit
            .asLiveData()
    }


    fun validateUserInput(
        name: String,
        designation: String,
        email: String,
        phone: String,
        onlyNumber: String
    ): Pair<Boolean, Int> {

        val emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()

        return when {
            name.isEmpty() -> Pair(false, R.string.error_enter_name) // Name required
            designation.isEmpty() -> Pair(
                false,
                R.string.error_enter_designation
            ) // Designation required
            onlyNumber.isEmpty() -> Pair(false, R.string.error_enter_phone) // Phone required
            !isValidPhoneNumberOnly(phone) -> { // Invalid phone format

                Pair(false, R.string.error_invalid_phone_number)
            }
            email.isEmpty() -> Pair(false, R.string.error_enter_email) // Email required
            !email.matches(emailPattern) -> Pair(
                false,
                R.string.error_invalid_email
            ) // Invalid email format


            else -> {
                Pair(true, R.string.empty_string)
            }
        }
    }


    fun validateCustomerInput(
        customerName: String,
        address: String,
        pinPoints: String,
        industryType: String,
        visit: String,
        numberOfVisitText: String, // Take raw string first
        contactsLength: Int
    ): Pair<Boolean, Int> {
        return when {
            customerName.isEmpty() -> Pair(
                false,
                R.string.error_enter_customer_name
            ) // Customer name required
            address.isEmpty() -> Pair(false, R.string.error_enter_address) // Address required
            pinPoints.isEmpty() -> Pair(false, R.string.error_enter_pinpoints) // Pinpoints required
            industryType.isEmpty() -> Pair(
                false,
                R.string.error_select_industry_type
            ) // Industry type required
            visit.isEmpty() -> Pair(false, R.string.error_select_visit_type) // Visit required
            numberOfVisitText.isEmpty() -> Pair(
                false,
                R.string.error_number_of_visit_empty
            ) // Field must not be empty
            numberOfVisitText.toIntOrNull() == null || numberOfVisitText.toInt() <= 0 ->
                Pair(false, R.string.error_number_of_visit_invalid) // Invalid number
            contactsLength < 1 -> Pair(
                false,
                R.string.error_atleast_one_contact
            ) // At least one contact required
            else -> Pair(true, R.string.empty_string) // All good
        }
    }


}