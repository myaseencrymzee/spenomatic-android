package com.crymzee.spenomatic.ui.splash

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.crymzee.spenomatic.R
import com.crymzee.spenomatic.base.BaseActivity
import com.crymzee.spenomatic.databinding.ActivitySplashBinding
import com.crymzee.spenomatic.model.request.RefreshTokenRequestBody
import com.crymzee.spenomatic.model.response.meResponse.MeResponseBody
import com.crymzee.spenomatic.sharedPreference.SharedPrefsHelper
import com.crymzee.spenomatic.state.Resource
import com.crymzee.spenomatic.ui.MainActivity
import com.crymzee.spenomatic.ui.auth.SignInActivity
import com.crymzee.spenomatic.utils.SpenoMaticLogger
import com.crymzee.spenomatic.utils.extractFirstErrorMessage
import com.crymzee.spenomatic.viewModel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : BaseActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val authViewModel by viewModels<AuthViewModel>()

    private var hasNavigated = false // 👈 single guard for ALL navigation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        enableEdgeToEdge()
        checkAndRequestPermissions()
    }

    // ---------------- Permissions ----------------
    private fun checkAndRequestPermissions() {
        val notGrantedPermissions = permissionArrays.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (notGrantedPermissions.isNotEmpty()) {
            requestPermissionLauncher.launch(notGrantedPermissions.toTypedArray())
        } else {
            if (!hasShownPermissionGrantedToast()) {
                savePermissionGrantedStatus()
            }
            checkLogin()
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val deniedPermissions = permissions.filterValues { !it }
            if (deniedPermissions.isEmpty()) {
                if (!hasShownPermissionGrantedToast()) {
                    savePermissionGrantedStatus()
                }
            }
            checkLogin()
        }

    // ---------------- Auth & Navigation ----------------
    private fun checkLogin() {
        val refreshToken = SharedPrefsHelper.getUserAuthRefresh() ?: ""
        if (refreshToken.isBlank()) {
            gotoAuthScreen()
            return
        }

        authViewModel.refreshToken(RefreshTokenRequestBody(refreshToken))
            .observe(this) { response ->
                when (response) {
                    is Resource.Error -> {
                        val errorMessage = extractFirstErrorMessage(response.throwable)
                        SpenoMaticLogger.logErrorMsg("Refresh Error", errorMessage.description)
                        gotoAuthScreen()
                    }

                    is Resource.Loading -> {
                        // Optional: show loading UI
                    }

                    is Resource.Success -> {
                        SharedPrefsHelper.setUserAuth(response.data?.access)
                        checkAutoLogin()
                    }
                }
            }
    }

    private fun checkAutoLogin() {
        authViewModel.checkLogin.removeObservers(this)
        authViewModel.checkLogin.observe(this) { response ->
            when (response) {
                is Resource.Error -> {
                    val errorMessage = extractFirstErrorMessage(response.throwable)
                    SpenoMaticLogger.logErrorMsg("Login Error", errorMessage.description)
                    gotoAuthScreen()
                }

                is Resource.Loading -> {
                    // Optional loading
                }

                is Resource.Success -> {
                    response.data?.let {
                        gotoUserScreen(it)
                    }
                }
            }
        }
    }

    private fun gotoUserScreen(data: MeResponseBody?) {
        if (hasNavigated) return
        hasNavigated = true

        data?.let {
            SharedPrefsHelper.setUserID(it.id)
            SharedPrefsHelper.setUserImage(it.profile_picture)
            SharedPrefsHelper.setPhoneNumber(it.phone)
            SharedPrefsHelper.setUserEmail(it.email)
            SharedPrefsHelper.setName(it.fullname)
            SharedPrefsHelper.setUserRole(it.role)

            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            intent.putExtra("ROLE", it.role)
            startActivity(intent)
            finish()
        }
    }

    private fun gotoAuthScreen() {
        if (hasNavigated) return
        hasNavigated = true

        val intent = Intent(this, SignInActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        startActivity(intent)
        finish()
    }

    // ---------------- Helpers ----------------
    private fun hasShownPermissionGrantedToast(): Boolean {
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("permissions_granted", false)
    }

    private fun savePermissionGrantedStatus() {
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("permissions_granted", true).apply()
    }
}
