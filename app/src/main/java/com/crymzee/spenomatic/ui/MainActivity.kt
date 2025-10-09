package com.crymzee.spenomatic.ui

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.crymzee.spenomatic.R
import com.crymzee.spenomatic.base.BaseActivity
import com.crymzee.spenomatic.databinding.ActivityMainBinding
import com.crymzee.spenomatic.databinding.DialogCheckInUserBinding
import dagger.hilt.android.AndroidEntryPoint
import np.com.susanthapa.curved_bottom_navigation.CbnMenuItem

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Handle window insets (status bar padding)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)


        window.statusBarColor = ContextCompat.getColor(this, R.color.background)
        supportActionBar?.setBackgroundDrawable(
            ColorDrawable(ContextCompat.getColor(this, R.color.background))
        )
        WindowInsetsControllerCompat(window, window.decorView)
            .isAppearanceLightStatusBars = true
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        setupCurvedBottomNav()
        setupBottomNavVisibility()
    }

    private fun setupCurvedBottomNav() {
        binding.navView.setMenuItems(
            listOf(
                CbnMenuItem(R.drawable.home_nav, R.drawable.home_avd, R.id.homeFragment, "Home"),
                CbnMenuItem(
                    R.drawable.curtomer_nav,
                    R.drawable.customer_avd,
                    R.id.customerFragment,
                    "Customer"
                ),
                CbnMenuItem(
                    R.drawable.ic_visit_nav,
                    R.drawable.visit_avd,
                    R.id.visitFragment,
                    "Visits"
                ),
                CbnMenuItem(
                    R.drawable.expenses_nav,
                    R.drawable.expenses_avd,
                    R.id.expensesFragment,
                    "Expenses"
                ),
                CbnMenuItem(
                    R.drawable.leaves_nav,
                    R.drawable.leaves_avd,
                    R.id.leaveFragment,
                    "Leave"
                )
            ).toTypedArray(),  // 👈 FIX here
            activeIndex = 0
        )


        // Attach with NavController
        binding.navView.setupWithNavController(navController)
    }

    private fun setupBottomNavVisibility() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment,
                R.id.customerFragment,
                R.id.visitFragment,
                R.id.expensesFragment,
                R.id.leaveFragment -> binding.navView.visibility = View.VISIBLE

                else -> binding.navView.visibility = View.GONE
            }
        }
    }

}


