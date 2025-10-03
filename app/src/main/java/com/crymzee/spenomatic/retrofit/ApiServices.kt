package com.crymzee.spenomatic.retrofit

import com.crymzee.drivetalk.model.response.verification.OTPVerifyResponseBody
import com.crymzee.spenomatic.model.request.CheckInRequestBody
import com.crymzee.spenomatic.model.request.CheckOutRequestBody
import com.crymzee.spenomatic.model.request.CreateCustomerRequestBody
import com.crymzee.spenomatic.model.request.CreateLeaveRequest
import com.crymzee.spenomatic.model.request.CreateVisitRequestBody
import com.crymzee.spenomatic.model.request.ForgotRequestBody
import com.crymzee.spenomatic.model.request.LoginRequestBody
import com.crymzee.spenomatic.model.request.OtherExpensesRequest
import com.crymzee.spenomatic.model.request.RefreshTokenRequestBody
import com.crymzee.spenomatic.model.request.ResetPasswordRequestPassword
import com.crymzee.spenomatic.model.request.VerifyOTPRequestBody
import com.crymzee.spenomatic.model.request.checkInVisit.CheckInVisitRequest
import com.crymzee.spenomatic.model.request.createFuelExpense.CreateFuelExpenseRequest
import com.crymzee.spenomatic.model.request.createLocalExpense.CreateLocalExpenseRequest
import com.crymzee.spenomatic.model.request.createOutsideExpense.CreateOutsideExpenseRequest
import com.crymzee.spenomatic.model.request.pendingVisits.AllPendingVisitResponse
import com.crymzee.spenomatic.model.response.ForgotPasswordResponseBody
import com.crymzee.spenomatic.model.response.RefreshResponseBody
import com.crymzee.spenomatic.model.response.ResetPasswordResponseBody
import com.crymzee.spenomatic.model.response.allCustomers.AllCustomersResponseBody
import com.crymzee.spenomatic.model.response.allLeaves.AllLeavesResponseBody
import com.crymzee.spenomatic.model.response.attendenceList.AttendanceListResponse
import com.crymzee.spenomatic.model.response.checkOutResponse.CheckOutResponse
import com.crymzee.spenomatic.model.response.checkedInResponse.CheckInResponseBody
import com.crymzee.spenomatic.model.response.createCustomer.CreateCustomerResponse
import com.crymzee.spenomatic.model.response.createLeaveResponse.CreateLeaveResponseBody
import com.crymzee.spenomatic.model.response.createdVisitResponse.CreatedVisitResponse
import com.crymzee.spenomatic.model.response.customerDetail.CustomerDetailResponse
import com.crymzee.spenomatic.model.response.expenses.AllExpensesResponseBody
import com.crymzee.spenomatic.model.response.expenses.Data
import com.crymzee.spenomatic.model.response.loginResponseBody.LoginResponseBody
import com.crymzee.spenomatic.model.response.meResponse.MeResponseBody
import com.crymzee.spenomatic.model.response.updateCustomer.UpdateCustomerResponse
import com.crymzee.spenomatic.model.response.updateProfile.UpdateProfileResponse
import com.crymzee.spenomatic.model.response.visitDetail.VisitDetailResponseBody
import com.crymzee.spenomatic.model.response.visitsList.AllVisitListResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query


interface ApiServices {


    @POST("auth/login")
    suspend fun loginUser(
        @Body loginRequestBody: LoginRequestBody
    ): LoginResponseBody

    /*refresh token*/
    @POST("auth/refresh")
    suspend fun refreshTokens(
        @Body refreshTokenRequestBody: RefreshTokenRequestBody
    ): RefreshResponseBody

    @GET("me")
    suspend fun getMe(): MeResponseBody


    @PATCH("auth/otp/verify")
    suspend fun verifyOTP(
        @Body verifyOTPRequestBody: VerifyOTPRequestBody
    ): OTPVerifyResponseBody

    @POST("auth/forgot-password")
    suspend fun resetPassword(
        @Body resetPasswordRequestPassword: ResetPasswordRequestPassword
    ): ResetPasswordResponseBody


//    @PATCH("password/change")
//    suspend fun updateUserPassword(
//        @Body updatePasswordRequestBody: UpdatePasswordRequestBody
//    ): PasswordUpdateResponseBody

    @POST("auth/otp/send")
    suspend fun forgotPassword(
        @Body forgotRequestBody: ForgotRequestBody
    ): ForgotPasswordResponseBody


//    //delete fcm from server
//    @DELETE("me/")
//    suspend fun deleteUser(): Response<Unit>


    @PATCH("me")
    @Multipart
    suspend fun updateProfile(
        @Part name: MultipartBody.Part?,
        @Part phone: MultipartBody.Part?,
        @Part current_password: MultipartBody.Part?,
        @Part new_password: MultipartBody.Part?,
        @Part confirm_password: MultipartBody.Part?,
    ): UpdateProfileResponse


    /*---------------------------  Customers APis  ----------------- */
    @GET("customers")
    suspend fun getAllCustomers(
        @Query("ordering") ordering: String,
        @Query("page") page: Int,
        @Query("perPage") perPage: Int
    ): AllCustomersResponseBody

    @GET("expenses/pending_visits")
    suspend fun getAllExpensesVisits(
        @Query("ordering") ordering: String,
        @Query("page") page: Int,
        @Query("perPage") perPage: Int
    ): AllPendingVisitResponse

    @GET("customers/{id}")
    suspend fun getSpecificCustomer(
        @Path("id") page: Int,
    ): CustomerDetailResponse

    @POST("customers")
    suspend fun addCustomer(
        @Body createCustomerRequestBody: CreateCustomerRequestBody
    ): CreateCustomerResponse

    @PATCH("customers/{id}")
    suspend fun updateCustomer(
        @Path("id") id: Int,
        @Body createCustomerRequestBody: CreateCustomerRequestBody
    ): UpdateCustomerResponse

    @DELETE("customers/{id}")
    suspend fun deleteCustomers(@Path("id") id: Int): Response<Unit>


    /*--------------------------- visits APIs ----------------- */

    @GET("visits")
    suspend fun getAllVisits(
        @Query("status__in", encoded = true) status: String,
        @Query("ordering") ordering: String,
        @Query("page") page: Int,
        @Query("perPage") perPage: Int
    ): AllVisitListResponse

    @GET("visits")
    suspend fun getRecentVisit(
        @Query("customer") order: Int,
        @Query("ordering") ordering: String,
        @Query("page") page: Int,
        @Query("perPage") perPage: Int
    ): AllVisitListResponse


    @POST("visits")
    suspend fun createVisit(
        @Body createVisitRequestBody: CreateVisitRequestBody
    ): CreatedVisitResponse

    @GET("visits/{id}")
    suspend fun getVisitDetail(
        @Path("id") visitId: Int,
    ): VisitDetailResponseBody

    @PATCH("visits/{id}")
    suspend fun checkInVisit(
        @Path("id") id: Int,
        @Body checkInVisitRequest: CheckInVisitRequest
    ): CheckInResponseBody

    @PATCH("visits/{id}")
    @Multipart
    suspend fun checkOutVisit(
        @Path("id") id: Int,
        @Part status: MultipartBody.Part?,
        @Part location_picture: MultipartBody.Part?,
        @Part visit_summary: MultipartBody.Part?,
        @Part check_out_time: MultipartBody.Part?,
    ): CheckOutResponse


    /*--------------------------- Leaves APIs ----------------- */

    @GET("leaves")
    suspend fun getAllLeaves(
        @Query("status") order: String,
        @Query("ordering") ordering: String,
        @Query("page") page: Int,
        @Query("perPage") perPage: Int
    ): AllLeavesResponseBody

    @POST("leaves")
    suspend fun createLeave(
        @Body createLeaveRequest: CreateLeaveRequest
    ): CreateLeaveResponseBody


    /*--------------------------- Leaves APIs ----------------- */
    @POST("users/attendance/check_in")
    suspend fun checkInApp(
        @Body checkInRequestBody: CheckInRequestBody
    ): CreatedVisitResponse

    @POST("users/attendance/check_out")
    suspend fun checkOutApp(
        @Body checkOutRequestBody: CheckOutRequestBody
    ): CreatedVisitResponse


    @GET("users/attendance")
    suspend fun getAllLeaves(): AttendanceListResponse


    /*--------------------------- Expenses APIs ----------------- */

    @GET("expenses")
    suspend fun getAllExpenses(
        @Query("status") order: String,
        @Query("ordering") ordering: String,
        @Query("page") page: Int,
        @Query("perPage") perPage: Int
    ): AllExpensesResponseBody


    @POST("expenses")
    suspend fun createOtherExpenses(
        @Body otherExpensesRequest: OtherExpensesRequest
    ): Data

    @POST("expenses")
    suspend fun createFuelExpenses(
        @Body createFuelExpenseRequest: CreateFuelExpenseRequest
    ): Data

    @POST("expenses")
    suspend fun createLocalExpenses(
        @Body createLocalExpenseRequest: CreateLocalExpenseRequest
    ): Data

    @POST("expenses")
    suspend fun createOutsideExpenses(
        @Body createOutsideExpenseRequest: CreateOutsideExpenseRequest
    ): Data
}


