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
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crymzee.spenomatic.R
import com.crymzee.spenomatic.adapter.AddServiceDropDownAdapter
import com.crymzee.spenomatic.adapter.AllAllowanceExpenseListAdapter
import com.crymzee.spenomatic.adapter.AllBusesTrainExpenseListAdapter
import com.crymzee.spenomatic.adapter.AllLodgingExpenseListAdapter
import com.crymzee.spenomatic.adapter.AllMiscellaneousExpenseListAdapter
import com.crymzee.spenomatic.adapter.AllTransportExpenseListAdapter
import com.crymzee.spenomatic.adapter.DropDownVisitAdapterClient
import com.crymzee.spenomatic.adapter.OutCityCustomerVisitListAdapter
import com.crymzee.spenomatic.base.BaseFragment
import com.crymzee.spenomatic.databinding.DialogAddAllowanceExpenseBinding
import com.crymzee.spenomatic.databinding.DialogAddBusTrianExpenseBinding
import com.crymzee.spenomatic.databinding.DialogAddLodgingExpenseBinding
import com.crymzee.spenomatic.databinding.DialogAddMiscellaneousExpenseBinding
import com.crymzee.spenomatic.databinding.DialogAddTransportExpenseBinding
import com.crymzee.spenomatic.databinding.DialogAddUserBinding
import com.crymzee.spenomatic.databinding.FragmentAddOutCityVisitBinding
import com.crymzee.spenomatic.model.DropDownClientType
import com.crymzee.spenomatic.model.request.VisitModelRequest
import com.crymzee.spenomatic.model.request.createLocalExpense.MiscellaneousExpense
import com.crymzee.spenomatic.model.request.createLocalExpense.TransportExpense
import com.crymzee.spenomatic.model.request.createOutsideExpense.BusTrainExpense
import com.crymzee.spenomatic.model.request.createOutsideExpense.CreateOutsideExpenseRequest
import com.crymzee.spenomatic.model.request.createOutsideExpense.LodgingBoardingExpense
import com.crymzee.spenomatic.model.request.createOutsideExpense.TravelAllowance
import com.crymzee.spenomatic.model.request.createOutsideExpense.Visit
import com.crymzee.spenomatic.model.request.pendingVisits.Data
import com.crymzee.spenomatic.state.Resource
import com.crymzee.spenomatic.utils.SpenoMaticLogger
import com.crymzee.spenomatic.utils.confirmationPopUp
import com.crymzee.spenomatic.utils.extractFirstErrorMessage
import com.crymzee.spenomatic.utils.goBack
import com.crymzee.spenomatic.utils.hide
import com.crymzee.spenomatic.utils.showErrorPopup
import com.crymzee.spenomatic.utils.showSuccessPopup
import com.crymzee.spenomatic.utils.visible
import com.crymzee.spenomatic.viewModel.CustomersViewModel
import com.crymzee.spenomatic.viewModel.ExpensesViewModel
import com.example.flowit.abstracts.PaginationScrollListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddOutCityVisitFragment : BaseFragment() {
    private lateinit var binding: FragmentAddOutCityVisitBinding
    private lateinit var addServiceDropDownAdapter: AddServiceDropDownAdapter

    private lateinit var dropDownAdapterClient: DropDownVisitAdapterClient
    private lateinit var allMiscellaneousExpenseListAdapter: AllMiscellaneousExpenseListAdapter
    private lateinit var allTransportExpenseListAdapter: AllTransportExpenseListAdapter
    private lateinit var allAllowanceExpenseListAdapter: AllAllowanceExpenseListAdapter
    private lateinit var allBusesTrainExpenseListAdapter: AllBusesTrainExpenseListAdapter
    private lateinit var allLodgingExpenseListAdapter: AllLodgingExpenseListAdapter
    private lateinit var outCityCustomerVisitListAdapter: OutCityCustomerVisitListAdapter
    private var activeDialog: AlertDialog? = null  // Keep track of the active dialog
    val userList: MutableList<VisitModelRequest> = mutableListOf()
    val miscellaneousListExpense: MutableList<MiscellaneousExpense> = mutableListOf()
    val transportExpenseList: MutableList<TransportExpense> = mutableListOf()
    val lodgingExpenseList: MutableList<LodgingBoardingExpense> = mutableListOf()
    val busTimingExpenseList: MutableList<BusTrainExpense> = mutableListOf()
    val allowanceExpenseList: MutableList<TravelAllowance> = mutableListOf()
    private var dialogueAddUser: DialogAddUserBinding? = null
    private val customersViewModel: CustomersViewModel by activityViewModels()
    private val expensesViewModel: ExpensesViewModel by activityViewModels()
    private var fromDate: String? = null
    private var toDate: String? = null


    private var objective = ""
    private var visitData: Data? = null
    private val visitMap = mutableMapOf<Int, Visit>()
    private var isLastPage: Boolean = false
    private var isLoading: Boolean = false
    private val layoutManager by lazy { getLinearLayoutManager() }
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
        toggleEmptyStateBusTime()
        toggleEmptyStateLodging()
        toggleEmptyStateAllowance()

        binding.apply {
            ivBack.setOnClickListener { goBack() }
            ivAddTransport.setOnClickListener { addTransportExpenses() }
            ivAddBusTrain.setOnClickListener { addBusTrainExpenses() }
            ivAddAllowance.setOnClickListener { addAllowanceExpenses() }
            ivAddLodging.setOnClickListener { addLodgingExpenses() }
            ivAddMiscellaneous.setOnClickListener { addMiscellaneousExpenses() }

            binding.btnSave.setOnClickListener {
                // :white_check_mark: :one: Always persist the current visit data before proceeding
                visitData?.id?.let { currentId ->
                    if (
                        transportExpenseList.isNotEmpty() ||
                        miscellaneousListExpense.isNotEmpty() ||
                        allowanceExpenseList.isNotEmpty() ||
                        lodgingExpenseList.isNotEmpty() ||
                        busTimingExpenseList.isNotEmpty() ||
                        !objective.isNullOrEmpty()
                    ) {
                        visitMap[currentId] = Visit(
                            objective = objective,
                            transport_expenses = transportExpenseList.toList(),
                            miscellaneous_expenses = miscellaneousListExpense.toList(),
                            bus_train_expenses = busTimingExpenseList.toList(),
                            lodging_boarding_expenses = lodgingExpenseList.toList(),
                            travel_allowances = allowanceExpenseList.toList(),
                            visit = currentId
                        )
                    } else {
                        visitMap.remove(currentId) // remove if empty
                    }
                }

                // :white_check_mark: :two: Check if objective exists
                if (outCityCustomerVisitListAdapter.itemCount == 0) {
                    showErrorPopup(requireContext(), "", "No pending visit  yet")
                    return@setOnClickListener
                }
                // :white_check_mark: :two: Check if objective exists
                if (objective.isEmpty()) {
                    showErrorPopup(requireContext(), "", "Objective field must not be empty")
                    return@setOnClickListener
                }

                // :white_check_mark: :three: Ensure at least one visit exists
                if (visitMap.isEmpty()) {
                    showErrorPopup(requireContext(), "", "Please add at least one visit before submitting")
                    return@setOnClickListener
                }

                // :white_check_mark: :four: Validate each visit — check if any required list is null or empty
                val incompleteVisit = visitMap.values.find { visit ->
                    visit.transport_expenses.isEmpty() ||
                            visit.bus_train_expenses.isNullOrEmpty() ||
                            visit.travel_allowances.isNullOrEmpty() ||
                            visit.lodging_boarding_expenses.isNullOrEmpty() ||
                            visit.miscellaneous_expenses.isEmpty()
                }

                if (incompleteVisit != null) {
                    when {
                        incompleteVisit.transport_expenses.isEmpty() ->
                            showErrorPopup(requireContext(), "", "Please add transport expense.")
                        incompleteVisit.lodging_boarding_expenses.isNullOrEmpty() ->
                            showErrorPopup(requireContext(), "", "Please add lodging & boarding expense.")
                        incompleteVisit.bus_train_expenses.isNullOrEmpty() ->
                            showErrorPopup(requireContext(), "", "Please add bus/train expense.")
                        incompleteVisit.travel_allowances.isNullOrEmpty() ->
                            showErrorPopup(requireContext(), "", "Please add allowances expense.")
                        incompleteVisit.miscellaneous_expenses.isEmpty() ->
                            showErrorPopup(requireContext(), "", "Please add miscellaneous expense.")
                    }
                    return@setOnClickListener
                }

                // :white_check_mark: :five: Double-check all visits complete
                val allFilled = visitMap.values.all { visit ->
                    visit.transport_expenses.isNotEmpty() &&
                            !visit.bus_train_expenses.isNullOrEmpty() &&
                            !visit.travel_allowances.isNullOrEmpty() &&
                            !visit.lodging_boarding_expenses.isNullOrEmpty() &&
                            visit.miscellaneous_expenses.isNotEmpty()
                }

                if (!allFilled) {
                    showErrorPopup(requireContext(), "", "Please make sure all visits have complete their expenses detail")
                    return@setOnClickListener
                }

                // :white_check_mark: :six: Prepare final list with objective set
                val validVisits = visitMap.values.map { visit ->
                    visit.copy(objective = objective)
                }

                if (validVisits.isEmpty()) {
                    showErrorPopup(requireContext(), "", "Please fill at least one complete visit before submitting")
                    return@setOnClickListener
                }

                // :white_check_mark: :seven: Submit final request
                val requestBody = CreateOutsideExpenseRequest(
                    description = "Outstation sales trip to meet clients",
                    type = "outstation_sales",
                    visits = validVisits
                )

                Log.d("DEBUG_EXPENSE", "Submitting ${validVisits.size} visits → $requestBody")
                addOutStationExpense(requestBody)
            }



        }
    }

    private fun adjustRecyclerHeight() {
        val params = binding.rvCustomers.layoutParams
        params.height = if (outCityCustomerVisitListAdapter.itemCount > 3) {
            resources.getDimensionPixelSize(R.dimen._160sdp) // use 160sdp from dimens
        } else {
            ViewGroup.LayoutParams.WRAP_CONTENT
        }
        binding.rvCustomers.layoutParams = params
    }

    private fun addOutStationExpense(model: CreateOutsideExpenseRequest) {
        expensesViewModel.createOutsideExpenses(model)
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
                                navigateClear(R.id.action_addOutCityVisitFragment_to_expensesFragment)
                            })
                    }

                    is Resource.Loading<*> -> {}
                }
            }
    }

    private fun setupAdapters() {
        outCityCustomerVisitListAdapter = OutCityCustomerVisitListAdapter(requireContext())
        binding.rvCustomers.apply {
            adapter = outCityCustomerVisitListAdapter
            layoutManager = this@AddOutCityVisitFragment.layoutManager
            addOnScrollListener(object :
                PaginationScrollListener(this@AddOutCityVisitFragment.layoutManager, 10) {
                override fun isLastPage(): Boolean = this@AddOutCityVisitFragment.isLastPage
                override fun isLoading(): Boolean = this@AddOutCityVisitFragment.isLoading

                override fun loadMoreItems() {
                    if (!isLastPage && !isLoading) {
                        isLoading = true
//                        visitViewModel.getAllVisit( visitViewModel.selectedTab)
                    }
                }
            })
        }

        outCityCustomerVisitListAdapter.setOnItemClick { selectedVisit ->
            val previousId = visitData?.id

            // ✅ 1. Save previous visit data if exists
            previousId?.let { prevId ->
                if (
                    transportExpenseList.isNotEmpty() ||
                    miscellaneousListExpense.isNotEmpty() ||
                    allowanceExpenseList.isNotEmpty() ||
                    lodgingExpenseList.isNotEmpty() ||
                    busTimingExpenseList.isNotEmpty() ||
                    !objective.isNullOrEmpty()
                ) {
                    visitMap[prevId] = Visit(
                        objective = objective,
                        transport_expenses = transportExpenseList.toList(),
                        miscellaneous_expenses = miscellaneousListExpense.toList(),
                        bus_train_expenses = busTimingExpenseList.toList(),
                        lodging_boarding_expenses = lodgingExpenseList.toList(),
                        travel_allowances = allowanceExpenseList.toList(),
                        visit = prevId
                    )
                } else {
                    visitMap.remove(prevId)
                }
            }

            // ✅ 2. Switch to new visit
            visitData = selectedVisit

            // ✅ 3. Clear all UI lists
            transportExpenseList.clear()
            miscellaneousListExpense.clear()
            allowanceExpenseList.clear()
            lodgingExpenseList.clear()
            busTimingExpenseList.clear()

            // ✅ 4. Load existing saved data (if available)
            visitMap[selectedVisit.id]?.let { saved ->
                objective = saved.objective
                transportExpenseList.addAll(saved.transport_expenses)
                miscellaneousListExpense.addAll(saved.miscellaneous_expenses)
                busTimingExpenseList.addAll(saved.bus_train_expenses ?: emptyList())
                allowanceExpenseList.addAll(saved.travel_allowances ?: emptyList())
                lodgingExpenseList.addAll(saved.lodging_boarding_expenses ?: emptyList())
            } ?: run {
                objective = ""
            }

            // ✅ 5. Notify all adapters
            allTransportExpenseListAdapter.notifyDataSetChanged()
            allMiscellaneousExpenseListAdapter.notifyDataSetChanged()
            allBusesTrainExpenseListAdapter.notifyDataSetChanged()
            allLodgingExpenseListAdapter.notifyDataSetChanged()
            allAllowanceExpenseListAdapter.notifyDataSetChanged()

            toggleEmptyStateMiscellaneous()
            toggleEmptyTransportState()
            toggleEmptyStateBusTime()
            toggleEmptyStateLodging()
            toggleEmptyStateAllowance()

            // ✅ 6. Update adapter selection + objective
            outCityCustomerVisitListAdapter.setSelectedVisit(selectedVisit.id, objective)
        }


        outCityCustomerVisitListAdapter.getTypeObject { typedObjective ->
            objective = typedObjective
            visitData?.id?.let { currentId ->
                visitMap[currentId]?.let { visit ->
                    visitMap[currentId] = visit.copy(objective = typedObjective)
                }
            }
        }

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
                    visitData?.id?.let { currentId ->
                        visitMap[currentId] = Visit(
                            objective = objective,
                            transport_expenses = transportExpenseList.toList(),
                            miscellaneous_expenses = miscellaneousListExpense.toList(),
                            visit = currentId,
                            bus_train_expenses = busTimingExpenseList,
                            lodging_boarding_expenses = lodgingExpenseList,
                            travel_allowances = allowanceExpenseList

                        )
                    }
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
                    visitData?.id?.let { currentId ->
                        visitMap[currentId] = Visit(
                            objective = objective,
                            transport_expenses = transportExpenseList.toList(),
                            miscellaneous_expenses = miscellaneousListExpense.toList(),
                            visit = currentId,
                            bus_train_expenses = busTimingExpenseList,
                            lodging_boarding_expenses = lodgingExpenseList,
                            travel_allowances = allowanceExpenseList

                        )
                    }
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
                    visitData?.id?.let { currentId ->
                        visitMap[currentId] = Visit(
                            objective = objective,
                            transport_expenses = transportExpenseList.toList(),
                            miscellaneous_expenses = miscellaneousListExpense.toList(),
                            visit = currentId,
                            bus_train_expenses = busTimingExpenseList,
                            lodging_boarding_expenses = lodgingExpenseList,
                            travel_allowances = allowanceExpenseList

                        )
                    }
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
                    visitData?.id?.let { currentId ->
                        visitMap[currentId] = Visit(
                            objective = objective,
                            transport_expenses = transportExpenseList.toList(),
                            miscellaneous_expenses = miscellaneousListExpense.toList(),
                            visit = currentId,
                            bus_train_expenses = busTimingExpenseList,
                            lodging_boarding_expenses = lodgingExpenseList,
                            travel_allowances = allowanceExpenseList

                        )
                    }
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
                    visitData?.id?.let { currentId ->
                        visitMap[currentId] = Visit(
                            objective = objective,
                            transport_expenses = transportExpenseList.toList(),
                            miscellaneous_expenses = miscellaneousListExpense.toList(),
                            visit = currentId,
                            bus_train_expenses = busTimingExpenseList,
                            lodging_boarding_expenses = lodgingExpenseList,
                            travel_allowances = allowanceExpenseList

                        )
                    }
                    toggleEmptyStateBusTime()
                }
            )
        }
        binding.rvBusTrain.adapter = allBusesTrainExpenseListAdapter


    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchPaginatedData()
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
            // OK button with slight delay before invoking the callback
            dialogueAllowance.layoutSelectLocation.setOnClickListener {
                selectType(dialogueAllowance)
            }

            dialogueAllowance.btnAdd.setOnClickListener {
                Handler(Looper.getMainLooper()).postDelayed({
                    val type = dialogueAllowance.tvLocation.text.toString().trim().toLowerCase()
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

    private fun selectType(dialogueAllowance: DialogAddAllowanceExpenseBinding) {
        try {
            val itemList = mutableListOf<DropDownClientType>().apply {
                add(DropDownClientType("Day", "1"))
                add(DropDownClientType("Night", "2"))
            }

            dialogueAllowance.ivDropDownGender.rotation = 180f

            // Ensure the anchor view is measured before using its width
            dialogueAllowance.layoutSelectGender.post {
                val anchorView = dialogueAllowance.layoutSelectGender
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
                        dialogueAllowance.ivDropDownGender.rotation = 0f
                    }
                }

                // RecyclerView setup
                val rvItems: RecyclerView = dialogView.findViewById(R.id.rv_year)
                addServiceDropDownAdapter = AddServiceDropDownAdapter(requireContext())
                rvItems.layoutManager = LinearLayoutManager(requireContext())
                rvItems.adapter = addServiceDropDownAdapter
                addServiceDropDownAdapter.addAll(itemList)

                // Handle item selection
                addServiceDropDownAdapter.getClientType { selected ->
                    dialogueAllowance.tvLocation.text = selected
                    popUp.dismiss()
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun fetchPaginatedData() {
        customersViewModel.getAllPendingVisits()
        customersViewModel.getAllPendingVisitLiveData.removeObservers(viewLifecycleOwner)
        customersViewModel.getAllPendingVisitLiveData.observe(viewLifecycleOwner) { response ->

            when (response) {
                is Resource.Error -> {
                    val errorMessage = extractFirstErrorMessage(response.throwable)
                    SpenoMaticLogger.logErrorMsg("Error", errorMessage.description)
                }

                is Resource.Loading -> {
                    // you can show loader here if needed
                }

                is Resource.Success -> {
                    response.data?.let { posts ->
                        val isFirstPage = expensesViewModel.page == 1
                        val isEmptyList = posts.data.isEmpty() && isFirstPage

                        if(isEmptyList){
                            binding.labelNoPendingVisit.visible()
                            binding.rvCustomers.hide()
                        }else{
                            binding.labelNoPendingVisit.hide()
                            binding.rvCustomers.visible()
                        }
                        if (isFirstPage) {
                            // ✅ Use new setData (auto-selects first item)
                            outCityCustomerVisitListAdapter.setData(posts.data)
                            adjustRecyclerHeight()
                            if (posts.data.isNotEmpty()) {
                                visitData = outCityCustomerVisitListAdapter.getSelectedItem()

                                // preload saved expenses if any
                                transportExpenseList.clear()
                                miscellaneousListExpense.clear()
                                allowanceExpenseList.clear()
                                lodgingExpenseList.clear()
                                busTimingExpenseList.clear()
                                visitData?.id?.let { id ->
                                    visitMap[id]?.let { saved ->
                                        transportExpenseList.addAll(saved.transport_expenses)
                                        miscellaneousListExpense.addAll(saved.miscellaneous_expenses)
                                        busTimingExpenseList.addAll(saved.bus_train_expenses ?: emptyList())
                                        allowanceExpenseList.addAll(saved.travel_allowances ?: emptyList())
                                        lodgingExpenseList.addAll(saved.lodging_boarding_expenses ?: emptyList())
                                    }
                                }

                                allTransportExpenseListAdapter.notifyDataSetChanged()
                                allMiscellaneousExpenseListAdapter.notifyDataSetChanged()
                                toggleEmptyStateMiscellaneous()
                                toggleEmptyTransportState()
                                toggleEmptyStateBusTime()
                                toggleEmptyStateLodging()
                                toggleEmptyStateAllowance()
                            }
                        } else {
                            // ✅ Append unique items for pagination
                            outCityCustomerVisitListAdapter.addPaginatedData(posts.data)
                            adjustRecyclerHeight()
                        }

                        val hasNextPage = !posts.pagination.links.next.isNullOrEmpty()
                        isLastPage = !hasNextPage
                        isLoading = false

                        if (hasNextPage) {
                            expensesViewModel.page++
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
            var fromDateCalendar: Calendar? = null // store selected from-date

            // Pick From Date
            dialogueLodge.etFrom.setOnClickListener {
                val datePicker = DatePickerDialog(
                    requireContext(),
                    { _, year, month, dayOfMonth ->
                        calendar.set(year, month, dayOfMonth)

                        fromDateCalendar = Calendar.getInstance().apply {
                            set(year, month, dayOfMonth)
                        }

                        val displayFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                        dialogueLodge.etFrom.text = displayFormat.format(calendar.time)

                        val apiFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        fromDate = apiFormat.format(calendar.time)

                        // Reset "To Date" when From Date changes
                        dialogueLodge.etSecondName.text = ""
                        toDate = null
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
                datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
                datePicker.show()
            }

            // Pick To Date
            dialogueLodge.etSecondName.setOnClickListener {
                if (fromDateCalendar == null) {
                    showErrorPopup(requireContext(), "", "Please select From Date first")
                    return@setOnClickListener
                }

                val toCalendar = Calendar.getInstance()
                val datePicker = DatePickerDialog(
                    requireContext(),
                    { _, year, month, dayOfMonth ->
                        toCalendar.set(year, month, dayOfMonth)

                        val displayFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                        dialogueLodge.etSecondName.text = displayFormat.format(toCalendar.time)

                        val apiFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        toDate = apiFormat.format(toCalendar.time)
                    },
                    toCalendar.get(Calendar.YEAR),
                    toCalendar.get(Calendar.MONTH),
                    toCalendar.get(Calendar.DAY_OF_MONTH)
                )

                // ✅ Restrict "To Date" to be >= selected "From Date"
                datePicker.datePicker.minDate = fromDateCalendar!!.timeInMillis

                datePicker.show()
            }

            // Cancel Button
            dialogueLodge.ivCancel.setOnClickListener {
                alertDialog.dismiss()
                activeDialog = null
            }

            // Add Button (same as before)
            dialogueLodge.btnAdd.setOnClickListener {
                val nights = dialogueLodge.etPumpLocation.text.toString().trim()
                val nightAmount = dialogueLodge.etFilledFuel.text.toString().trim()
                val amount = dialogueLodge.etAmount.text.toString().trim()

                val validation = expensesViewModel.validateLodgingInput(
                    fromDate ?: "",
                    toDate ?: "",
                    nights,
                    nightAmount,
                    amount
                )
                if (!validation.first) {
                    showErrorPopup(requireContext(), "", getString(validation.second))
                    return@setOnClickListener
                }

                lodgingExpenseList.add(
                    LodgingBoardingExpense(
                        fromDate ?: "",
                        nights,
                        nightAmount,
                        toDate ?: "",
                        amount
                    )
                )

                allLodgingExpenseListAdapter.notifyItemInserted(lodgingExpenseList.size - 1)
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