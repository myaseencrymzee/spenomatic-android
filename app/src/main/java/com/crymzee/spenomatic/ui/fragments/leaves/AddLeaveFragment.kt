package com.crymzee.spenomatic.ui.fragments.leaves

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.crymzee.spenomatic.R
import com.crymzee.spenomatic.adapter.AddServiceDropDownAdapter
import com.crymzee.spenomatic.base.BaseFragment
import com.crymzee.spenomatic.databinding.DialogCustomSuccessBinding
import com.crymzee.spenomatic.databinding.FragmentAddLeavesBinding
import com.crymzee.spenomatic.model.DropDownClientType
import com.crymzee.spenomatic.model.request.CreateLeaveRequest
import com.crymzee.spenomatic.state.Resource
import com.crymzee.spenomatic.utils.SpenoMaticLogger
import com.crymzee.spenomatic.utils.extractFirstErrorMessage
import com.crymzee.spenomatic.utils.goBack
import com.crymzee.spenomatic.utils.showErrorPopup
import com.crymzee.spenomatic.viewModel.LeavesViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddLeaveFragment : BaseFragment() {
    private lateinit var binding: FragmentAddLeavesBinding
    private lateinit var addServiceDropDownAdapter: AddServiceDropDownAdapter
    private var activeDialog: AlertDialog? = null  // Keep track of the active dialog
    private val leavesViewModel: LeavesViewModel by activityViewModels()
    private var startDateApi: String? = null
    private var endDateApi: String? = null
    private var startDateDisplay: String? = null
    private var endDateDisplay: String? = null
    private var type: String? = null
    var totalLeaves = 0.0

    private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        if (!::binding.isInitialized) {
            binding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_add_leaves,
                container,
                false
            )
            viewInit()
            hideBottomNav()
        }


        return binding.root
    }

    private fun viewInit() {
        totalLeaves = arguments?.getDouble("totalLeaves") ?: 0.0
        binding.apply {
            ivBack.setOnClickListener { goBack() }
            layoutSelectLocation.setOnClickListener {
                selectLocation()
            }
            btnSave.setOnClickListener {
                checkValidation()
            }

            // Start Date Picker
            containerFrom.setOnClickListener {
                showEndDatePicker()
            }

            // End Date Picker
            containerTo.setOnClickListener {
                showStartDatePicker()
            }
        }
    }

    private fun showStartDatePicker() {
        val calendar = Calendar.getInstance()

        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)

                // API format (yyyy-MM-dd)
                startDateApi = apiDateFormat.format(calendar.time)

                // Display format (dd-MM-yyyy)
                startDateDisplay = displayDateFormat.format(calendar.time)

                // Set text in "From" field
                binding.etEmail.text = startDateDisplay

                // Reset end date if invalid
                if (endDateApi != null) {
                    val endCal = Calendar.getInstance().apply {
                        time = apiDateFormat.parse(endDateApi!!)
                    }
                    if (endCal.before(calendar) || endCal == calendar) {
                        endDateApi = null
                        endDateDisplay = null
                        binding.etFrom.text = getString(R.string.empty_string)
                    }
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePicker.datePicker.minDate = System.currentTimeMillis()
        datePicker.show()
    }

    private fun showEndDatePicker() {
        if (startDateApi == null) {
            showErrorPopup(requireContext(), "", "Please select a start date first")
            return
        }

        val startCal = Calendar.getInstance().apply {
            time = apiDateFormat.parse(startDateApi!!)
        }

        val calendar = Calendar.getInstance()

        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)

                endDateApi = apiDateFormat.format(calendar.time)   // API
                endDateDisplay = displayDateFormat.format(calendar.time) // UI

                // Set text in "To" field
                binding.etFrom.text = endDateDisplay
            },
            startCal.get(Calendar.YEAR),
            startCal.get(Calendar.MONTH),
            startCal.get(Calendar.DAY_OF_MONTH)
        )

        // End date must be after start date
        datePicker.datePicker.minDate = startCal.timeInMillis + (24 * 60 * 60 * 1000)
        datePicker.show()
    }


    private fun selectLocation() {
        try {
            val itemList = mutableListOf<DropDownClientType>().apply {
                add(DropDownClientType("Full Day", "1"))
                add(DropDownClientType("Half Day", "2"))
            }

            binding.ivDropDownGender.rotation = 180f

            // Ensure width matches anchor (layoutSelectGender)
            binding.layoutSelectLocation.post {
                val anchorView = binding.layoutSelectLocation
                val width = anchorView.width  // ✅ exact width of anchor

                val dialogView = View.inflate(context, R.layout.layout_drop_down_new, null)

                val popUp = PopupWindow(
                    dialogView,
                    width,  // ✅ same as layoutSelectGender width
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    false
                ).apply {
                    isTouchable = true
                    isFocusable = true
                    isOutsideTouchable = true
                    showAsDropDown(anchorView, 0, 0)
                    setOnDismissListener {
                        binding.ivDropDownGender.rotation = 0f
                    }
                }

                val rvItems: RecyclerView = dialogView.findViewById(R.id.rv_year)
                addServiceDropDownAdapter = AddServiceDropDownAdapter(requireContext())
                rvItems.layoutManager = getLinearLayoutManager()
                rvItems.adapter = addServiceDropDownAdapter
                addServiceDropDownAdapter.addAll(itemList)

                addServiceDropDownAdapter.getClientType {
                    binding.tvLocation.text = it
                    type = if (it.equals("Full Day", ignoreCase = true)) {
                        "full_day"
                    } else {
                        "half_day"
                    }
                    popUp.dismiss()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun checkValidation() {
        val remark = binding.etDescription.text.toString()
        val validationResult = leavesViewModel.validateLeaveInput(
            type,
            startDateApi,
            endDateApi,
            remark
        )
        if (validationResult.first) {
            val requestBody = CreateLeaveRequest(
                endDateApi.toString(), remark, startDateApi.toString(), type ?: "",
            )
            addLeave(requestBody)
        } else {
            val message = getString(validationResult.second)
            showErrorPopup(requireContext(), "", message)
        }
    }

    private fun addLeave(requestBody: CreateLeaveRequest) {
        leavesViewModel.createLeaves(createLeaveRequest = requestBody)
            .observe(viewLifecycleOwner) { response ->
                binding.loader.isVisible = response is Resource.Loading
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
                        response.data?.let {
                            leaveSubmitted()
                        }

                    }
                }
            }
    }

    fun leaveSubmitted() {
        try {
            activeDialog?.dismiss()  // Dismiss any existing dialog before showing a new one

            // Inflate layout using ViewBinding
            val dialogueCheckIn = DialogCustomSuccessBinding.inflate(LayoutInflater.from(context))

            val alertDialog = AlertDialog.Builder(context).create()
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            // Store reference to active dialog
            activeDialog = alertDialog
            dialogueCheckIn.descriptionLabel.text =
                "Your leave application has been submitted. You only have $totalLeaves leaves left."

            // OK button with slight delay before invoking the callback
            dialogueCheckIn.ivCancel.setOnClickListener {
                alertDialog.dismiss()
                activeDialog = null
            }

            dialogueCheckIn.btnAdd.setOnClickListener {
                alertDialog.dismiss()
                activeDialog = null

                Handler(Looper.getMainLooper()).postDelayed({
                    goBack()
                }, 300)
            }

            // Set the view and show dialog
            alertDialog.setView(dialogueCheckIn.root)
            alertDialog.setCancelable(false)
            alertDialog.show()

        } catch (e: Exception) {
            Log.e("CustomDialog", "Error showing dialog: $e")
        }
    }

}
