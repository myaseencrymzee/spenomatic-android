package com.crymzee.spenomatic.ui.fragments.expenses.addOutCityExpense

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crymzee.spenomatic.R
import com.crymzee.spenomatic.adapter.AllAllowanceExpenseListAdapter
import com.crymzee.spenomatic.adapter.AllBusesTrainExpenseListAdapter
import com.crymzee.spenomatic.adapter.AllLodgingExpenseListAdapter
import com.crymzee.spenomatic.adapter.AllMiscellaneousExpenseListAdapter
import com.crymzee.spenomatic.adapter.AllTransportExpenseListAdapter
import com.crymzee.spenomatic.adapter.DropDownAdapterClient
import com.crymzee.spenomatic.adapter.OutCityCustomerVisitListAdapter
import com.crymzee.spenomatic.base.BaseFragment
import com.crymzee.spenomatic.databinding.DialogAddAllowanceExpenseBinding
import com.crymzee.spenomatic.databinding.DialogAddBusTrianExpenseBinding
import com.crymzee.spenomatic.databinding.DialogAddLodgingExpenseBinding
import com.crymzee.spenomatic.databinding.DialogAddMiscellaneousExpenseBinding
import com.crymzee.spenomatic.databinding.DialogAddTransportExpenseBinding
import com.crymzee.spenomatic.databinding.DialogAddUserBinding
import com.crymzee.spenomatic.databinding.FragmentAddOutCityVisitBinding
import com.crymzee.spenomatic.model.request.createLocalExpense.CreateLocalExpenseRequest
import com.crymzee.spenomatic.model.request.createLocalExpense.Customer
import com.crymzee.spenomatic.model.request.createLocalExpense.MiscellaneousExpense
import com.crymzee.spenomatic.model.request.createLocalExpense.TransportExpense
import com.crymzee.spenomatic.model.request.createOutsideExpense.BusTrainExpense
import com.crymzee.spenomatic.model.request.createOutsideExpense.LodgingBoardingExpense
import com.crymzee.spenomatic.model.request.createOutsideExpense.TravelAllowance
import com.crymzee.spenomatic.state.Resource
import com.crymzee.spenomatic.utils.SpenoMaticLogger
import com.crymzee.spenomatic.utils.confirmationPopUp
import com.crymzee.spenomatic.utils.extractFirstErrorMessage
import com.crymzee.spenomatic.utils.goBack
import com.crymzee.spenomatic.utils.showErrorPopup
import com.crymzee.spenomatic.utils.showSuccessPopup
import com.crymzee.spenomatic.viewModel.CustomersViewModel
import com.crymzee.spenomatic.viewModel.ExpensesViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddOutCityVisitFragment : BaseFragment() {
    private lateinit var binding: FragmentAddOutCityVisitBinding
    private lateinit var dropDownAdapterClient: DropDownAdapterClient
    private lateinit var allMiscellaneousExpenseListAdapter: AllMiscellaneousExpenseListAdapter
    private lateinit var allTransportExpenseListAdapter: AllTransportExpenseListAdapter
    private lateinit var allAllowanceExpenseListAdapter: AllAllowanceExpenseListAdapter
    private lateinit var allBusesTrainExpenseListAdapter: AllBusesTrainExpenseListAdapter
    private lateinit var allLodgingExpenseListAdapter: AllLodgingExpenseListAdapter
    private lateinit var outCityCustomerVisitListAdapter: OutCityCustomerVisitListAdapter
    private var activeDialog: AlertDialog? = null  // Keep track of the active dialog
    val userList: MutableList<Customer> = mutableListOf()
    val miscellaneousListExpense: MutableList<MiscellaneousExpense> = mutableListOf()
    val transportExpenseList: MutableList<TransportExpense> = mutableListOf()
    val lodgingExpenseList: MutableList<LodgingBoardingExpense> = mutableListOf()
    val busTimingExpenseList: MutableList<BusTrainExpense> = mutableListOf()
    val allowanceExpenseList: MutableList<TravelAllowance> = mutableListOf()
    private var dialogueAddUser: DialogAddUserBinding? = null
    private val customersViewModel: CustomersViewModel by activityViewModels()
    private val expensesViewModel: ExpensesViewModel by activityViewModels()

    // At top of your Fragment class
    private var selectedCustomer: Customer? = null

    var currentPage = 1
    var perPage = 10
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        if (!::binding.isInitialized) {
            binding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_add_out_city_visit,
                container,
                false
            )
            hideBottomNav()
            viewInit()

        }


        return binding.root
    }

    private fun viewInit() {
        setupAdapters()
        toggleEmptyStateMiscellaneous()
        toggleEmptyTransportState()
        toggleEmptyStateClient()
        toggleEmptyStateBusTime()
        toggleEmptyStateLodging()
        toggleEmptyStateAllowance()
        dropDownAdapterClient = DropDownAdapterClient(requireContext())
        fetchPaginatedData(currentPage, perPage)
        binding.apply {
            ivBack.setOnClickListener { goBack() }
            ivAddUser.setOnClickListener { addUserList() }
            ivAddTransport.setOnClickListener { addTransportExpenses() }
            ivAddBusTrain.setOnClickListener { addBusTrainExpenses() }
            ivAddAllowance.setOnClickListener { addAllowanceExpenses() }
            ivAddLodging.setOnClickListener { addLodgingExpenses() }
            ivAddMiscellaneous.setOnClickListener { addMiscellaneousExpenses() }

            btnSave.setOnClickListener {
                if (userList.isEmpty()) {
                    showErrorPopup(requireContext(), "", "Please select a client to visit")
                } else if (transportExpenseList.isEmpty()) {
                    showErrorPopup(requireContext(), "", "Please add transport expense")
                } else if (lodgingExpenseList.isEmpty()) {
                    showErrorPopup(requireContext(), "", "Please add lodging expense")
                }else if (busTimingExpenseList.isEmpty()) {
                    showErrorPopup(requireContext(), "", "Please add bus/train expense")
                } else if (allowanceExpenseList.isEmpty()) {
                    showErrorPopup(requireContext(), "", "Please add allowance expense")
                }else if (miscellaneousListExpense.isEmpty()) {
                    showErrorPopup(requireContext(), "", "Please add miscellaneous expense")
                } else {
                    showSuccessPopup(
                        requireContext(),
                        "Success!", "Expense has been created successfully",
                        onConfirm = {
                            goBack()
                        })
                }
            }
        }
    }

    private fun setupAdapters() {

        outCityCustomerVisitListAdapter = OutCityCustomerVisitListAdapter(userList)
        outCityCustomerVisitListAdapter.getVisitId { visitId ->
            confirmationPopUp(
                requireContext(),
                heading = "Confirm Deletion",
                description = "Are you sure you want to delete this item? This action cannot be undone.",
                icon = R.drawable.ic_delete_item,
                onConfirm = {
                    // ✅ Remove from adapter
                    outCityCustomerVisitListAdapter.deleteAction(visitId)

                    // ✅ Keep ViewModel list in sync
                    userList.removeIf { it.name == visitId }
                    toggleEmptyStateClient()
                }
            )
        }
        binding.rvCustomers.adapter = outCityCustomerVisitListAdapter
        allMiscellaneousExpenseListAdapter =
            AllMiscellaneousExpenseListAdapter(miscellaneousListExpense)
        allMiscellaneousExpenseListAdapter.getVisitId { visitId ->
            confirmationPopUp(
                requireContext(),
                heading = "Confirm Deletion",
                description = "Are you sure you want to delete this item? This action cannot be undone.",
                icon = R.drawable.ic_delete_item,
                onConfirm = {
                    // ✅ Remove from adapter
                    allMiscellaneousExpenseListAdapter.deleteAction(visitId)

                    // ✅ Keep ViewModel list in sync
                    miscellaneousListExpense.removeIf { it.objective == visitId }
                    toggleEmptyStateMiscellaneous()
                }
            )
        }
        binding.rvMiscellaneous.adapter = allMiscellaneousExpenseListAdapter
        allTransportExpenseListAdapter = AllTransportExpenseListAdapter(transportExpenseList)
        allTransportExpenseListAdapter.getVisitId { visitId ->
            confirmationPopUp(
                requireContext(),
                heading = "Confirm Deletion",
                description = "Are you sure you want to delete this item? This action cannot be undone.",
                icon = R.drawable.ic_delete_item,
                onConfirm = {
                    // ✅ Remove from adapter
                    allTransportExpenseListAdapter.deleteAction(visitId)

                    // ✅ Keep ViewModel list in sync
                    transportExpenseList.removeIf { it.from_location == visitId }
                    toggleEmptyTransportState()
                }
            )

        }
        binding.rvTransport.adapter = allTransportExpenseListAdapter

        allLodgingExpenseListAdapter = AllLodgingExpenseListAdapter(lodgingExpenseList)
        allLodgingExpenseListAdapter.getVisitId { visitId ->
            confirmationPopUp(
                requireContext(),
                heading = "Confirm Deletion",
                description = "Are you sure you want to delete this item? This action cannot be undone.",
                icon = R.drawable.ic_delete_item,
                onConfirm = {
                    // ✅ Remove from adapter
                    allLodgingExpenseListAdapter.deleteAction(visitId)

                    // ✅ Keep ViewModel list in sync
                    lodgingExpenseList.removeIf { it.nights_stayed == visitId }
                    toggleEmptyStateLodging()
                }
            )
        }
        binding.rvLoging.adapter = allLodgingExpenseListAdapter
        allAllowanceExpenseListAdapter = AllAllowanceExpenseListAdapter(allowanceExpenseList)
        allAllowanceExpenseListAdapter.getVisitId { visitId ->
            confirmationPopUp(
                requireContext(),
                heading = "Confirm Deletion",
                description = "Are you sure you want to delete this item? This action cannot be undone.",
                icon = R.drawable.ic_delete_item,
                onConfirm = {
                    // ✅ Remove from adapter
                    allAllowanceExpenseListAdapter.deleteAction(visitId)

                    // ✅ Keep ViewModel list in sync
                    allowanceExpenseList.removeIf { it.allowance_type == visitId }
                    toggleEmptyStateAllowance()
                }
            )
        }
        binding.rvAllowance.adapter = allAllowanceExpenseListAdapter

        allBusesTrainExpenseListAdapter = AllBusesTrainExpenseListAdapter(busTimingExpenseList)
        allBusesTrainExpenseListAdapter.getVisitId { visitId ->
            confirmationPopUp(
                requireContext(),
                heading = "Confirm Deletion",
                description = "Are you sure you want to delete this item? This action cannot be undone.",
                icon = R.drawable.ic_delete_item,
                onConfirm = {
                    // ✅ Remove from adapter
                    allBusesTrainExpenseListAdapter.deleteAction(visitId)

                    // ✅ Keep ViewModel list in sync
                    busTimingExpenseList.removeIf { it.date == visitId }
                    toggleEmptyStateBusTime()
                }
            )
        }
        binding.rvBusTrain.adapter = allBusesTrainExpenseListAdapter


    }

    private fun toggleEmptyTransportState() {
        if (transportExpenseList.isEmpty()) {
            binding.labelNoTransportData.visibility = View.VISIBLE
            binding.rvTransport.visibility = View.GONE
        } else {
            binding.labelNoTransportData.visibility = View.GONE
            binding.rvTransport.visibility = View.VISIBLE
        }
    }

    private fun toggleEmptyStateClient() {
        if (userList.isEmpty()) {
            binding.labelNoUserData.visibility = View.VISIBLE
            binding.rvCustomers.visibility = View.GONE
        } else {
            binding.labelNoUserData.visibility = View.GONE
            binding.rvCustomers.visibility = View.VISIBLE
        }
    }

    private fun toggleEmptyStateMiscellaneous() {
        if (miscellaneousListExpense.isEmpty()) {
            binding.labelNoMiscellaneousData.visibility = View.VISIBLE
            binding.rvMiscellaneous.visibility = View.GONE
        } else {
            binding.labelNoMiscellaneousData.visibility = View.GONE
            binding.rvMiscellaneous.visibility = View.VISIBLE
        }
    }

    private fun toggleEmptyStateBusTime() {
        if (busTimingExpenseList.isEmpty()) {
            binding.labelNoBusTrainData.visibility = View.VISIBLE
            binding.rvBusTrain.visibility = View.GONE
        } else {
            binding.labelNoBusTrainData.visibility = View.GONE
            binding.rvBusTrain.visibility = View.VISIBLE
        }
    }

    private fun toggleEmptyStateLodging() {
        if (lodgingExpenseList.isEmpty()) {
            binding.labelNoLodgingData.visibility = View.VISIBLE
            binding.rvLoging.visibility = View.GONE
        } else {
            binding.labelNoLodgingData.visibility = View.GONE
            binding.rvLoging.visibility = View.VISIBLE
        }
    }

    private fun toggleEmptyStateAllowance() {
        if (allowanceExpenseList.isEmpty()) {
            binding.labelNoAllowanceData.visibility = View.VISIBLE
            binding.rvAllowance.visibility = View.GONE
        } else {
            binding.labelNoAllowanceData.visibility = View.GONE
            binding.rvAllowance.visibility = View.VISIBLE
        }
    }

    fun addTransportExpenses() {
        try {
            activeDialog?.dismiss()  // Dismiss any existing dialog before showing a new one

            // Inflate layout using ViewBinding
            val dialogueTrans =
                DialogAddTransportExpenseBinding.inflate(LayoutInflater.from(context))

            val alertDialog = AlertDialog.Builder(context).create()
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            // Store reference to active dialog
            activeDialog = alertDialog


            // OK button with slight delay before invoking the callback
            dialogueTrans.ivCancel.setOnClickListener {
                alertDialog.dismiss()
                activeDialog = null
            }

            dialogueTrans.btnAdd.setOnClickListener {
                Handler(Looper.getMainLooper()).postDelayed({
                    val from = dialogueTrans.etCustomerName.text.toString().trim()
                    val to = dialogueTrans.etContact.text.toString().trim()
                    val amount = dialogueTrans.etEmail.text.toString().trim()

                    val validationResult = expensesViewModel.validateUserInput(from, to, amount)

                    if (validationResult.first) {
                        val amountValue = amount
                        if (amountValue == null || amountValue <= "0.0") {
                            showErrorPopup(requireContext(), "", "Please enter a valid amount")
                            return@postDelayed
                        }

                        // Add item to list
                        transportExpenseList.add(
                            TransportExpense(amountValue.toString(), from, to)
                        )

                        // Notify adapter & scroll
                        allTransportExpenseListAdapter.notifyItemInserted(transportExpenseList.size - 1)
                        binding.rvTransport.scrollToPosition(transportExpenseList.size - 1)
                        toggleEmptyTransportState()
                        alertDialog.dismiss()
                        activeDialog = null
                    } else {
                        val message = getString(validationResult.second)
                        showErrorPopup(requireContext(), "", message)
                    }
                }, 300)
            }

            // Set the view and show dialog
            alertDialog.setView(dialogueTrans.root)
            alertDialog.setCancelable(false)
            alertDialog.show()

        } catch (e: Exception) {
            Log.e("CustomDialog", "Error showing dialog: $e")
        }
    }

    fun addUserList() {
        try {
            activeDialog?.dismiss()

            // Always create a new binding
            val dialogueAddUser = DialogAddUserBinding.inflate(LayoutInflater.from(context))

            val alertDialog = AlertDialog.Builder(context).create()
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            activeDialog = alertDialog

            // Cancel button
            dialogueAddUser.ivCancel.setOnClickListener {
                alertDialog.dismiss()
                activeDialog = null
            }

            // DropDown click
            dialogueAddUser.layoutSelectLocation.setOnClickListener {
                selectCategory(dialogueAddUser)
            }

            // Add button
            dialogueAddUser.btnAdd.setOnClickListener {
                val description = dialogueAddUser.etDescription.text.toString()
                Handler(Looper.getMainLooper()).postDelayed({
                    if (selectedCustomer == null) {
                        showErrorPopup(requireContext(), "", "Please select client")
                    } else if (description.isEmpty()) {
                        showErrorPopup(requireContext(), "", "Please enter objective with client")
                    } else {
                        val newCustomer = Customer(
                            customer = selectedCustomer?.customer ?: 0,
                            objective = description,
                            name = selectedCustomer?.name ?: "",
                            email = selectedCustomer?.email ?: ""
                        )

                        userList.add(newCustomer)
                        outCityCustomerVisitListAdapter.notifyItemInserted(userList.size - 1)
                        binding.rvCustomers.scrollToPosition(userList.size - 1)
                        selectedCustomer = null
                        toggleEmptyStateClient()
                        alertDialog.dismiss()
                        activeDialog = null
                    }
                }, 300)
            }

            alertDialog.setView(dialogueAddUser.root)
            alertDialog.setCancelable(false)
            alertDialog.show()

        } catch (e: Exception) {
            Log.e("CustomDialog", "Error showing dialog: $e")
        }
    }



    fun addAllowanceExpenses() {
        try {
            activeDialog?.dismiss()  // Dismiss any existing dialog before showing a new one

            // Inflate layout using ViewBinding
            val dialogueAllowance =
                DialogAddAllowanceExpenseBinding.inflate(LayoutInflater.from(context))

            val alertDialog = AlertDialog.Builder(context).create()
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            // Store reference to active dialog
            activeDialog = alertDialog


            // OK button with slight delay before invoking the callback
            dialogueAllowance.ivCancel.setOnClickListener {
                alertDialog.dismiss()
                activeDialog = null
            }

            dialogueAllowance.btnAdd.setOnClickListener {
                Handler(Looper.getMainLooper()).postDelayed({
                    val type = dialogueAllowance.etAllowance.text.toString().trim()
                    val description = dialogueAllowance.etDescription.text.toString().trim()
                    val amount = dialogueAllowance.etEmail.text.toString().trim()

                    val validationResult =
                        expensesViewModel.validateAllowanceInput(type, amount, description)

                    if (validationResult.first) {
                        // Add item to list
                        allowanceExpenseList.add(
                            TravelAllowance(type, amount, description)
                        )

                        // Notify adapter & scroll
                        allAllowanceExpenseListAdapter.notifyItemInserted(
                            allowanceExpenseList.size - 1
                        )
                        binding.rvAllowance.scrollToPosition(allowanceExpenseList.size - 1)
                        toggleEmptyStateAllowance()
                        alertDialog.dismiss()
                        activeDialog = null
                    } else {
                        val message = getString(validationResult.second)
                        showErrorPopup(requireContext(), "", message)
                    }
                }, 300)
            }

            // Set the view and show dialog
            alertDialog.setView(dialogueAllowance.root)
            alertDialog.setCancelable(false)
            alertDialog.show()

        } catch (e: Exception) {
            Log.e("CustomDialog", "Error showing dialog: $e")
        }
    }

    private fun selectCategory(dialogueAddUser: DialogAddUserBinding) {
        try {
            // Function to load more data
            fun loadMoreData() {
                if (dropDownAdapterClient.itemCount >= 10) {
                    fetchPaginatedData(currentPage, perPage)
                }
            }

            // Rotate dropdown icon
            dialogueAddUser.ivDropDownGender.rotation = 180f

            // Wait until layoutSelectLocation has a width
            dialogueAddUser.layoutSelectLocation.post {
                val anchorView = dialogueAddUser.layoutSelectLocation
                val width = anchorView.width  // exact width of selectLocation layout

                // Inflate popup layout
                val dialogView = View.inflate(requireContext(), R.layout.layout_drop_down_new, null)

                val popUp = PopupWindow(
                    dialogView,
                    width, // make popup same width as anchor view
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    true
                ).apply {
                    isTouchable = true
                    isFocusable = true
                    isOutsideTouchable = true
                    showAsDropDown(anchorView, 0, 0) // show below anchor view
                    setOnDismissListener {
                        dialogueAddUser.ivDropDownGender.rotation = 0f
                    }
                }

                // RecyclerView setup inside popup
                val rvItems: RecyclerView = dialogView.findViewById(R.id.rv_year)
                rvItems.layoutManager = LinearLayoutManager(requireContext())
                rvItems.adapter = dropDownAdapterClient

                // Load first batch
                loadMoreData()

                // Add scroll listener for pagination
                rvItems.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)

                        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                        val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                        val totalItems = layoutManager.itemCount

                        if (totalItems > 10 && lastVisibleItem >= totalItems - 1) {
                            loadMoreData()
                        }
                    }
                })

                // Handle item selection
                dropDownAdapterClient.getClientType {
                    selectedCustomer = Customer(
                        customer = it.id,
                        objective = dialogueAddUser.etDescription.text.toString(),
                        name = it.fullname,
                        email = it.address
                    )
                    dialogueAddUser.tvLocation.text = selectedCustomer?.name


                    popUp.dismiss()
                }
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

    fun addBusTrainExpenses() {
        try {
            activeDialog?.dismiss()  // Dismiss any existing dialog before showing a new one

            // Inflate layout using ViewBinding
            val dialogueTrains =
                DialogAddBusTrianExpenseBinding.inflate(LayoutInflater.from(context))

            val alertDialog = AlertDialog.Builder(context).create()
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            // Store reference to active dialog
            activeDialog = alertDialog

            // Cancel button
            dialogueTrains.ivCancel.setOnClickListener {
                alertDialog.dismiss()
                activeDialog = null
            }

            // --- Date Picker ---
            dialogueTrains.containerCustomerName.setOnClickListener {
                val calendar = Calendar.getInstance()
                val datePicker = DatePickerDialog(
                    requireContext(),
                    { _, year, month, dayOfMonth ->
                        // Format date: yyyy-MM-dd
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        calendar.set(year, month, dayOfMonth)
                        dialogueTrains.etCustomerName.text = sdf.format(calendar.time)
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )

                // ✅ Prevent selecting past dates
                datePicker.datePicker.minDate = System.currentTimeMillis() - 1000

                datePicker.show()
            }


            // --- Time Picker ---
            dialogueTrains.containerContact.setOnClickListener {
                val calendar = Calendar.getInstance()
                val timePicker = TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        // Format time: HH:mm (24hr format)
                        val formattedTime = String.format("%02d:%02d", hourOfDay, minute)
                        dialogueTrains.etContact.text = formattedTime
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true // 24-hour format
                )
                timePicker.show()
            }

            // OK button
            dialogueTrains.btnAdd.setOnClickListener {
                Handler(Looper.getMainLooper()).postDelayed({
                    val date = dialogueTrains.etCustomerName.text.toString().trim()
                    val time = dialogueTrains.etContact.text.toString().trim()
                    val amount = dialogueTrains.etEmail.text.toString().trim()

                    val validationResult =
                        expensesViewModel.validateBusTrainExpenseInput(date, time, amount)

                    if (validationResult.first) {

                        // Add item to list
                        busTimingExpenseList.add(
                            BusTrainExpense(amount, date, time)
                        )

                        // Notify adapter & scroll
                        allBusesTrainExpenseListAdapter.notifyItemInserted(
                            busTimingExpenseList.size - 1
                        )
                        binding.rvBusTrain.scrollToPosition(busTimingExpenseList.size - 1)
                        toggleEmptyStateBusTime()
                        alertDialog.dismiss()
                        activeDialog = null
                    } else {
                        val message = getString(validationResult.second)
                        showErrorPopup(requireContext(), "", message)
                    }
                }, 300)
            }

            // Set the view and show dialog
            alertDialog.setView(dialogueTrains.root)
            alertDialog.setCancelable(false)
            alertDialog.show()

        } catch (e: Exception) {
            Log.e("CustomDialog", "Error showing dialog: $e")
        }
    }


    fun addLodgingExpenses() {
        try {
            activeDialog?.dismiss()

            val dialogueLodge = DialogAddLodgingExpenseBinding.inflate(LayoutInflater.from(context))
            val alertDialog = AlertDialog.Builder(context).create()
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            activeDialog = alertDialog

            val calendar = Calendar.getInstance()

            // Pick From Date
            dialogueLodge.layoutSelectLocation.setOnClickListener {
                val datePicker = DatePickerDialog(
                    requireContext(),
                    { _, year, month, dayOfMonth ->
                        calendar.set(year, month, dayOfMonth)
                        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                        dialogueLodge.etFrom.text = sdf.format(calendar.time)
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
                datePicker.datePicker.minDate = System.currentTimeMillis() - 1000 // no past dates
                datePicker.show()
            }

            // Pick To Date
            dialogueLodge.layoutSecondName.setOnClickListener {
                val datePicker = DatePickerDialog(
                    requireContext(),
                    { _, year, month, dayOfMonth ->
                        calendar.set(year, month, dayOfMonth)
                        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                        dialogueLodge.etSecondName.text = sdf.format(calendar.time)
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
                datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
                datePicker.show()
            }

            // Cancel Button
            dialogueLodge.ivCancel.setOnClickListener {
                alertDialog.dismiss()
                activeDialog = null
            }

            // Add Button with validation
            dialogueLodge.btnAdd.setOnClickListener {
                val fromDate = dialogueLodge.etFrom.text.toString().trim()
                val toDate = dialogueLodge.etSecondName.text.toString().trim()
                val nights = dialogueLodge.etPumpLocation.text.toString().trim()
                val nightAmount = dialogueLodge.etFilledFuel.text.toString().trim()
                val amount = dialogueLodge.etAmount.text.toString().trim()

                val validation =
                    expensesViewModel.validateLodgingInput(fromDate, toDate, nights,nightAmount, amount)
                if (!validation.first) {
                    showErrorPopup(requireContext(), "", getString(validation.second))
                    return@setOnClickListener
                }

                // Add item to list
                lodgingExpenseList.add(
                    LodgingBoardingExpense(fromDate,nights,nightAmount,toDate,amount)
                )

                // Notify adapter & scroll
                allLodgingExpenseListAdapter.notifyItemInserted(
                    lodgingExpenseList.size - 1
                )
                binding.rvLoging.scrollToPosition(lodgingExpenseList.size - 1)
                toggleEmptyStateLodging()
                alertDialog.dismiss()
                activeDialog = null
            }

            alertDialog.setView(dialogueLodge.root)
            alertDialog.setCancelable(false)
            alertDialog.show()

        } catch (e: Exception) {
            Log.e("CustomDialog", "Error showing dialog: $e")
        }
    }


    fun addMiscellaneousExpenses() {
        try {
            activeDialog?.dismiss()  // Dismiss any existing dialog before showing a new one

            // Inflate layout using ViewBinding
            val dialogueMis =
                DialogAddMiscellaneousExpenseBinding.inflate(LayoutInflater.from(context))

            val alertDialog = AlertDialog.Builder(context).create()
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            // Store reference to active dialog
            activeDialog = alertDialog


            // OK button with slight delay before invoking the callback
            dialogueMis.ivCancel.setOnClickListener {
                alertDialog.dismiss()
                activeDialog = null
            }

            dialogueMis.btnAdd.setOnClickListener {
                Handler(Looper.getMainLooper()).postDelayed({
                    val from = dialogueMis.etCustomerName.text.toString().trim()
                    val to = dialogueMis.etDescription.text.toString().trim()
                    val amount = dialogueMis.etEmail.text.toString().trim()

                    val validationResult =
                        expensesViewModel.validateMiscellaneousInput(from, to, amount)

                    if (validationResult.first) {
                        val amountValue = amount
                        if (amountValue == null || amountValue <= "0.0") {
                            showErrorPopup(requireContext(), "", "Please enter a valid amount")
                            return@postDelayed
                        }

                        // Add item to list
                        miscellaneousListExpense.add(
                            MiscellaneousExpense(amountValue.toString(), to, from)
                        )

                        // Notify adapter & scroll
                        allMiscellaneousExpenseListAdapter.notifyItemInserted(
                            miscellaneousListExpense.size - 1
                        )
                        binding.rvMiscellaneous.scrollToPosition(miscellaneousListExpense.size - 1)
                        toggleEmptyStateMiscellaneous()
                        alertDialog.dismiss()
                        activeDialog = null
                    } else {
                        val message = getString(validationResult.second)
                        showErrorPopup(requireContext(), "", message)
                    }
                }, 300)
            }

            // Set the view and show dialog
            alertDialog.setView(dialogueMis.root)
            alertDialog.setCancelable(false)
            alertDialog.show()

        } catch (e: Exception) {
            Log.e("CustomDialog", "Error showing dialog: $e")
        }
    }
}