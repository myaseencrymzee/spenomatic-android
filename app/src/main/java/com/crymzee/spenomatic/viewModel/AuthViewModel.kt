package com.crymzee.spenomatic.viewModel

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.crymzee.drivetalk.model.response.verification.OTPVerifyResponseBody
import com.crymzee.spenomatic.R
import com.crymzee.spenomatic.model.request.ForgotRequestBody
import com.crymzee.spenomatic.model.request.LoginRequestBody
import com.crymzee.spenomatic.model.request.RefreshTokenRequestBody
import com.crymzee.spenomatic.model.request.ResetPasswordRequestPassword
import com.crymzee.spenomatic.model.request.VerifyOTPRequestBody
import com.crymzee.spenomatic.model.response.ForgotPasswordResponseBody
import com.crymzee.spenomatic.model.response.RefreshResponseBody
import com.crymzee.spenomatic.model.response.ResetPasswordResponseBody
import com.crymzee.spenomatic.model.response.loginResponseBody.LoginResponseBody
import com.crymzee.spenomatic.model.response.meResponse.MeResponseBody
import com.crymzee.spenomatic.model.response.updateProfile.UpdateProfileResponse
import com.crymzee.spenomatic.repository.AuthRepository
import com.crymzee.spenomatic.state.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject


@HiltViewModel
class AuthViewModel @Inject constructor(private val providerAuthRepository: AuthRepository) :
    ViewModel() {
    var filePathProfile: String? = null

    var firstName: String? = null
    var password: String? = null
    var confirmPassword: String? = null
    var newPassword: String? = null
    var currentPassword: String? = null
    var email: String? = null


    private var _getUserProfile = MutableLiveData<Resource<out MeResponseBody>>()
    val getUserProfile: LiveData<Resource<out MeResponseBody>> = _getUserProfile


    fun loginUser(
        loginRequestBody: LoginRequestBody
    ): LiveData<Resource<out LoginResponseBody>> {
        return providerAuthRepository.executeLoginUser(loginRequestBody).asLiveData()
    }

    fun forgotPassword(
        forgotRequestBody: ForgotRequestBody
    ): LiveData<Resource<out ForgotPasswordResponseBody>> {
        return providerAuthRepository.executeForgotPassword(forgotRequestBody).asLiveData()
    }

    fun resetPassword(
        resetPasswordRequestPassword: ResetPasswordRequestPassword
    ): LiveData<Resource<out ResetPasswordResponseBody>> {
        return providerAuthRepository.executeResetPassword(resetPasswordRequestPassword)
            .asLiveData()
    }


    //
    val checkLogin
        get() = providerAuthRepository.executeAutoLogin().asLiveData()

    fun getUserProfile() {
        viewModelScope.launch {
            providerAuthRepository.executeAutoLogin().collect {
                _getUserProfile.postValue(it)
            }
        }
    }


    fun refreshToken(
        refreshTokenRequestBody: RefreshTokenRequestBody
    ): LiveData<Resource<out RefreshResponseBody>> {
        return providerAuthRepository.executeRefreshToken(refreshTokenRequestBody).asLiveData()
    }


    fun verifyOTP(
        otpRequestBody: VerifyOTPRequestBody
    ): LiveData<Resource<out OTPVerifyResponseBody>> {
        return providerAuthRepository.executeVerifyOTP(otpRequestBody).asLiveData()
    }



    fun updateProfile(): LiveData<Resource<out UpdateProfileResponse>> {
        var sendName: MultipartBody.Part? = null
        var send_current_password: MultipartBody.Part? = null
        var send_new_password: MultipartBody.Part? = null
        var send_confirm_password: MultipartBody.Part? = null
        var sendProfile_picture: MultipartBody.Part? = null

        filePathProfile?.let {
            val file: File = org.apache.commons.io.FileUtils.getFile(it)
            val requestFile = file.asRequestBody("media/*".toMediaTypeOrNull())
            sendProfile_picture = MultipartBody.Part.createFormData(
                "profile_picture",
                file.name,
                requestFile
            )
        }
        firstName?.let {
            sendName =
                MultipartBody.Part.createFormData("fullname", it)
        }
        currentPassword?.let {
            send_current_password =
                MultipartBody.Part.createFormData("current_password", it)
        }
        newPassword?.let {
            send_new_password =
                MultipartBody.Part.createFormData("new_password", it)
        }
        confirmPassword?.let {
            send_confirm_password =
                MultipartBody.Part.createFormData("confirm_password", it)
        }


        return providerAuthRepository.executeUpdateProfile(
            sendName,
            send_current_password,
            send_new_password,
            send_confirm_password,
            sendProfile_picture
        )
            .asLiveData()
    }

    fun validateCredential(email: String, password: String): Pair<Boolean, Int> {
        var result: Pair<Boolean, Int> = true to R.string.empty_string

        if (TextUtils.isEmpty(email)) {
            result = false to R.string.error_enter_email
        } else if (TextUtils.isEmpty(password)) {
            result = false to R.string.error_enter_password
        }

        return result
    }

    fun validateOTPCredential(otp: String): Pair<Boolean, Int> {
        var result = Pair(true, R.string.empty_string)
        if (TextUtils.isEmpty(otp)) {
            result = Pair(false, R.string.error_enter_otp)
        } else if (otp.length < 4) {
            result = Pair(false, R.string.error_enter_valid_otp)
        }
        return result
    }

    fun validateForgotEmail(email: String): Pair<Boolean, Int> {
        val emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
        var result = Pair(true, R.string.empty_string)

        if (TextUtils.isEmpty(email)) {
            result = Pair(false, R.string.error_enter_email)
        } else if (!email.matches(emailPattern)) {
            result = Pair(false, R.string.error_invalid_email)
        }

        return result
    }

    fun validateResetPassword(password: String, confirmPassword: String): Pair<Boolean, Int> {
        var result = Pair(true, R.string.empty_string)

        when {
            TextUtils.isEmpty(password) -> {
                result = Pair(false, R.string.error_enter_password) // Prompt to enter password
            }


            password.contains(" ") -> {
                result = Pair(
                    false,
                    R.string.error_password_whitespace
                ) // Password must not contain spaces
            }

            password.length < 8 -> {
                result = Pair(
                    false,
                    R.string.error_password_length
                ) // Password must be at least 8 characters long
            }

            !password.any { it.isUpperCase() } -> {
                result = Pair(
                    false,
                    R.string.error_password_uppercase
                ) // Must contain an uppercase letter
            }

            !password.any { it.isLowerCase() } -> {
                result = Pair(
                    false,
                    R.string.error_password_lowercase
                ) // Must contain a lowercase letter
            }

            !password.any { it.isDigit() } -> {
                result = Pair(false, R.string.error_password_digit) // Must contain a number
            }

            !password.any { it in "@#\$%^&+=!" } -> {
                result =
                    Pair(false, R.string.error_password_special) // Must contain a special character
            }

            TextUtils.isEmpty(confirmPassword) -> {
                result = Pair(
                    false,
                    R.string.error_enter_confirm_password
                ) // Prompt to enter confirm password
            }

            password != confirmPassword -> {
                result = Pair(false, R.string.error_password_mismatch) // Passwords do not match
            }
        }

        return result
    }

    fun validateUpdatePassword(
        password: String,
        newPassword: String,
        confirmPassword: String
    ): Pair<Boolean, Int> {
        var result = Pair(true, R.string.empty_string)

        when {
            TextUtils.isEmpty(password) -> {
                result = Pair(false, R.string.error_enter_old_password)
            }

            TextUtils.isEmpty(newPassword) -> {
                result = Pair(false, R.string.error_enter_new_password)
            }

            newPassword.contains(" ") -> {
                result = Pair(false, R.string.error_password_whitespace) // No spaces allowed
            }

            newPassword.length < 8 -> {
                result = Pair(false, R.string.error_password_length)
            }

            !newPassword.any { it.isUpperCase() } -> {
                result = Pair(false, R.string.error_password_lowercase)
            }

            !newPassword.any { it.isLowerCase() } -> {
                result = Pair(false, R.string.error_password_uppercase)
            }

            !newPassword.any { it.isDigit() } -> {
                result = Pair(false, R.string.error_password_digit)
            }

            !newPassword.any { it in "@#\$%^&+=!" } -> {
                result = Pair(false, R.string.error_password_special)
            }
            TextUtils.isEmpty(confirmPassword) -> {
                result = Pair(false, R.string.error_enter_confirm_password)
            }

            newPassword != confirmPassword -> {
                result = Pair(false, R.string.error_password_mismatch)
            }
        }

        return result
    }

}