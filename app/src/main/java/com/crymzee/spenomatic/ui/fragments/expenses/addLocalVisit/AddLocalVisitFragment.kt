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
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crymzee.spenomatic.R
import com.crymzee.spenomatic.adapter.AllMiscellaneousExpenseListAdapter
import com.crymzee.spenomatic.adapter.AllTransportExpenseListAdapter
import com.crymzee.spenomatic.adapter.CustomerVisitListAdapter
import com.crymzee.spenomatic.adapter.DropDownVisitAdapterClient
import com.crymzee.spenomatic.base.BaseFragment
import com.crymzee.spenomatic.databinding.DialogAddMiscellaneousExpenseBinding
import com.crymzee.spenomatic.databinding.DialogAddTransportExpenseBinding
import com.crymzee.spenomatic.databinding.FragmentAddLocalVisitBinding
import com.crymzee.spenomatic.model.request.VisitModelRequest
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
import com.crymzee.spenomatic.utils.showErrorPopup
import com.crymzee.spenomatic.utils.showSuccessPopup
import com.crymzee.spenomatic.viewModel.CustomersViewModel
import com.crymzee.spenomatic.viewModel.ExpensesViewModel

class AddLocalVisitFragment : BaseFragment() {
    private lateinit var binding: FragmentAddLocalVisitBinding
    private lateinit var dropDownAdapterClient: DropDownVisitAdapterClient
    private lateinit var customerListAdapter: CustomerVisitListAdapter
    private lateinit var allMiscellaneousExpenseListAdapter: AllMiscellaneousExpenseListAdapter
    private lateinit var allTransportExpenseListAdapter: AllTransportExpenseListAdapter
    private var activeDialog: AlertDialog? = null  // Keep track of the active dialog
    val userList: MutableList<VisitModelRequest> = mutableListOf()
    val miscellaneousListExpense: MutableList<MiscellaneousExpense> = mutableListOf()
    val transportExpenseList: MutableList<TransportExpense> = mutableListOf()
    private val customersViewModel: CustomersViewModel by activityViewModels()
    private val expensesViewModel: ExpensesViewModel by activityViewModels()

    var currentPage = 1
    var perPage = 10
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
        dropDownAdapterClient = DropDownVisitAdapterClient(requireContext())
        fetchPaginatedData(currentPage, perPage)
        binding.apply {
            layoutSelectLocation.setOnClickListener {
                selectCategory()
            }
            btnSave.setOnClickListener {
                if (userList.isEmpty()) {
                    showErrorPopup(requireContext(), "", "Please select a client to visit")
                } else if (transportExpenseList.isEmpty()) {
                    showErrorPopup(requireContext(), "", "Please add transport expense")
                } else if (miscellaneousListExpense.isEmpty()) {
                    showErrorPopup(requireContext(), "", "Please add miscellaneous expense")
                } else {
                    val requestBody = CreateLocalExpenseRequest(
                        type = "local_visit",
                        description = "local sales trip",
                        visits = listOf(
                            Visit(
                                miscellaneous_expenses = miscellaneousListExpense,
                                objective = "",
                                transport_expenses = transportExpenseList,
                                visit = userList.firstOrNull()?.id ?: 0

                            )
                        )
                    )
                    addLocalExpense(requestBody)
                }
            }

            ivBack.setOnClickListener { goBack() }
            ivAddTransport.setOnClickListener { addTransportExpenses() }
            ivAddMiscellaneous.setOnClickListener { addMiscellaneousExpenses() }
        }
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
                                navigateClear(R.id.action_addLocalVisitFragment_to_expensesFragment)
                            })
                    }

                    is Resource.Loading<*> -> {}
                }
            }
    }

    private fun setupAdapters() {
        customerListAdapter = CustomerVisitListAdapter(userList) {

        }
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
                    userList.removeIf { it.id == visitId }
                    toggleEmptyState1()
                }
            )
        }
        binding.rvCustomers.adapter = customerListAdapter
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

                    // ✅ Keep ViewModel list in sync
                    transportExpenseList.removeIf { it.from_location == visitId }
                    toggleEmptyState()
                }
            )

        }
        binding.rvTransport.adapter = allTransportExpenseListAdapter

    }

    private fun selectCategory() {
        try {
            // Function to load more data
            fun loadMoreData() {
                if (dropDownAdapterClient.itemCount >= 10) {
                    fetchPaginatedData(currentPage, perPage)
                }
            }

            binding.ivDropDownGender.rotation = 180f

            // Ensure anchor view is measured before using its width
            binding.layoutSelectLocation.post {
                val anchorView = binding.layoutSelectLocation
                val width = anchorView.width // exact width of the anchor view

                // Inflate popup layout
                val dialogView = View.inflate(requireContext(), R.layout.layout_drop_down_new, null)

                val popUp = PopupWindow(
                    dialogView,
                    width,  // same width as layoutSelectLocation
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

                // Find views inside popup
                val rvItems: RecyclerView = dialogView.findViewById(R.id.rv_year)
                val tvEmpty: TextView = dialogView.findViewById(R.id.tv_empty)

                rvItems.layoutManager = LinearLayoutManager(requireContext())
                rvItems.adapter = dropDownAdapterClient

                // Empty state check
                if (dropDownAdapterClient.itemCount == 0) {
                    rvItems.visibility = View.GONE
                    tvEmpty.visibility = View.VISIBLE
                } else {
                    rvItems.visibility = View.VISIBLE
                    tvEmpty.visibility = View.GONE
                    loadMoreData() // load first batch if list has items
                }

                // Pagination on scroll
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
                dropDownAdapterClient.getClientType { data ->
                    val exists = userList.any { it.id == data.id }

                    if (!exists) {
                        // Clear existing list
                        userList.clear()
                        customerListAdapter.notifyDataSetChanged()

                        val dataModel = VisitModelRequest(
                            id = data.id,
                            name = data.customer.fullname,
                            address = data.customer.address,
                            objective = "",
                            remark = data.visit_summary,
                            date = data.schedule_date,
                        )

                        // Add new item
                        userList.add(dataModel)
                        customerListAdapter.notifyItemInserted(0)
                        binding.rvCustomers.scrollToPosition(0)
                    }

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
        customersViewModel.getAllPendingVisits()
        customersViewModel.getAllPendingVisitLiveData.removeObservers(viewLifecycleOwner)
        customersViewModel.getAllPendingVisitLiveData.observe(viewLifecycleOwner) { response ->

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