package com.crymzee.spenomatic.repository


import com.crymzee.spenomatic.model.request.CreateLeaveRequest
import com.crymzee.spenomatic.model.request.OtherExpensesRequest
import com.crymzee.spenomatic.model.request.createFuelExpense.CreateFuelExpenseRequest
import com.crymzee.spenomatic.model.request.createLocalExpense.CreateLocalExpenseRequest
import com.crymzee.spenomatic.model.request.createOutsideExpense.CreateOutsideExpenseRequest
import com.crymzee.spenomatic.retrofit.ApiServices
import com.crymzee.spenomatic.state.networkBoundResource

class ExpensesRepository(private val apiServices: ApiServices) {

    fun executeGetAllExpenses(order: String, page: Int, perPage: Int) = networkBoundResource(
        fetch = {
            apiServices.getAllExpenses(order,"-id", page, perPage)
        }
    )

    fun executeCreateOtherExpenses(otherExpensesRequest: OtherExpensesRequest) = networkBoundResource(
        fetch = {
            apiServices.createOtherExpenses(otherExpensesRequest)
        }
    )
    fun executeCreateFuelExpenses(createFuelExpenseRequest: CreateFuelExpenseRequest) = networkBoundResource(
        fetch = {
            apiServices.createFuelExpenses(createFuelExpenseRequest)
        }
    )

    fun executeCreateLocalExpenses(createLocalExpenseRequest: CreateLocalExpenseRequest) = networkBoundResource(
        fetch = {
            apiServices.createLocalExpenses(createLocalExpenseRequest)
        }
    )

    fun executeCreateOutsideExpenses(createOutsideExpenseRequest: CreateOutsideExpenseRequest) = networkBoundResource(
        fetch = {
            apiServices.createOutsideExpenses(createOutsideExpenseRequest)
        }
    )
}