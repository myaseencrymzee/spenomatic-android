package com.crymzee.spenomatic.ui.fragments.home

import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.crymzee.spenomatic.R
import com.crymzee.spenomatic.adapter.WeekDaysAdapter
import com.crymzee.spenomatic.base.BaseFragment
import com.crymzee.spenomatic.databinding.DialogCheckInUserBinding
import com.crymzee.spenomatic.databinding.FragmentHomeBinding
import com.crymzee.spenomatic.model.WeekDay
import com.crymzee.spenomatic.model.request.CheckInRequestBody
import com.crymzee.spenomatic.model.request.CheckOutRequestBody
import com.crymzee.spenomatic.model.request.Location
import com.crymzee.spenomatic.model.response.attendenceList.Data
import com.crymzee.spenomatic.model.response.meResponse.MeResponseBody
import com.crymzee.spenomatic.sharedPreference.SharedPrefsHelper
import com.crymzee.spenomatic.state.Resource
import com.crymzee.spenomatic.utils.SpenoMaticLogger
import com.crymzee.spenomatic.utils.extractFirstErrorMessage
import com.crymzee.spenomatic.utils.hide
import com.crymzee.spenomatic.utils.showErrorPopup
import com.crymzee.spenomatic.utils.visible
import com.crymzee.spenomatic.viewModel.AuthViewModel
import com.crymzee.spenomatic.viewModel.HomeViewModel
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeFragment : BaseFragment() {
    private lateinit var binding: FragmentHomeBinding
    private val authViewModel: AuthViewModel by activityViewModels()
    private var activeDialog: AlertDialog? = null  // Keep track of the active dialog

    private val homeViewModel: HomeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        if (!::binding.isInitialized) {
            binding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_home,
                container,
                false
            )

            if (SharedPrefsHelper.getUserCheckedIn() == false || SharedPrefsHelper.getUserCheckedIn() == null) {
                addCheckInPopup()
            }
            showBottomNav()
            viewInit()
        }


        return binding.root
    }

    private fun viewInit() {

        binding.apply {
            containerProfileImage.setOnClickListener {
                navigate(R.id.action_homeFragment_to_profile_fragment)
            }
            ivNotifications.setOnClickListener {
                navigate(R.id.action_homeFragment_to_notificationFragment)
            }
            if (SharedPrefsHelper.getUserCheckedIn() == true) {
                btnSave.text = "Check Out"
            } else {
                btnSave.text = "Check In"
            }

            if (SharedPrefsHelper.getUserCheckedInTime() != null && SharedPrefsHelper.getUserCheckedInTime() != "") {
                labelExplore.visible()
                labelExplore.text = "Attendance at ${SharedPrefsHelper.getUserCheckedInTime()}"
            } else {
                labelExplore.hide()
            }

            btnSave.setOnClickListener {
                if (SharedPrefsHelper.getUserCheckedIn() == true) {
                    getCurrentLocation { lat, lng ->
                        val model = CheckOutRequestBody(
                            check_out = getLocalDateTime(),
                            check_out_location = Location(
                                type = "Point", coordinates = listOf(lng, lat)
                            )
                        )
                        // Send to API
                        SpenoMaticLogger.logErrorMsg("CheckIn Payload", model.toString())
                        checkOutApp(model)
                    }
                } else {
                    addCheckInPopup()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getData()
        getAttendance()
    }

    override fun onResume() {
        super.onResume()
        authViewModel.getUserProfile()
    }


    private fun getCurrentLocation(onLocationReady: (Double, Double) -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        if (ActivityCompat.checkSelfPermission(
                requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO request runtime permission before using
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                onLocationReady(location.latitude, location.longitude)
            }
        }
    }

    fun addCheckInPopup() {
        try {
            activeDialog?.dismiss()  // Dismiss any existing dialog before showing a new one

            // Inflate layout using ViewBinding
            val dialogueCheckIn =
                DialogCheckInUserBinding.inflate(LayoutInflater.from(requireContext()))

            val alertDialog = AlertDialog.Builder(requireContext()).create()
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            // Store reference to active dialog
            activeDialog = alertDialog


            // OK button with slight delay before invoking the callback
            dialogueCheckIn.ivCancel.setOnClickListener {
                alertDialog.dismiss()
                activeDialog = null
            }

            dialogueCheckIn.btnAdd.setOnClickListener {
                getCurrentLocation { lat, lng ->
                    val model = CheckInRequestBody(
                        check_in = getLocalDateTime(),
                        check_in_location = Location(
                            type = "Point", coordinates = listOf(lng, lat)
                        )
                    )
                    // Send to API
                    SpenoMaticLogger.logErrorMsg("CheckIn Payload", model.toString())
                    checkInApp(model, alertDialog)
                }
            }

            // Set the view and show dialog
            alertDialog.setView(dialogueCheckIn.root)
            alertDialog.setCancelable(false)
            alertDialog.show()

        } catch (e: Exception) {
            Log.e("CustomDialog", "Error showing dialog: $e")
        }
    }

    private fun checkInApp(model: CheckInRequestBody, alertDialog: AlertDialog) {
        homeViewModel.checkInUser(model)
            .observe(viewLifecycleOwner) { response ->
                when (response) {

                    is Resource.Error -> {
                        val errorMessage = extractFirstErrorMessage(response.throwable)
                        SpenoMaticLogger.logErrorMsg("Error", errorMessage.description)
                        showErrorPopup(
                            requireContext(),
                            heading = errorMessage.heading,
                            description = errorMessage.description
                        )

                    }

                    is Resource.Loading -> {

                    }

                    is Resource.Success -> {
                        SharedPrefsHelper.setUserCheckedIn(true)
                        binding.labelExplore.visible()
                        binding.labelExplore.text = "Attendance at ${SharedPrefsHelper.getUserCheckedInTime()}"
                        alertDialog.dismiss()
                        activeDialog = null
                        binding.btnSave.text = "Check Out"

                    }
                }
            }
    }


    private fun checkOutApp(model: CheckOutRequestBody) {
        homeViewModel.checkOutUser(model)
            .observe(viewLifecycleOwner) { response ->
                when (response) {

                    is Resource.Error -> {
                        val errorMessage = extractFirstErrorMessage(response.throwable)
                        SpenoMaticLogger.logErrorMsg("Error", errorMessage.description)
                        showErrorPopup(
                            requireContext(),
                            heading = errorMessage.heading,
                            description = errorMessage.description
                        )

                    }

                    is Resource.Loading -> {

                    }

                    is Resource.Success -> {
                        SharedPrefsHelper.setUserCheckedIn(false)
                        binding.btnSave.text = "Check In"
                        binding.labelExplore.hide()
                        SharedPrefsHelper.setUserCheckedInTime("")
                    }
                }
            }
    }

    private fun getLocalDateTime(): String {
        val calendar = Calendar.getInstance() // Local time

        val fullFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())

        val fullDateTime = fullFormatter.format(calendar.time)
        val checkInTime = timeFormatter.format(calendar.time)

        SharedPrefsHelper.setUserCheckedInTime(checkInTime)

        return fullDateTime
    }

    private fun getData() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                authViewModel.getUserProfile.observe(viewLifecycleOwner) { response ->
                    when (response) {
                        is Resource.Success -> bindData(response.data)
                        is Resource.Error -> {
                            val httpCode = (response.throwable as? HttpException)?.code()
                            val errorMessage = extractFirstErrorMessage(response.throwable)
                            SpenoMaticLogger.logErrorMsg("Error", errorMessage.description)
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    private fun getAttendance() {
        homeViewModel.getAllAttendance()
        homeViewModel.getAllAttendanceLiveData.removeObservers(viewLifecycleOwner)
        homeViewModel.getAllAttendanceLiveData.observe(viewLifecycleOwner) { response ->
            when (response) {

                is Resource.Error -> {
                    val httpCode = (response.throwable as? HttpException)?.code()
                    val errorMessage = extractFirstErrorMessage(response.throwable)
                    SpenoMaticLogger.logErrorMsg("Error", errorMessage.description)
                }
                is Resource.Success -> {
                    setupWeekRecyclerView(response.data?.data)
                }

                else -> Unit
            }
        }
    }

    private fun bindData(data: MeResponseBody?) {
        binding.apply {
            SharedPrefsHelper.setUserEmail(data?.email)
            SharedPrefsHelper.setPhoneNumber(data?.phone)
            SharedPrefsHelper.setUserID(data?.id)
            SharedPrefsHelper.setStatus(data?.status)
            SharedPrefsHelper.setName(data?.fullname)
            SharedPrefsHelper.setUserImage(data?.profile_picture)
            Glide.with(requireContext()).load(data?.profile_picture ?: "")
                .into(containerProfileImage)

            labelHello.text = "Welcome ${data?.fullname.toString()}"


        }
    }

    private fun setupWeekRecyclerView(data: List<Data>?) {
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())   // Mon, Tue
        val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault()) // 02 Sep

        val days = mutableListOf<WeekDay>()
        repeat(7) {
            days.add(
                WeekDay(
                    dayName = dayFormat.format(calendar.time),
                    dateLabel = dateFormat.format(calendar.time),
                    calendar = calendar.clone() as Calendar
                )
            )
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        val adapter = WeekDaysAdapter(days)
        binding.rvDays.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvDays.adapter = adapter
    }


}