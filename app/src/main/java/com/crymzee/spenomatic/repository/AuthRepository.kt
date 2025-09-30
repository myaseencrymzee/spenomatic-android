package com.crymzee.spenomatic.repository


import com.crymzee.spenomatic.model.request.ForgotRequestBody
import com.crymzee.spenomatic.state.networkBoundResource
import com.crymzee.spenomatic.model.request.LoginRequestBody
import com.crymzee.spenomatic.model.request.RefreshTokenRequestBody
import com.crymzee.spenomatic.model.request.ResetPasswordRequestPassword
import com.crymzee.spenomatic.model.request.VerifyOTPRequestBody
import com.crymzee.spenomatic.retrofit.ApiServices
import okhttp3.MultipartBody

class AuthRepository(private val apiServices: ApiServices) {

    fun executeLoginUser(loginRequestBody: LoginRequestBody) = networkBoundResource(
        fetch = {
            apiServices.loginUser(loginRequestBody)
        }
    )

    fun executeAutoLogin() = networkBoundResource(
        fetch = {
            apiServices.getMe()
        }
    )

    fun executeRefreshToken(refreshTokenRequestBody: RefreshTokenRequestBody) =
        networkBoundResource(
            fetch = {
                apiServices.refreshTokens(refreshTokenRequestBody)
            }
        )

    fun executeForgotPassword(forgotRequestBody: ForgotRequestBody) = networkBoundResource(
        fetch = {
            apiServices.forgotPassword(forgotRequestBody)
        }
    )

    fun executeVerifyOTP(otpRequestBody: VerifyOTPRequestBody) = networkBoundResource(
        fetch = {
            apiServices.verifyOTP(otpRequestBody)
        }
    )

    fun executeResetPassword(resetPasswordRequestPassword: ResetPasswordRequestPassword) =
        networkBoundResource(
            fetch = {
                apiServices.resetPassword(resetPasswordRequestPassword)
            }
        )


    fun executeUpdateProfile(
        name: MultipartBody.Part?,
        phone: MultipartBody.Part?,
        current_password: MultipartBody.Part?,
        new_password: MultipartBody.Part?,
        confirm_password: MultipartBody.Part?,

    ) = networkBoundResource(
        fetch = {
            apiServices.updateProfile(
                name,
                phone,
                current_password,
                new_password,
                confirm_password,

            )
        }
    )
}