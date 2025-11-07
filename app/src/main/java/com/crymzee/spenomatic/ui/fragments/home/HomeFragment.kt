package com.crymzee.spenomatic.ui.fragments.home

import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
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
import com.crymzee.spenomatic.model.response.dashboardData.DashboardDataResponse
import com.crymzee.spenomatic.model.response.meResponse.MeResponseBody
import com.crymzee.spenomatic.services.TrackingWorker
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
import java.time.LocalDate
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class HomeFragment : BaseFragment() {
    private lateinit var binding: FragmentHomeBinding
    private val authViewModel: AuthViewModel by activityViewModels()
    private val homeViewModel: HomeViewModel by activityViewModels()
    private var activeDialog: AlertDialog? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        if (!::binding.isInitialized) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
            hideKeyboard()
            // ✅ Check on fragment start
            checkPreviousCheckIn()

            // ✅ If not checked in — show popup
            if (SharedPrefsHelper.getUserCheckedIn() == false || SharedPrefsHelper.getUserCheckedIn() == null) {
                hideKeyboard()
                addCheckInPopup()
            }

            showBottomNav()

            viewInit()
        }

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
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

            val role = SharedPrefsHelper.getRole()
            if(role == "office"){
                containerEarningWeek.hide()
                val params = containerCompletedWeek.layoutParams as ConstraintLayout.LayoutParams
                params.width = 0 // same as 0dp in XML (match_constraint)
                containerCompletedWeek.layoutParams = params
            }else{
                containerEarningWeek.visible()
                val params = containerCompletedWeek.layoutParams as ConstraintLayout.LayoutParams
                params.width = 0 // same as 0dp in XML (match_constraint)
                containerCompletedWeek.layoutParams = params

            }

            if (!SharedPrefsHelper.getUserCheckedInTime().isNullOrEmpty()) {
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
                        SpenoMaticLogger.logErrorMsg("CheckOut Payload", model.toString())
                        checkOutApp(model)
                    }
                } else {
                    addCheckInPopup()
                }
            }
        }
    }

    // ✅ Modern, safe date validation logic added here
    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkPreviousCheckIn() {
        val lastCheckInDate = SharedPrefsHelper.getUserCheckedInDate()
        val today = LocalDate.now().toString()

        if (lastCheckInDate.isNullOrEmpty()) {
            // First-time or cleared prefs
            addCheckInPopup()
            return
        }

        try {
            val lastDate = LocalDate.parse(lastCheckInDate)
            val currentDate = LocalDate.now()

            when {
                lastDate.isBefore(currentDate) -> {
                    // ✅ Previous date → reset
                    SharedPrefsHelper.setUserCheckedInTimeDate(null)
                    SharedPrefsHelper.setUserCheckedInTime("")
                    SharedPrefsHelper.setUserCheckedIn(false)
                    addCheckInPopup()
                }

                lastDate.isEqual(currentDate) -> {
                    // ✅ Same-day — do nothing
                }

                else -> {
                    // Future date — reset (corrupted data case)
                    SharedPrefsHelper.setUserCheckedInTimeDate(null)
                    SharedPrefsHelper.setUserCheckedInTime("")
                    SharedPrefsHelper.setUserCheckedIn(false)
                    addCheckInPopup()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            SharedPrefsHelper.setUserCheckedInTimeDate(null)
            SharedPrefsHelper.setUserCheckedInTime("")
            SharedPrefsHelper.setUserCheckedIn(false)
            addCheckInPopup()
        }
    }


    private fun getCurrentLocation(onLocationReady: (Double, Double) -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        if (ActivityCompat.checkSelfPermission(
                requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                onLocationReady(location.latitude, location.longitude)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun addCheckInPopup() {
        try {
            activeDialog?.dismiss()
            val dialogueCheckIn =
                DialogCheckInUserBinding.inflate(LayoutInflater.from(requireContext()))
            val alertDialog = AlertDialog.Builder(requireContext()).create()
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            activeDialog = alertDialog

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
                    SpenoMaticLogger.logErrorMsg("CheckIn Payload", model.toString())
                    checkInApp(model, alertDialog)
                }
            }

            alertDialog.setView(dialogueCheckIn.root)
            alertDialog.setCancelable(false)
            alertDialog.show()

        } catch (e: Exception) {
            Log.e("CustomDialog", "Error showing dialog: $e")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkInApp(model: CheckInRequestBody, alertDialog: AlertDialog) {
        homeViewModel.checkInUser(model)
            .observe(viewLifecycleOwner) { response ->
                when (response) {
                    is Resource.Error -> {
                        val errorMessage = extractFirstErrorMessage(response.throwable)
                        showErrorPopup(
                            requireContext(),
                            heading = errorMessage.heading,
                            description = errorMessage.description
                        )
                    }

                    is Resource.Success -> {
                        getDashBoardData()
                        val checkInId = response.data?.id ?: return@observe

                        // Save state
                        SharedPrefsHelper.setUserCheckedIn(true)
                        SharedPrefsHelper.setUserCheckedInTimeDate(LocalDate.now().toString())
                        SharedPrefsHelper.setUserCheckedInId(checkInId.toString())

                        binding.labelExplore.visible()
                        binding.labelExplore.text =
                            "Attendance at ${SharedPrefsHelper.getUserCheckedInTime()}"
                        alertDialog.dismiss()
                        activeDialog = null
                        binding.btnSave.text = "Check Out"
                        val role = SharedPrefsHelper.getRole()
                        // Schedule tracking
                        if (role != "office") {
                            val initialDelay = TrackingWorker.randomMinutes(1, 15)
                            TrackingWorker.scheduleOnce(
                                requireContext().applicationContext,
                                delayMinutes = initialDelay,
                                isFirstRun = true,
                                checkInId = checkInId.toString() // ✅ Pass ID
                            )
                        }

                    }


                    else -> Unit
                }
            }
    }

    private fun checkOutApp(model: CheckOutRequestBody) {
        homeViewModel.checkOutUser(model)
            .observe(viewLifecycleOwner) { response ->
                when (response) {
                    is Resource.Success -> {
                        SharedPrefsHelper.setUserCheckedIn(false)
                        SharedPrefsHelper.setUserCheckedInId(null)
                        TrackingWorker.cancelTracking(requireContext().applicationContext)

                        SharedPrefsHelper.setUserCheckedIn(false)
                        SharedPrefsHelper.setUserCheckedInTime("")
                        SharedPrefsHelper.setUserCheckedInTimeDate(null)
                        binding.btnSave.text = "Check In"
                        binding.labelExplore.hide()
                    }

                    is Resource.Error -> {
                        val errorMessage = extractFirstErrorMessage(response.throwable)
                        showErrorPopup(
                            requireContext(),
                            heading = errorMessage.heading,
                            description = errorMessage.description
                        )
                    }

                    else -> Unit
                }
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getLocalDateTime(): String {
        val calendar = Calendar.getInstance()
        val fullFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())

        val fullDateTime = fullFormatter.format(calendar.time)
        val checkInTime = timeFormatter.format(calendar.time)

        SharedPrefsHelper.setUserCheckedInTime(checkInTime)
        SharedPrefsHelper.setUserCheckedInTimeDate(LocalDate.now().toString()) // ✅ Save date
        return fullDateTime
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getData()
        getAttendance()
        getDashBoardData()
    }

    override fun onResume() {
        super.onResume()
        authViewModel.getUserProfile()
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

    private fun getDashBoardData() {
        homeViewModel.getAllDashboardData()
        homeViewModel.getDashboardDataLiveData.removeObservers(viewLifecycleOwner)
        homeViewModel.getDashboardDataLiveData.observe(viewLifecycleOwner) { response ->
            binding.loader.isVisible = response is Resource.Loading<*>
            when (response) {

                is Resource.Error -> {
                    val errorMessage = extractFirstErrorMessage(response.throwable)
                    SpenoMaticLogger.logErrorMsg("Error", errorMessage.description)
                }

                is Resource.Success -> {
                    bindDashBoardData(response.data)
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
            SharedPrefsHelper.setUserRole(data?.role)
            SharedPrefsHelper.setUserImage(data?.profile_picture)
            Glide.with(requireContext()).load(data?.profile_picture ?: "")
                .into(containerProfileImage)

            labelHello.text = "Welcome ${data?.fullname.toString()}"


        }
    }


    private fun bindDashBoardData(data: DashboardDataResponse?) {
        binding.apply {
            tvEarnings.text = data?.total_distance.toString()
            tvIdle.text = data?.idle_time?.toString() ?: "--"
            tvReviews.text = getTimeAgo(data?.today_attendance?.check_in)
            tvAverageRating.text = formatToLocalTime(data?.today_attendance?.check_in ) ?: "--"

            tvApproved.text = data?.approved_expenses.toString()
            tvPending.text = data?.pending_expenses.toString()
            tvRejected.text = data?.rejected_expenses.toString()

            containerInfo.tvPresent.text = data?.total_present.toString()
            containerInfo.tvAbsent.text = data?.total_absent.toString()
            containerInfo.tvAvailableLeaves.text = data?.leaves_left.toString()
            containerInfo.tvHalfDay.text = data?.half_day_leaves.toString()
            containerInfo.tvOnLeave.text = data?.full_day_leaves.toString()

        }
    }

    private fun setupWeekRecyclerView(attendanceList: List<Data>?) {
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())   // Mon, Tue
        val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault()) // 02 Sep
        val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val today = Calendar.getInstance()
        val days = mutableListOf<WeekDay>()

        repeat(7) {
            val dateStr = apiDateFormat.format(calendar.time)
            val matchingAttendance = attendanceList?.find { it.date == dateStr }

            // You can extend WeekDay dynamically with matchingAttendance if needed,
            // but for now, we’ll just use it later in the adapter (via lookup map).

            days.add(
                WeekDay(
                    dayName = dayFormat.format(calendar.time),
                    dateLabel = dateFormat.format(calendar.time),
                    calendar = calendar.clone() as Calendar
                )
            )
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        // Create a quick lookup for attendance by date
        val attendanceMap = attendanceList?.associateBy { it.date } ?: emptyMap()

        val adapter = WeekDaysAdapter(days, attendanceMap)
        binding.rvDays.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvDays.adapter = adapter
    }

    fun getTimeAgo(utcTime: String?): String {
        if (utcTime.isNullOrEmpty()) return "--"

        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")

            val past = sdf.parse(utcTime)
            val now = Date()

            val diff = now.time - (past?.time ?: 0)

            val seconds = TimeUnit.MILLISECONDS.toSeconds(diff)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
            val hours = TimeUnit.MILLISECONDS.toHours(diff)
            val days = TimeUnit.MILLISECONDS.toDays(diff)

            when {
                seconds < 60 -> "Just now"
                minutes < 60 -> "$minutes minute${if (minutes > 1) "s" else ""} "
                hours < 24 -> "$hours hour${if (hours > 1) "s" else ""} "
                days < 7 -> "$days day${if (days > 1) "s" else ""} "
                else -> SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(past ?: now)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }


    fun formatToLocalTime(utcTime: String?): String? {
        if (utcTime.isNullOrEmpty()) return "--"

        return try {
            // Parse UTC timestamp
            val utcFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
            utcFormat.timeZone = TimeZone.getTimeZone("UTC")

            val date = utcFormat.parse(utcTime) ?: return ""

            // Format to local 12-hour time (e.g., 9:30 AM)
            val localFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
            localFormat.timeZone = TimeZone.getDefault()

            localFormat.format(date)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}
