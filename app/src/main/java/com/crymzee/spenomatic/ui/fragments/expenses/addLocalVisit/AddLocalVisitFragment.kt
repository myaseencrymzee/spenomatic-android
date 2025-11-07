package com.crymzee.spenomatic.ui.fragments.expenses.addLocalVisit

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.crymzee.spenomatic.R
import com.crymzee.spenomatic.adapter.AllMiscellaneousExpenseListAdapter
import com.crymzee.spenomatic.adapter.AllTransportExpenseListAdapter
import com.crymzee.spenomatic.adapter.CustomerVisitListAdapter
import com.crymzee.spenomatic.adapter.DropDownVisitAdapterClient
import com.crymzee.spenomatic.base.BaseFragment
import com.crymzee.spenomatic.databinding.DialogAddMiscellaneousExpenseBinding
import com.crymzee.spenomatic.databinding.DialogAddTransportExpenseBinding
import com.crymzee.spenomatic.databinding.FragmentAddLocalVisitBinding
import com.crymzee.spenomatic.model.request.createLocalExpense.CreateLocalExpenseRequest
import com.crymzee.spenomatic.model.request.createLocalExpense.MiscellaneousExpense
import com.crymzee.spenomatic.model.request.createLocalExpense.TransportExpense
import com.crymzee.spenomatic.model.request.createLocalExpense.Visit
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

class AddLocalVisitFragment : BaseFragment() {
    private lateinit var binding: FragmentAddLocalVisitBinding
    private lateinit var dropDownAdapterClient: DropDownVisitAdapterClient
    private lateinit var customerListAdapter: CustomerVisitListAdapter
    private lateinit var allMiscellaneousExpenseListAdapter: AllMiscellaneousExpenseListAdapter
    private lateinit var allTransportExpenseListAdapter: AllTransportExpenseListAdapter
    private var activeDialog: AlertDialog? = null  // Keep track of the active dialog
    val localVisit: MutableList<Visit> = mutableListOf()
    val miscellaneousListExpense: MutableList<MiscellaneousExpense> = mutableListOf()
    val transportExpenseList: MutableList<TransportExpense> = mutableListOf()
    private val customersViewModel: CustomersViewModel by activityViewModels()
    private val expensesViewModel: ExpensesViewModel by activityViewModels()
    private var isLastPage: Boolean = false
    private var isLoading: Boolean = false
    private val layoutManager by lazy { getLinearLayoutManager() }
    var currentPage = 1
    var perPage = 10

    // Replace this in your fragment
    private var visitData: Data? = null
    private val visitMap = mutableMapOf<Int, Visit>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        if (!::binding.isInitialized) {
            binding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_add_local_visit,
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
        toggleEmptyState()
        toggleEmptyState1()
        binding.apply {
//            layoutSelectLocation.setOnClickListener {
//                selectCategory()
//            }
            btnSave.setOnClickListener {
                hideKeyboard()
                // ✅ Always update map for the last selected visit
                visitData?.id?.let { currentId ->
                    if (transportExpenseList.isNotEmpty() || miscellaneousListExpense.isNotEmpty()) {
                        visitMap[currentId] = Visit(
                            objective = "",
                            transport_expenses = transportExpenseList.toList(),
                            miscellaneous_expenses = miscellaneousListExpense.toList(),
                            visit = currentId
                        )
                    } else {
                        visitMap.remove(currentId) // ❌ ensure we drop empty visits
                    }
                }

                if (customerListAdapter.itemCount == 0) {
                    showErrorPopup(requireContext(), "", "No pending client visited yet ")
                    return@setOnClickListener
                }
                if (visitMap.isEmpty()) {
                    showErrorPopup(
                        requireContext(),
                        "Missing Expenses",
                        "Please add at least one Transport and one Miscellaneous expense for each selected visit."
                    )
                    return@setOnClickListener
                }

                // ✅ Double-check no empty visits remain
                val invalidVisit = visitMap.values.find { visit ->
                    visit.transport_expenses.isEmpty() || visit.miscellaneous_expenses.isEmpty()
                }
                if (invalidVisit != null) {
                    showErrorPopup(
                        requireContext(),
                        "Missing Expenses",
                        "Please add at least one Transport and one Miscellaneous expense for each selected visit."
                    )
                    return@setOnClickListener
                }

                val requestBody = CreateLocalExpenseRequest(
                    type = "local_visit",
                    description = "local sales trip",
                    visits = visitMap.values.toList()
                )
                addLocalExpense(requestBody)
            }

            ivBack.setOnClickListener { goBack() }
            ivAddTransport.setOnClickListener { addTransportExpenses() }
            ivAddMiscellaneous.setOnClickListener { addMiscellaneousExpenses() }
        }
    }
    private fun adjustRecyclerHeight() {
        val params = binding.rvCustomers.layoutParams
        params.height = if (customerListAdapter.itemCount > 3) {
            resources.getDimensionPixelSize(R.dimen._160sdp) // use 160sdp from dimens
        } else {
            ViewGroup.LayoutParams.WRAP_CONTENT
        }
        binding.rvCustomers.layoutParams = params
    }

    private fun toggleEmptyState() {
        if (transportExpenseList.isEmpty()) {
            binding.labelNoTransportData.visibility = View.VISIBLE
            binding.rvTransport.visibility = View.GONE
        } else {
            binding.labelNoTransportData.visibility = View.GONE
            binding.rvTransport.visibility = View.VISIBLE
        }
    }

    private fun toggleEmptyState1() {
        if (miscellaneousListExpense.isEmpty()) {
            binding.labelNoMiscellaneousData.visibility = View.VISIBLE
            binding.rvMiscellaneous.visibility = View.GONE
        } else {
            binding.labelNoMiscellaneousData.visibility = View.GONE
            binding.rvMiscellaneous.visibility = View.VISIBLE
        }
    }


    private fun addLocalExpense(model: CreateLocalExpenseRequest) {
        expensesViewModel.createLocalExpenses(model)
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
                                customerListAdapter.setData(emptyList())
                                customerListAdapter.notifyDataSetChanged()
                                navigateClear(R.id.action_addLocalVisitFragment_to_expensesFragment)
                            })
                    }

                    is Resource.Loading<*> -> {}
                }
            }
    }

    private fun setupAdapters() {
        customerListAdapter = CustomerVisitListAdapter(requireContext())
        binding.rvCustomers.apply {
            adapter = customerListAdapter
            layoutManager = this@AddLocalVisitFragment.layoutManager
            addOnScrollListener(object :
                PaginationScrollListener(this@AddLocalVisitFragment.layoutManager, 10) {
                override fun isLastPage(): Boolean = this@AddLocalVisitFragment.isLastPage
                override fun isLoading(): Boolean = this@AddLocalVisitFragment.isLoading

                override fun loadMoreItems() {
                    if (!isLastPage && !isLoading) {
                        isLoading = true
//                        visitViewModel.getAllVisit( visitViewModel.selectedTab)
                    }
                }
            })
        }

        customerListAdapter.setOnItemClick { item ->
            val currentId = visitData?.id

            // ✅ Always update map when leaving a visit
            if (currentId != null) {
                if (transportExpenseList.isNotEmpty() || miscellaneousListExpense.isNotEmpty()) {
                    visitMap[currentId] = Visit(
                        objective = "",
                        transport_expenses = transportExpenseList.toList(),
                        miscellaneous_expenses = miscellaneousListExpense.toList(),
                        visit = currentId
                    )
                } else {
                    // ❌ remove if empty
                    visitMap.remove(currentId)
                }
            }

            // ✅ Switch selection
            visitData = item

            // ✅ Load saved data if exists, otherwise start fresh
            transportExpenseList.clear()
            miscellaneousListExpense.clear()

            visitMap[item.id]?.let { savedVisit ->
                transportExpenseList.addAll(savedVisit.transport_expenses)
                miscellaneousListExpense.addAll(savedVisit.miscellaneous_expenses)
            }

            allTransportExpenseListAdapter.notifyDataSetChanged()
            allMiscellaneousExpenseListAdapter.notifyDataSetChanged()
            toggleEmptyState()
            toggleEmptyState1()
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

                    // ✅ Keep local list in sync
                    miscellaneousListExpense.removeIf { it.objective == visitId }

                    // ✅ Update visitMap for current customer
                    visitData?.id?.let { currentId ->
                        visitMap[currentId] = Visit(
                            objective = "",
                            transport_expenses = transportExpenseList.toList(),
                            miscellaneous_expenses = miscellaneousListExpense.toList(),
                            visit = currentId
                        )
                    }

                    toggleEmptyState1()
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

                    // ✅ Keep local list in sync
                    transportExpenseList.removeIf { it.from_location == visitId }

                    // ✅ Update visitMap for current customer
                    visitData?.id?.let { currentId ->
                        visitMap[currentId] = Visit(
                            objective = "",
                            transport_expenses = transportExpenseList.toList(),
                            miscellaneous_expenses = miscellaneousListExpense.toList(),
                            visit = currentId
                        )
                    }

                    toggleEmptyState()
                }
            )
        }

        binding.rvTransport.adapter = allTransportExpenseListAdapter

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchPaginatedData()
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
                            customerListAdapter.setData(posts.data)
                            adjustRecyclerHeight()
                            if (posts.data.isNotEmpty()) {
                                // Keep reference to first item
                                visitData = customerListAdapter.getSelectedItem()

                                // preload expenses if previously saved
                                transportExpenseList.clear()
                                miscellaneousListExpense.clear()
                                visitData?.id?.let { id ->
                                    visitMap[id]?.let { saved ->
                                        transportExpenseList.addAll(saved.transport_expenses)
                                        miscellaneousListExpense.addAll(saved.miscellaneous_expenses)
                                    }
                                }

                                allTransportExpenseListAdapter.notifyDataSetChanged()
                                allMiscellaneousExpenseListAdapter.notifyDataSetChanged()
                                toggleEmptyState()
                                toggleEmptyState1()
                            }
                        } else {
                            // ✅ Append unique items
                            customerListAdapter.addPaginatedData(posts.data)
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

                null -> {}
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        customersViewModel.clearLiveData()

    }



    fun addTransportExpenses() {
        try {
            // Dismiss any existing dialog before showing a new one
            activeDialog?.dismiss()

            // Inflate layout using ViewBinding
            val dialogueTrans =
                DialogAddTransportExpenseBinding.inflate(LayoutInflater.from(context))

            val alertDialog = AlertDialog.Builder(context).create()
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            // Store reference to active dialog
            activeDialog = alertDialog

            // Cancel button
            dialogueTrans.ivCancel.setOnClickListener {
                alertDialog.dismiss()
                activeDialog = null
            }

            // Add button
            dialogueTrans.btnAdd.setOnClickListener {
                Handler(Looper.getMainLooper()).postDelayed({
                    val from = dialogueTrans.etCustomerName.text.toString().trim()
                    val to = dialogueTrans.etContact.text.toString().trim()
                    val amount = dialogueTrans.etEmail.text.toString().trim()

                    val validationResult = expensesViewModel.validateUserInput(from, to, amount)

                    if (validationResult.first) {
                        val amountValue = amount
                        if (from == to) {
                            showErrorPopup(requireContext(), "", "From and To destinations cannot be the same.")
                            return@postDelayed
                        }else if (amountValue == null || amountValue <= "0.0") {
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
                        toggleEmptyState()
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
            Log.e("CustomDialog", "Error showing dialog: ${e.localizedMessage}")
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
                        toggleEmptyState1()
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