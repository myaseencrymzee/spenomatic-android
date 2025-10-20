package com.crymzee.spenomatic.ui.auth

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.crymzee.spenomatic.R
import com.crymzee.spenomatic.base.BaseActivity
import com.crymzee.spenomatic.databinding.ActivityOtpverifyBinding
import com.crymzee.spenomatic.enums.OTPType
import com.crymzee.spenomatic.model.request.ForgotRequestBody
import com.crymzee.spenomatic.model.request.VerifyOTPRequestBody
import com.crymzee.spenomatic.state.Resource
import com.crymzee.spenomatic.ui.auth.ForgotPasswordActivity
import com.crymzee.spenomatic.utils.SpenoMaticLogger
import com.crymzee.spenomatic.utils.extractFirstErrorMessage
import com.crymzee.spenomatic.utils.setSingleClickListener
import com.crymzee.spenomatic.utils.showErrorPopup
import com.crymzee.spenomatic.utils.showToast
import com.crymzee.spenomatic.viewModel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class OTPVerifyActivity : BaseActivity() {
    private lateinit var binding: ActivityOtpverifyBinding
    var otpCode = ""
    var role = ""
    private val authViewModel by viewModels<AuthViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_otpverify)
        enableEdgeToEdge()
        window.statusBarColor = ContextCompat.getColor(this, R.color.background)
        supportActionBar?.setBackgroundDrawable(
            ColorDrawable(ContextCompat.getColor(this, R.color.background))
        )
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Dynamically adjust bottom padding so content moves above keyboard
            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                maxOf(systemBars.bottom, imeInsets.bottom)
            )
            insets
        }
        viewInit()
    }

    private fun viewInit() {
        val data = intent.getStringExtra("email")
        role = intent.getStringExtra("ROLE") ?: ""

        binding.apply {
            icBack.setOnClickListener {
                moveBack(this@OTPVerifyActivity)
            }
            btnConfirm.setSingleClickListener {
                checkValidations(data)
            }
            binding.containerEmail.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    otpCode = s.toString()
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            btnResend.setSingleClickListener {
                setupObserver(data)
            }
        }
    }

    private fun checkValidations(data: String?) {

        val validationResult = authViewModel.validateOTPCredential(otpCode)
        if (validationResult.first) {
            verifyOTP(data)

        } else {
            val message = getString(validationResult.second)
            showErrorPopup("",message)
        }
    }

    private fun verifyOTP(data: String?) {

        authViewModel.verifyOTP(
            VerifyOTPRequestBody(
                data ?: "",
                otpCode,
                OTPType.FORGOT.toStringValue(),role
            )
        )
            .observe(this) { response ->
                binding.loader.isVisible = response is Resource.Loading
                when (response) {

                    is Resource.Error -> {
                        val errorMessage = extractFirstErrorMessage(response.throwable)
                        SpenoMaticLogger.logErrorMsg("Error", errorMessage.description)
                        showErrorPopup(errorMessage.heading,errorMessage.description)
                    }

                    is Resource.Loading -> {

                    }

                    is Resource.Success -> {
                        val intent =
                            Intent(
                                this@OTPVerifyActivity,
                                ResetPasswordActivity::class.java
                            )
                        intent.putExtra(
                            "OTP_CODE",
                            response.data?.data?.verification_token
                        )
                        intent.putExtra(
                            "email",
                           data
                        )
                        intent.putExtra(
                            "ROLE",
                           role
                        )
                        startActivity(intent)
                    }
                }
            }
    }


    private fun setupObserver(data: String?) {
        authViewModel.forgotPassword(
            forgotRequestBody = ForgotRequestBody(
                data ?: "",
                OTPType.FORGOT.toStringValue(),
                role
            )
        )
            .observe(this) { response ->
                binding.loader.isVisible = response is Resource.Loading
                when (response) {
                    is Resource.Error -> {
                        val errorMessage = extractFirstErrorMessage(response.throwable)
                        SpenoMaticLogger.logErrorMsg("Error", errorMessage.description)
                        showErrorPopup(errorMessage.heading,errorMessage.description)
                    }

                    is Resource.Loading -> {}

                    is Resource.Success -> {
                        showToast("OTP has been sent to your email successfully")
                    }
                }
            }


    }
}