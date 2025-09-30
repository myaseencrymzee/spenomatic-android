package com.crymzee.spenomatic.base

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.crymzee.spenomatic.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
open class BaseActivity : AppCompatActivity() {
    companion object {
        @JvmStatic

        public var hasInternet: Boolean = false

    }

    @Inject
    lateinit var navOptions: NavOptions
    var context: Context? = null
    private val PERMISSION_REQUEST_CODE = 123
    internal var permissionArrays =
        arrayOf(
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.ACCESS_NETWORK_STATE
        )


    private val TAG: String = "TAG_PERMISSION"

    fun showKeyboard(view: View) {
        val imm: InputMethodManager =
            applicationContext.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        // imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    fun moveBack(context: Context) {
        if (context is Activity) {
            context.finish()  // Close the current activity
            backActivityAnim()  // Optional, if you want to add custom animations
        } else {
            Log.e("MoveBack", "Context is not an Activity or is null")
        }
    }

    fun backActivityAnim() {
        try {
            (context as Activity).overridePendingTransition(
                R.anim.enter_from_left,
                R.anim.exit_to_right
            )
            hideKeyboard(null)
        } catch (ignored: java.lang.Exception) {
        }
    }



    fun hideKeyboard(view: View?) {
        val imm =
            applicationContext.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }


    fun navigate(
        destination: Int,
        navController: NavController
    ) {
        try {
            navController.navigate(destination, null, navOptions)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun showSnackBar(view: View, message: String = "", action: String = "") {

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    }

    fun checkPermissions() {
        val missingPermissions = permissionArrays.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                missingPermissions.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        } else {
//            showToast(getString(R.string.permission_granted))
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val deniedPermissions = permissions.zip(grantResults.toTypedArray()).filter {
                it.second != PackageManager.PERMISSION_GRANTED
            }.map { it.first }

            if (deniedPermissions.isNotEmpty()) {
                // Some permissions were denied, handle accordingly
                handleDeniedPermissions(deniedPermissions)
            } else {
                //  showToast(getString(R.string.permission_granted))
            }
        }
    }

    private fun handleDeniedPermissions(deniedPermissions: List<String>) {
        ActivityCompat.requestPermissions(
            this,
            deniedPermissions.toTypedArray(), PERMISSION_REQUEST_CODE
        )
    }

}

