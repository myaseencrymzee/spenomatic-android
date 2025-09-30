package com.crymzee.spenomatic.ui.fragments.expenses.addExpenses

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
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
import androidx.recyclerview.widget.RecyclerView
import com.crymzee.spenomatic.R
import com.crymzee.spenomatic.adapter.AddServiceDropDownAdapter
import com.crymzee.spenomatic.adapter.PlaceAutoSuggestAdapter
import com.crymzee.spenomatic.base.BaseFragment
import com.crymzee.spenomatic.databinding.FragmentAddFuelBinding
import com.crymzee.spenomatic.model.DropDownClientType
import com.crymzee.spenomatic.model.request.CreateCustomerRequestBody
import com.crymzee.spenomatic.model.request.Location
import com.crymzee.spenomatic.model.request.OtherExpensesRequest
import com.crymzee.spenomatic.model.request.createFuelExpense.CreateFuelExpenseRequest
import com.crymzee.spenomatic.model.request.createFuelExpense.FuelPumpLocation
import com.crymzee.spenomatic.model.request.createFuelExpense.FuelVoucherDetails
import com.crymzee.spenomatic.state.Resource
import com.crymzee.spenomatic.utils.SpenoMaticLogger
import com.crymzee.spenomatic.utils.extractFirstErrorMessage
import com.crymzee.spenomatic.utils.goBack
import com.crymzee.spenomatic.utils.showErrorPopup
import com.crymzee.spenomatic.utils.showSuccessPopup
import com.crymzee.spenomatic.utils.toCamelCase
import com.crymzee.spenomatic.viewModel.ExpensesViewModel
import java.io.IOException
import java.util.Locale

class AddFuelFragment : BaseFragment() {
    private lateinit var binding: FragmentAddFuelBinding
    private lateinit var addServiceDropDownAdapter: AddServiceDropDownAdapter
    private val expensesViewModel: ExpensesViewModel by activityViewModels()
    private val LOCATION_PERMISSION_REQUEST_CODE = 1


    var petrolType = ""
    var selectedLat = 0.0
    var selectedLng = 0.0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        if (!::binding.isInitialized) {
            binding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_add_fuel,
                container,
                false
            )
            viewInit()
            hideBottomNav()
        }


        return binding.root
    }

    private fun viewInit() {
        binding.apply {

            val adapter =
                PlaceAutoSuggestAdapter(requireContext(), R.layout.simple_layout_places_suggession)
            ivBack.setOnClickListener { goBack() }
            layoutSelectLocation.setOnClickListener {
                selectLocation()
            }

            btnSave.setOnClickListener {
                checkValidation()
            }

            binding.etPumpLocation.setAdapter(adapter)
            binding.etPumpLocation.dropDownVerticalOffset = 0
            binding.etPumpLocation.dropDownAnchor = binding.etPumpLocation.id

            binding.etPumpLocation.onItemClickListener =
                AdapterView.OnItemClickListener { _, _, _, _ ->

                    val geocoder = Geocoder(requireContext(), Locale.getDefault())
                    try {
                        val addresses =
                            geocoder.getFromLocationName(binding.etPumpLocation.text.toString(), 1)

                        if (!addresses.isNullOrEmpty()) {
                            val selectedAddress = addresses[0]

                            val streetAddress = selectedAddress.getAddressLine(0)
                            selectedLat = selectedAddress.latitude
                            selectedLng = selectedAddress.longitude

                            // Set the complete address in EditText
                            binding.etPumpLocation.setText(streetAddress)


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

    private fun checkValidation() {
        val driverName = binding.etCustomerName.text.toString()
        val vehicleNo = binding.etEmail.text.toString()
        val petrolFilled = binding.etFuelLitre.text.toString()
        val pumpName = binding.etFilledFuel.text.toString()
        val pumpLocation = binding.etPumpLocation.text.toString()
        val amount = binding.etAmount.text.toString()
        val mpesaNo = binding.etMpesa.text.toString()
        val startMeter = binding.etStartMeter.text.toString()
        val endMeter = binding.etEndMeter.text.toString()
        val kmMeter = binding.etKiloMeter.text.toString()
        val description = binding.etDescription.text.toString()
        val validationResult = expensesViewModel.validateFuelVoucherInput(
            driverName,vehicleNo,petrolType,pumpName,pumpLocation,petrolFilled,amount,mpesaNo,startMeter,endMeter,description,kmMeter
        )
        if (validationResult.first) {
            val requestBody = CreateFuelExpenseRequest(
              type = "fuel_voucher",
                fuel_voucher_details = FuelVoucherDetails(
                    amount.toString(), driverName, petrolFilled,
                    fuel_pump_location = FuelPumpLocation(
                        coordinates = listOf(selectedLat, selectedLng),
                        type = "Point"

                    ),
                    fuel_pump_name = pumpName,
                    fuel_type = petrolType,
                    km_travelled = kmMeter,
                    places_visited = description,
                    start_meter_reading = startMeter,
                    till_number = mpesaNo.toString(),
                    vehicle_number = vehicleNo,
                )
            )
            addFuelExpense(requestBody)


        } else {
            val message = getString(validationResult.second)
            showErrorPopup(requireContext(), "", message)
        }
    }

    private fun addFuelExpense(model: CreateFuelExpenseRequest) {
        expensesViewModel.createFuelExpenses(model)
            .observe(viewLifecycleOwner) { response ->
                binding.loader.isVisible = response is Resource.Loading<*>

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

                    is Resource.Success -> {
                        showSuccessPopup(
                            requireContext(),
                            "Success!", "Expense has been created successfully",
                            onConfirm = {
                                goBack()
                            })
                    }

                    is Resource.Loading<*> -> {}
                }
            }
    }

    private fun selectLocation() {
        try {
            val itemList = mutableListOf<DropDownClientType>().apply {
                add(DropDownClientType("Petrol", "1"))
                add(DropDownClientType("Diesel", "2"))

            }



            binding.ivDropDownGender.rotation = 180f

            val dialogView = View.inflate(context, R.layout.layout_drop_down_new, null)
            val location = IntArray(2)
            binding.layoutSelectGender.getLocationOnScreen(location)

            val popUp = PopupWindow(
                dialogView,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                false
            ).apply {
                isTouchable = true
                isFocusable = true
                isOutsideTouchable = true
                showAsDropDown(binding.layoutSelectGender, 0, 0)
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
                petrolType = it
                popUp.dismiss()
            }


        } catch (e: Exception) {
            // Handle the exception if needed
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

}
