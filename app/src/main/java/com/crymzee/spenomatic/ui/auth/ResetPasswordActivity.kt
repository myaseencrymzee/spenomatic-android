package com.crymzee.spenomatic.ui.auth

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.crymzee.spenomatic.R
import com.crymzee.spenomatic.base.BaseActivity
import com.crymzee.spenomatic.databinding.ActivityResetPasswordBinding
import com.crymzee.spenomatic.model.request.ResetPasswordRequestPassword
import com.crymzee.spenomatic.state.Resource
import com.crymzee.spenomatic.utils.SpenoMaticLogger
import com.crymzee.spenomatic.utils.extractFirstErrorMessage
import com.crymzee.spenomatic.utils.setSingleClickListener
import com.crymzee.spenomatic.utils.showErrorPopup
import com.crymzee.spenomatic.utils.showSuccessPopup
import com.crymzee.spenomatic.viewModel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ResetPasswordActivity : BaseActivity() {
    private lateinit var binding: ActivityResetPasswordBinding
    private val authViewModel by viewModels<AuthViewModel>()

    var confirmPassword = ""
    var password = ""
    var code = ""
    var email = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_reset_password)
        enableEdgeToEdge()
        window.statusBarColor = ContextCompat.getColor(this, R.color.background)
        supportActionBar?.setBackgroundDrawable(
            ColorDrawable(ContextCompat.getColor(this, R.color.background))
        )
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        viewInit()
    }

    private fun viewInit() {
        code = intent.getStringExtra("OTP_CODE").toString()
        email = intent.getStringExtra("email").toString()

        binding.apply {
            icBack.setOnClickListener {
                moveBack(this@ResetPasswordActivity)
            }
            btnContinue.setSingleClickListener {
                validPasswordFields()
            }
        }
    }

    private fun validPasswordFields() {
        confirmPassword = binding.etConfirmPassword.text.toString()
        password = binding.etPassword.text.toString()

        val validationResult = authViewModel.validateResetPassword(password, confirmPassword)
        if (validationResult.first) {
            setupObserver()

        } else {
            val message = getString(validationResult.second)
            showErrorPopup("", message)
        }
    }

    private fun setupObserver() {
        authViewModel.resetPassword(
            resetPasswordRequestPassword = ResetPasswordRequestPassword(
                email,
                password,
                confirmPassword,
                code,
                "delivery"
            )
        )
            .observe(this) { response ->
                binding.loader.isVisible = response is Resource.Loading
                when (response) {

                    is Resource.Error -> {
                        val errorMessage = extractFirstErrorMessage(response.throwable)
                        SpenoMaticLogger.logErrorMsg("Error", errorMessage.description)
                        showErrorPopup(errorMessage.heading, errorMessage.description)
                    }

                    is Resource.Loading -> {

                    }

                    is Resource.Success -> {
                        showSuccessPopup(
                            "Success!", "Password has been reset successfully.",
                            onConfirm = {
                                gotoLogin()
                            })

                    }
                }
            }
    }

    private fun gotoLogin() {
        val intent = Intent(this@ResetPasswordActivity, SignInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}