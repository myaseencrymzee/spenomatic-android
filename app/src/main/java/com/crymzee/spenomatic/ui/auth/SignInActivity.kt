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
import com.crymzee.spenomatic.databinding.ActivitySignInBinding
import com.crymzee.spenomatic.model.request.LoginRequestBody
import com.crymzee.spenomatic.sharedPreference.SharedPrefsHelper
import com.crymzee.spenomatic.state.Resource
import com.crymzee.spenomatic.ui.MainActivity
import com.crymzee.spenomatic.utils.SpenoMaticLogger
import com.crymzee.spenomatic.utils.extractFirstErrorMessage
import com.crymzee.spenomatic.utils.setSingleClickListener
import com.crymzee.spenomatic.utils.showErrorPopup
import com.crymzee.spenomatic.viewModel.AuthViewModel
import com.google.android.material.internal.ViewUtils.hideKeyboard
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignInActivity : BaseActivity() {
    var email = ""
    var password = ""
    private lateinit var binding: ActivitySignInBinding
    private val authViewModel by viewModels<AuthViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_in)
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
        // ✅ Set default selection for role
        binding.radioGroupRoles.check(R.id.radio_delivery)
        binding.apply {
            labelForgotPassword.setOnClickListener {
                val selectedRole = getSelectedRole()
                if (selectedRole == null) {
                    showErrorPopup("Validation", "Please select a role before continuing")
                } else {
                    val intent = Intent(this@SignInActivity, ForgotPasswordActivity::class.java)
                    intent.putExtra("ROLE", selectedRole)
                    startActivity(intent)
                }
            }

            btnLogin.setSingleClickListener {
                val selectedRole = getSelectedRole()

                if (selectedRole == null) {
                    showErrorPopup("Validation", "Please select a role before login")
                } else {
                    hideKeyboard()
                    checkValidations(selectedRole)
                }
            }
        }
    }

    private fun checkValidations(role: String) {
        email = binding.etEmail.text.toString()
        password = binding.etPassword.text.toString()

        val validationResult = authViewModel.validateCredential(email, password)
        if (validationResult.first) {
            setupObserver(role)
        } else {
            val message = getString(validationResult.second)
            showErrorPopup("", message)
        }
    }

    private fun getSelectedRole(): String? {
        return when (binding.radioGroupRoles.checkedRadioButtonId) {
            R.id.radio_delivery -> "delivery"
            R.id.radio_office -> "office"
            R.id.radio_sales -> "sales"
            else -> null
        }
    }

    private fun setupObserver(role: String) {
        authViewModel.loginUser(loginRequestBody = LoginRequestBody(email, password, role))
            .observe(this) { response ->
                binding.loader.isVisible = response is Resource.Loading
                when (response) {

                    is Resource.Error -> {
                        val errorMessage = extractFirstErrorMessage(response.throwable)
                        SpenoMaticLogger.logErrorMsg("Error", errorMessage.description)
                        showErrorPopup(
                            heading = errorMessage.heading,
                            description = errorMessage.description
                        )

                    }

                    is Resource.Loading -> {

                    }

                    is Resource.Success -> {
                        response.data?.let {

                            SharedPrefsHelper.saveLoggedInInstitute(it)
                            val intent = Intent(
                                this@SignInActivity,
                                MainActivity::class.java
                            )
                            intent.putExtra("ROLE", role)
                            startActivity(intent)
                            finish()

                        }

                    }
                }
            }
    }
}