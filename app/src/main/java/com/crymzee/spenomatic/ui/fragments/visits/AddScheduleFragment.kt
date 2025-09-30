package com.crymzee.spenomatic.ui.fragments.visits

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crymzee.spenomatic.R
import com.crymzee.spenomatic.adapter.AddServiceDropDownAdapter
import com.crymzee.spenomatic.adapter.DropDownAdapterClient
import com.crymzee.spenomatic.base.BaseFragment
import com.crymzee.spenomatic.databinding.FragmentAddScheduleBinding
import com.crymzee.spenomatic.model.request.CreateVisitRequestBody
import com.crymzee.spenomatic.model.response.customerDetail.Data
import com.crymzee.spenomatic.state.Resource
import com.crymzee.spenomatic.utils.SpenoMaticLogger
import com.crymzee.spenomatic.utils.extractFirstErrorMessage
import com.crymzee.spenomatic.utils.goBack
import com.crymzee.spenomatic.utils.showErrorPopup
import com.crymzee.spenomatic.utils.showSuccessPopup
import com.crymzee.spenomatic.viewModel.CustomersViewModel
import com.crymzee.spenomatic.viewModel.VisitsViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddScheduleFragment : BaseFragment() {
    private lateinit var binding: FragmentAddScheduleBinding
    private lateinit var dropDownAdapterClient: DropDownAdapterClient

    private val customersViewModel: CustomersViewModel by activityViewModels()
    private val visitsViewModel: VisitsViewModel by activityViewModels()
    var clientId = 0
    var visitType = "sales"
    var currentPage = 1
    var perPage = 10

    private var selectedScheduleDate: String? = null // for API (yyyy-MM-dd)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        if (!::binding.isInitialized) {
            binding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_add_schedule,
                container,
                false
            )
            viewInit()
            hideBottomNav()
        }


        return binding.root
    }

    private fun viewInit() {

        dropDownAdapterClient = DropDownAdapterClient(requireContext())
        fetchPaginatedData(currentPage, perPage)


        binding.apply {
            ivBack.setOnClickListener { goBack() }
            layoutSelectLocation.setOnClickListener {
                selectCategory()
            }
            tvSchedule.setOnClickListener {
                selectTab(tvSchedule)
            }
            tvVisited.setOnClickListener {
                selectTab(tvVisited)
            }
            containerTo.setOnClickListener {
                showDatePicker()
            }
            btnSave.setOnClickListener {
                checkValidation()
            }
        }
    }

    private fun checkValidation() {
        val remark = binding.etDescription.text.toString()
        val validationResult = visitsViewModel.validateScheduleInput(
            clientId,
            selectedScheduleDate,
            visitType,
            remark
        )
        if (validationResult.first) {
            val requestBody = CreateVisitRequestBody(
                clientId, remark, selectedScheduleDate.toString(), visitType
            )
            createVisit(requestBody)
        } else {
            val message = getString(validationResult.second)
            showErrorPopup(requireContext(), "", message)
        }
    }


    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(
            requireContext(),
            { _, pickedYear, pickedMonth, pickedDay ->
                val pickedCal = Calendar.getInstance()
                pickedCal.set(pickedYear, pickedMonth, pickedDay)

                val displayFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val displayDate = displayFormat.format(pickedCal.time)

                val apiFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                selectedScheduleDate = apiFormat.format(pickedCal.time)

                binding.etEmail.text = displayDate
            },
            year, month, day
        )

        // Disable past dates
        datePicker.datePicker.minDate = calendar.timeInMillis

        // Show first so buttons exist
        datePicker.show()

        // Change button colors
        datePicker.getButton(DatePickerDialog.BUTTON_POSITIVE)?.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.theme_blue)
        )
        datePicker.getButton(DatePickerDialog.BUTTON_NEGATIVE)?.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.theme_blue)
        )

        // Change title divider/header color
        val dividerId = resources.getIdentifier("titleDivider", "id", "android")
        val divider = datePicker.findViewById<View>(dividerId)
        divider?.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.theme_blue))
    }


    private fun selectCategory() {
        try {
            // Function to load more data
            fun loadMoreData() {
                if (dropDownAdapterClient.itemCount >= 10) { // ✅ Only call API when more than 10 items
                    fetchPaginatedData(currentPage, perPage)
                }
            }

            binding.ivDropDownGender.rotation = 180f

            val dialogView = View.inflate(requireContext(), R.layout.layout_drop_down_new, null)
            val popUp = PopupWindow(
                dialogView,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                false
            ).apply {
                isTouchable = true
                isFocusable = true
                isOutsideTouchable = true
                showAsDropDown(binding.layoutSelectLocation, 0, 0)
                setOnDismissListener {
                    binding.ivDropDownGender.rotation = 0f
                }
            }

            val rvItems: RecyclerView = dialogView.findViewById(R.id.rv_year)
            rvItems.layoutManager = LinearLayoutManager(requireContext())
            rvItems.adapter = dropDownAdapterClient // ✅ Use pre-initialized adapter

            loadMoreData() // ✅ Load first batch

            rvItems.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                    val totalItems = layoutManager.itemCount

                    // ✅ Check if total items > 10 before calling API
                    if (totalItems > 10 && lastVisibleItem >= totalItems - 1) {
                        loadMoreData()
                    }
                }
            })

            dropDownAdapterClient.getClientType {
                binding.tvLocation.text = it.fullname
                popUp.dismiss()
            }

            dropDownAdapterClient.getCategoryId {
                clientId = it.id
                popUp.dismiss()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun fetchPaginatedData(page: Int, size: Int) {
        customersViewModel.page = page
        customersViewModel.perPage = size
        customersViewModel.getAllCustomersVisit()
        customersViewModel.getAllCustomersVisitLiveData.removeObservers(viewLifecycleOwner)
        customersViewModel.getAllCustomersVisitLiveData.observe(viewLifecycleOwner) { response ->

            when (response) {
                is Resource.Error -> {
                    val errorMessage = extractFirstErrorMessage(response.throwable)
                    SpenoMaticLogger.logErrorMsg("Error", errorMessage.description)

                }

                is Resource.Loading -> {}
                is Resource.Success -> {
                    response.data?.let { newItems ->
                        if (newItems.data.isNotEmpty()) {
                            dropDownAdapterClient.addMore(newItems.data)
                            currentPage++
                        }
                    }

                }
            }
        }
    }

    private fun selectTab(selected: View) {
        // reset all first
        val defaultTextColor = ContextCompat.getColor(requireContext(), R.color.black_1717)
        val views = listOf(binding.tvSchedule, binding.tvVisited)

        views.forEach { tv ->
            tv.setBackgroundResource(0) // remove background
            tv.setTextColor(defaultTextColor)
        }

        // set selected style
        (selected as? TextView)?.apply {
            setBackgroundResource(R.drawable.rounded_12_border_black)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            when (id) {
                binding.tvSchedule.id -> {
                    visitType = "sales"
                }

                binding.tvVisited.id -> {
                    visitType = "service"
                }
            }
        }

    }



    private fun createVisit(requestBody: CreateVisitRequestBody) {
        visitsViewModel.createVisit(createVisitRequestBody = requestBody)
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
                            showSuccessPopup(
                                requireContext(),
                                "Success!", "Visit has been schedule successfully",
                                onConfirm = {
                                    goBack()
                                })

                        }

                    }
                }
            }
    }

}
