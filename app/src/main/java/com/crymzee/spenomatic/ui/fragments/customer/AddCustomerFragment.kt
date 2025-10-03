package com.crymzee.spenomatic.ui.fragments.customer

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.location.Geocoder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crymzee.spenomatic.R
import com.crymzee.spenomatic.adapter.AddServiceDropDownAdapter
import com.crymzee.spenomatic.adapter.CustomerContactListAdapter
import com.crymzee.spenomatic.adapter.PlaceAutoSuggestAdapter
import com.crymzee.spenomatic.base.BaseFragment
import com.crymzee.spenomatic.databinding.DialogAddContactBinding
import com.crymzee.spenomatic.databinding.FragmentAddCustomerBinding
import com.crymzee.spenomatic.model.DropDownClientType
import com.crymzee.spenomatic.model.request.CreateCustomerRequestBody
import com.crymzee.spenomatic.model.request.Location
import com.crymzee.spenomatic.model.response.customerDetail.Contact
import com.crymzee.spenomatic.model.response.customerDetail.Data
import com.crymzee.spenomatic.state.Resource
import com.crymzee.spenomatic.utils.SpenoMaticLogger
import com.crymzee.spenomatic.utils.confirmationPopUp
import com.crymzee.spenomatic.utils.extractFirstErrorMessage
import com.crymzee.spenomatic.utils.goBack
import com.crymzee.spenomatic.utils.parseLocation
import com.crymzee.spenomatic.utils.showErrorPopup
import com.crymzee.spenomatic.utils.showSuccessPopup
import com.crymzee.spenomatic.utils.toCamelCase
import com.crymzee.spenomatic.viewModel.CustomersViewModel
import java.io.IOException
import java.util.Locale
import kotlin.math.abs

class AddCustomerFragment : BaseFragment() {
    private lateinit var binding: FragmentAddCustomerBinding
    private lateinit var addServiceDropDownAdapter: AddServiceDropDownAdapter
    private lateinit var customerListAdapter: CustomerContactListAdapter
    private var activeDialog: AlertDialog? = null  // Keep track of the active dialog


    var customerId = 0
    var isEditable = false
    var isFromMap = false
    private val customersViewModel: CustomersViewModel by activityViewModels()

    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        if (!::binding.isInitialized) {
            binding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_add_customer,
                container,
                false
            )
            viewInit()
            hideBottomNav()
        }


        return binding.root
    }

    private fun viewInit() {
        // ✅ Listen for result from MapFragment
        customerId = arguments?.getInt("customerId") ?: 0
        isEditable = arguments?.getBoolean("isEditable") ?: false
        setupAdapters()
        toggleEmptyState()
        val adapter =
            PlaceAutoSuggestAdapter(requireContext(), R.layout.simple_layout_places_suggession)
        binding.apply {
            ivBack.setOnClickListener { goBack() }
            layoutSelectLocation.setOnClickListener {
                selectLocation()
            }
            if (isEditable) {
                labelHeading.text = "Edit Customer"
                btnSave.text = "Update"
                getCustomerDetail(customerId)
            } else {
                customersViewModel.contactList.clear()
                labelHeading.text = "Add Customer"
                btnSave.text = "Add"
            }
            if (!customersViewModel.address.isNullOrEmpty()) {
                val latDirection = if (customersViewModel.lat >= 0.0) "N" else "S"
                val lonDirection = if (customersViewModel.lng >= 0.0) "E" else "W"

                val latLngFormatted = String.format(
                    "%.4f° %s, %.4f° %s",
                    abs(customersViewModel.lat), latDirection,
                    abs(customersViewModel.lng), lonDirection
                )

                etAddress.setText(customersViewModel.address.toString())
                etPinPoints.paintFlags = etPinPoints.paintFlags or Paint.UNDERLINE_TEXT_FLAG
                etPinPoints.text = latLngFormatted

            }
            btnSave.setOnClickListener {
                checkValidation()
            }



            ivPin.setOnClickListener {
                val bundle = Bundle()
                bundle.putInt("customerId", customerId)
                bundle.putBoolean("isEditable", isEditable)
                bundle.putString("address", customersViewModel.address)
                bundle.putDouble("lat", customersViewModel.lat)
                bundle.putDouble("lng", customersViewModel.lng)
                navigate(R.id.action_addCustomerFragment_to_mapFragment, bundle)
            }
            ivAddContact.setOnClickListener {
                addContactPopUp()
            }



            binding.etAddress.setAdapter(adapter)
            binding.etAddress.dropDownVerticalOffset = 0
            binding.etAddress.dropDownAnchor = binding.etAddress.id

            binding.etAddress.onItemClickListener =
                AdapterView.OnItemClickListener { _, _, _, _ ->

                    val geocoder = Geocoder(requireContext(), Locale.getDefault())
                    try {
                        val addresses =
                            geocoder.getFromLocationName(binding.etAddress.text.toString(), 1)

                        if (!addresses.isNullOrEmpty()) {
                            val selectedAddress = addresses[0]

                            val streetAddress = selectedAddress.getAddressLine(0)
                            customersViewModel.lat = selectedAddress.latitude
                            customersViewModel.lng = selectedAddress.longitude

                            // Format lat/lng with N/S, E/W
                            val latDirection = if (customersViewModel.lat >= 0) "N" else "S"
                            val lonDirection = if (customersViewModel.lng >= 0) "E" else "W"

                            val latLngFormatted = String.format(
                                "%.4f° %s, %.4f° %s",
                                abs(customersViewModel.lat), latDirection,
                                abs(customersViewModel.lng), lonDirection
                            )

                            customersViewModel.address = streetAddress

                            // Set the complete address in EditText
                            binding.etAddress.setText(streetAddress)

                            // Show coordinates in a TextView
                            binding.etPinPoints.text = latLngFormatted

                        } else {
                            SpenoMaticLogger.logErrorMsg("Address", "No matching address found")
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                        SpenoMaticLogger.logErrorMsg(
                            "Geocoder Error",
                            "Unable to get street address"
                        )
                    }
                }


            checkLocationPermission()

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        customersViewModel.address = ""
        customersViewModel.lat = 0.0
        customersViewModel.lng = 0.0
    }

    override fun onResume() {
        super.onResume()


        if(customersViewModel.lat >0.0){
            binding.etAddress.setText(customersViewModel.address)
            val latDirection = if (customersViewModel.lat >= 0.0) "N" else "S"
            val lonDirection = if (customersViewModel.lng >= 0.0) "E" else "W"

            val latLngFormatted = String.format(
                "%.4f° %s, %.4f° %s",
                kotlin.math.abs(customersViewModel.lat), latDirection,
                kotlin.math.abs(customersViewModel.lng), lonDirection
            )
            binding.etPinPoints.text = latLngFormatted
            binding.etPinPoints.paintFlags =
                binding.etPinPoints.paintFlags or android.graphics.Paint.UNDERLINE_TEXT_FLAG
        }


    }

    private fun checkValidation() {
        customersViewModel.customerName = binding.etCustomerName.text.toString()
        customersViewModel.address = binding.etAddress.text.toString()
        customersViewModel.industryType = binding.etIndustryType.text.toString()
        customersViewModel.visitFrequency = binding.tvLocation.text.toString().toLowerCase()
        customersViewModel.noOfVisit = binding.etSecondName.text.toString()
        val validationResult = customersViewModel.validateCustomerInput(
            customersViewModel.customerName,
            customersViewModel.address,
            customersViewModel.lat.toString(),
            customersViewModel.industryType,
            customersViewModel.visitFrequency,
            customersViewModel.noOfVisit,
            customersViewModel.contactList.size
        )
        if (validationResult.first) {
            val requestBody = CreateCustomerRequestBody(
                address = customersViewModel.address,
                contacts = customersViewModel.contactList,
                fullname = customersViewModel.customerName.toCamelCase(),
                industry_type = customersViewModel.industryType,
                location = Location(
                    type = "Point",
                    coordinates = listOf(customersViewModel.lng, customersViewModel.lat),
                ),
                number_of_visits = customersViewModel.noOfVisit.toInt(),
                visit_frequency = customersViewModel.visitFrequency?.lowercase().orEmpty(),
            )
            if (isEditable) {
                updateCustomer(customerId, requestBody)
            } else {
                addCustomer(requestBody)
            }

        } else {
            val message = getString(validationResult.second)
            showErrorPopup(requireContext(), "", message)
        }
    }


    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun addCustomer(requestBody: CreateCustomerRequestBody) {
        customersViewModel.addCustomer(createCustomerRequestBody = requestBody)
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
                                "Success!", "Customer has been created successfully",
                                onConfirm = {
                                    navigateClear(R.id.action_addCustomerFragment_to_customerFragment)
                                })

                        }

                    }
                }
            }
    }

    private fun updateCustomer(customerId: Int, requestBody: CreateCustomerRequestBody) {
        customersViewModel.updateCustomer(customerId, createCustomerRequestBody = requestBody)
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
                                "Success!", "Customer has been updated successfully",
                                onConfirm = {
                                    navigateClear(R.id.action_addCustomerFragment_to_customerFragment)
                                })

                        }

                    }
                }
            }
    }

    private fun selectLocation() {
        try {
            val itemList = mutableListOf<DropDownClientType>().apply {
                add(DropDownClientType("Monthly", "1"))
                add(DropDownClientType("Weekly", "2"))

            }

            binding.ivDropDownGender.rotation = 180f

            // Ensure the anchor view is measured before using its width
            binding.layoutSelectGender.post {
                val anchorView = binding.layoutSelectGender
                val width = anchorView.width // get exact width of layoutSelectGender

                // Inflate popup layout
                val dialogView = View.inflate(context, R.layout.layout_drop_down_new, null)

                val popUp = PopupWindow(
                    dialogView,
                    width,  // same width as layoutSelectGender
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    true
                ).apply {
                    isTouchable = true
                    isFocusable = true
                    isOutsideTouchable = true
                    showAsDropDown(anchorView, 0, 0) // show dropdown below
                    setOnDismissListener {
                        binding.ivDropDownGender.rotation = 0f
                    }
                }

                // RecyclerView setup
                val rvItems: RecyclerView = dialogView.findViewById(R.id.rv_year)
                addServiceDropDownAdapter = AddServiceDropDownAdapter(requireContext())
                rvItems.layoutManager = LinearLayoutManager(requireContext())
                rvItems.adapter = addServiceDropDownAdapter
                addServiceDropDownAdapter.addAll(itemList)

                // Handle item selection
                addServiceDropDownAdapter.getClientType {
                    binding.tvLocation.text = it
                    popUp.dismiss()
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupAdapters() {

        customerListAdapter = CustomerContactListAdapter(customersViewModel.contactList)
        binding.rvCustomers.adapter = customerListAdapter

        customerListAdapter.getVisitId { visitId ->
            confirmationPopUp(
                requireContext(),
                heading = "Confirm Deletion",
                description = "Are you sure you want to delete this item? This action cannot be undone.",
                icon = R.drawable.ic_delete_item,
                onConfirm = {
                    // ✅ Remove from adapter
                    customerListAdapter.deleteAction(visitId)

                    // ✅ Keep ViewModel list in sync
                    customersViewModel.contactList.removeIf { it.fullname == visitId }

                    toggleEmptyState()
                }
            )
        }

    }


    fun addContactPopUp() {
        try {
            activeDialog?.dismiss()  // Dismiss any existing dialog before showing a new one

            // Inflate layout using ViewBinding
            val addCustomerBinding = DialogAddContactBinding.inflate(LayoutInflater.from(context))

            val alertDialog = AlertDialog.Builder(context).create()
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            // Store reference to active dialog
            activeDialog = alertDialog


            // OK button with slight delay before invoking the callback
            addCustomerBinding.ivCancel.setOnClickListener {
                alertDialog.dismiss()
                activeDialog = null
            }
            // OK button with slight delay before invoking the callback
            addCustomerBinding.btnCancel.setOnClickListener {

                alertDialog.dismiss()
                activeDialog = null
            }
            addCustomerBinding.btnAdd.setOnClickListener {

                Handler(Looper.getMainLooper()).postDelayed({
                    val email = addCustomerBinding.etEmail.text.toString()
                    val name = addCustomerBinding.etCustomerName.text.toString()
                    val designation = addCustomerBinding.etDesignation.text.toString()
                    val phoneNumber =
                        "${addCustomerBinding.ccp.selectedCountryCodeWithPlus}${addCustomerBinding.etPhoneNumber.text}"

                    val validationResult =
                        customersViewModel.validateUserInput(
                            name,
                            designation,
                            email,
                            phoneNumber,
                            addCustomerBinding.etPhoneNumber.text.toString()
                        )
                    if (validationResult.first) {
                        alertDialog.dismiss()
                        activeDialog = null
                        customersViewModel.contactList.add(
                            Contact(
                                designation = designation,
                                email = email,
                                fullname = name,
                                id = null, // ✅ index as ID
                                phone = phoneNumber
                            )
                        )
                        customerListAdapter.notifyItemInserted(customersViewModel.contactList.size - 1)
                        binding.rvCustomers.scrollToPosition(customersViewModel.contactList.size - 1)
                        toggleEmptyState()

                    } else {
                        val message = getString(validationResult.second)
                        showErrorPopup(requireContext(), "", message)
                    }

                }, 300)
            }

            // Set the view and show dialog
            alertDialog.setView(addCustomerBinding.root)
            alertDialog.setCancelable(false)
            alertDialog.show()

        } catch (e: Exception) {
            Log.e("CustomDialog", "Error showing dialog: $e")
        }
    }

    private fun toggleEmptyState() {
        if (customersViewModel.contactList.isEmpty()) {
            binding.labelNoData.visibility = View.VISIBLE
            binding.rvCustomers.visibility = View.GONE
        } else {
            binding.labelNoData.visibility = View.GONE
            binding.rvCustomers.visibility = View.VISIBLE
        }
    }

    private fun getCustomerDetail(visitId: Int) {
        customersViewModel.getCustomerDetail(visitId)
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
                        response.data?.data?.let { data ->
                            setData(data)

                        }

                    }
                }
            }
    }

    private fun setData(data: Data) {

        if (!isFromMap) {
            val result = parseLocation(data.location)
            if (result != null) {
                val (setlng, setlat) = result
                val latDirection = if (setlat >= 0.0) "N" else "S"
                val lonDirection = if (setlng >= 0.0) "E" else "W"

                val latLngFormatted = String.format(
                    "%.4f° %s, %.4f° %s",
                    abs(setlat), latDirection,
                    abs(setlng), lonDirection
                )
                customersViewModel.lat = setlat
                customersViewModel.lng = setlng
                customersViewModel.address = data.address
                binding.etAddress.setText(data.address)
                binding.etPinPoints.paintFlags =
                    binding.etPinPoints.paintFlags or Paint.UNDERLINE_TEXT_FLAG
                binding.etPinPoints.text = latLngFormatted
            }
        }

        binding.apply {
            etCustomerName.setText(data.fullname)
            etIndustryType.setText(data.industry_type)


            tvLocation.text = data.visit_frequency.toCamelCase()

            etSecondName.setText(data.number_of_visits.toString())
            customersViewModel.contactList.clear()
            customersViewModel.contactList.addAll(data.contacts)
            customerListAdapter.notifyDataSetChanged()

            toggleEmptyState()


        }

    }


}
