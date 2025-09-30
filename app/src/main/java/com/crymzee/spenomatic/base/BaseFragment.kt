package com.crymzee.spenomatic.base

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crymzee.spenomatic.ui.MainActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
open class BaseFragment : Fragment() {


    private lateinit var requestStatus: (Boolean) -> Unit
    lateinit var navController: NavController
    var TAG = this.javaClass.simpleName
    open lateinit var dialog: BottomSheetDialog


    @Inject
    lateinit var navOptions: NavOptions
    var screenName = ""
    var navArgs: Bundle? = null
    val PERMISSION_CODE = 201

    var permissionArrays =
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_MEDIA_LOCATION
        )

    fun checkIfPermissionGranted(
        requestCode: Int,
        permission: String,
        message: String,
        requestStatus: (Boolean) -> Unit
    ) {
        this.requestStatus = requestStatus
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                requestStatus(true)
            }

            shouldShowRequestPermissionRationale(permission) -> {
                requestStatus(false)
//                ProperGeniesAlert.showAlertDialogAndPerformAction(
//                    requireActivity(),
//                    "Wait",
//                    "You need to Grant Access to $message From Settings >> AppInfo >> Permissions For Using Application",
//                    "Go to Settings"
//                ) {
//                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
//                    val uri = Uri.fromParts("package", requireContext().packageName, null)
//                    intent.data = uri
//                    startActivity(intent)
//
//                }
            }

            else -> {
                requestPermissions(
                    arrayOf(permission),
                    requestCode
                )
            }
        }
    }

    fun hideBottomNav() {
//        (activity as? MainActivity)?.setBottomNavVisibility(false)
    }

    fun showBottomNav() {
//        (activity as? MainActivity)?.setBottomNavVisibility(true)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
    }


    fun hideKeyboard() {
        val view = requireActivity().currentFocus
        if (view != null) {
            val imm =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }


    fun showKeyboard() {
        val imm: InputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }


    //This funtion is to reduce the code of line for initialization of layout manager
    fun getLayoutManager(): RecyclerView.LayoutManager {
        var layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        return layoutManager
    }

    fun getLinearLayoutManager(): LinearLayoutManager {
        var layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        return layoutManager
    }

    fun getHorizontalLayoutManager(): RecyclerView.LayoutManager {
        var layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        return layoutManager
    }


    fun navigate(destination: Int) {
        try {
            navController.navigate(destination, null, navOptions)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun navigate(destination: Int, bundle: Bundle) {
        try {
            navController.navigate(destination, bundle, navOptions)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun navigateClear(destination: Int, bundle: Bundle? = null) {
        try {
            val navOptions = NavOptions.Builder()
                .setPopUpTo(navController.graph.startDestinationId, true)
                .setLaunchSingleTop(true)
                .build()

            navController.navigate(destination, bundle, navOptions)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun navigateClearInclusive(destination: Int, popFrom: Int, bundle: Bundle? = null) {
        try {
            val navOptions = NavOptions.Builder()
                .setPopUpTo(popFrom, true) // clears only that fragment
                .setLaunchSingleTop(true)
                .build()

            navController.navigate(destination, bundle, navOptions)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setNavigationArgs(args: Bundle?) {
        this.navArgs = args
    }

//    fun Fragment.navigateTo(
//        @IdRes resId: Int,
//        isLoginRequire: Boolean = false,
//        args: Bundle? = null,
//        navOption: NavOptions? = navOptions,
//        navExtras: Navigator.Extras? = null
//    ) = with(findNavController()) {
//        if (isLoginRequire && SharedPrefsHelper.getProperGeniesUserAuth() == null) {
//            setNavigationArgs(args)
////            SharedPrefsHelper.setCallFrom(1)
//            //startAuthenticationFlow()
//        } else {
//
//            /* currentDestination?.getAction(resId)
//                 ?.let {
//
//                     navigate(resId, args, navOption, navExtras)
//                 }*/
//            try {
//                navigate(resId, args, navOption, navExtras)
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//
//        }
//    }

//    private fun startAuthenticationFlow() {
//
//        Common.instance?.callIntentWithResult(
//            activity as MainActivity,
//            LoginActivity::class.java,
//            0
//        )
//        //   navController.navigate(bypassDestination, null, navOptions)
//
//    }

    fun navigate(
        destination: Int,
        requiresLogin: Boolean,
        bypassDestination: Int,
        bundle: Bundle?
    ) {
        try {
            if (requiresLogin) {

                navController.navigate(bypassDestination, null, navOptions)
                return
            }
            navController.navigate(destination, bundle, navOptions)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun navigate(
        destination: Int,
        requiresLogin: Boolean,
        bypassDestination: Int,
        navController: NavController
    ) {
        try {
            if (requiresLogin) {
                navController.navigate(bypassDestination, null, navOptions)
                return
            }
            navController.navigate(destination, null, navOptions)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun bottomNavigationLock() {
        // (activity as MainActivity).lockSideMenu()
//        (activity as MainActivity).showHideBottomNavigation(false, null)
    }

    fun Fragment.getNavigationResult(key: String = "result") =
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<Any>(key)

    fun Fragment.setNavigationResult(result: Any, key: String = "result") {
        navController.previousBackStackEntry?.savedStateHandle?.set(key, result)
    }


    private fun hasPermissions(vararg permissions: String): Boolean = permissions.all {
        ActivityCompat.checkSelfPermission(
            requireContext(),
            it
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun enableRunTimePermission(permissions: Array<String>): Boolean {
        permissionArrays = permissions

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!hasPermissions(*permissions)) {
                ActivityCompat.requestPermissions(requireActivity(), permissions, PERMISSION_CODE);
                false
            } else
                true
        } else
            false
    }

}

