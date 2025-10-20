package com.crymzee.spenomatic.ui.auth

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
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
import com.crymzee.spenomatic.databinding.ActivityForgotPasswordBinding
import com.crymzee.spenomatic.enums.OTPType
import com.crymzee.spenomatic.model.request.ForgotRequestBody
import com.crymzee.spenomatic.state.Resource
import com.crymzee.spenomatic.utils.SpenoMaticLogger
import com.crymzee.spenomatic.utils.extractFirstErrorMessage
import com.crymzee.spenomatic.utils.showErrorPopup
import com.crymzee.spenomatic.utils.showToast
import com.crymzee.spenomatic.viewModel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue
import kotlin.toString

@AndroidEntryPoint
class ForgotPasswordActivity : BaseActivity() {
    private lateinit var binding: ActivityForgotPasswordBinding
    var email = ""
    var role = ""
    private val authViewModel by viewModels<AuthViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_forgot_password)
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
        role = intent.getStringExtra("ROLE") ?: ""
        binding.apply {
            icBack.setOnClickListener {
                moveBack(this@ForgotPasswordActivity)
            }
            btnContinue.setOnClickListener {
                checkValidations()
            }
            etEmail.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    hideKeyboard(etEmail)
                    etEmail.clearFocus()
                    true
                } else {
                    false
                }
            }

        }
    }


    private fun setupObserver() {
        authViewModel.forgotPassword(
            ForgotRequestBody(
                email, OTPType.FORGOT.toStringValue(),
               role
            )
        )
            .observe(this) { response ->
                binding.loader.isVisible = response is Resource.Loading
                when (response) {

                    is Resource.Error -> {
                        val errorMessage = extractFirstErrorMessage(response.throwable)
                        SpenoMaticLogger.logErrorMsg("Error", errorMessage.description)
                        showErrorPopup( errorMessage.heading,errorMessage.description)
                    }

                    is Resource.Loading -> {

                    }

                    is Resource.Success -> {
                        showToast("OTP has been sent to your email successfully")
                        gotoOTPScreen()
                    }
                }
            }
    }

    private fun gotoOTPScreen() {
        val intent = Intent(this@ForgotPasswordActivity, OTPVerifyActivity::class.java)

        intent.putExtra("email", email)
        intent.putExtra("ROLE", role)
        startActivity(intent)
    }

    private fun checkValidations() {
        email = binding.etEmail.text.toString()

        val validationResult = authViewModel.validateForgotEmail(email)
        if (validationResult.first) {
            setupObserver()

        } else {
            val message = getString(validationResult.second)
            showErrorPopup("",message)
        }
    }
}