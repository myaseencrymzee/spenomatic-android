package com.crymzee.spenomatic.ui.fragments.expenses.addOutCityExpense

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
import com.crymzee.spenomatic.model.request.createLocalExpense.Customer
import com.crymzee.spenomatic.model.request.createLocalExpense.MiscellaneousExpense
import com.crymzee.spenomatic.model.request.createLocalExpense.TransportExpense
import com.crymzee.spenomatic.model.request.createOutsideExpense.LodgingBoardingExpense
import com.crymzee.spenomatic.state.Resource
import com.crymzee.spenomatic.utils.SpenoMaticLogger
import com.crymzee.spenomatic.utils.extractFirstErrorMessage
import com.crymzee.spenomatic.utils.goBack
import com.crymzee.spenomatic.viewModel.CustomersViewModel
import com.crymzee.spenomatic.viewModel.ExpensesViewModel

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
    val busTimingExpenseList: MutableList<LodgingBoardingExpense> = mutableListOf()
    val allowanceExpenseList: MutableList<LodgingBoardingExpense> = mutableListOf()
    private var dialogueAddUser: DialogAddUserBinding? = null
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
        }
    }

    private fun setupAdapters() {

        outCityCustomerVisitListAdapter = OutCityCustomerVisitListAdapter(userList) {

        }
        binding.rvCustomers.adapter = outCityCustomerVisitListAdapter
        allMiscellaneousExpenseListAdapter =
            AllMiscellaneousExpenseListAdapter(miscellaneousListExpense) {

            }
        binding.rvMiscellaneous.adapter = allMiscellaneousExpenseListAdapter
        allTransportExpenseListAdapter = AllTransportExpenseListAdapter(transportExpenseList) {

        }
        binding.rvTransport.adapter = allTransportExpenseListAdapter

        allLodgingExpenseListAdapter = AllLodgingExpenseListAdapter(lodgingExpenseList) {

        }
        binding.rvLoging.adapter = allLodgingExpenseListAdapter
        allAllowanceExpenseListAdapter = AllAllowanceExpenseListAdapter(allowanceExpenseList) {

        }
        binding.rvAllowance.adapter = allAllowanceExpenseListAdapter

        allBusesTrainExpenseListAdapter = AllBusesTrainExpenseListAdapter(busTimingExpenseList) {

        }
        binding.rvBusTrain.adapter = allBusesTrainExpenseListAdapter


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
                alertDialog.dismiss()
                activeDialog = null

                Handler(Looper.getMainLooper()).postDelayed({
//                    reportUser(description, post.id)
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
            activeDialog?.dismiss()  // Dismiss any existing dialog before showing a new one

            // Inflate only once (re-use binding if already created)
            if (dialogueAddUser == null) {
                dialogueAddUser = DialogAddUserBinding.inflate(LayoutInflater.from(context))
            }

            val alertDialog = AlertDialog.Builder(context).create()
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))



            activeDialog = alertDialog

            // OK button with slight delay before invoking the callback
            dialogueAddUser?.ivCancel?.setOnClickListener {
                selectCategory(dialogueAddUser!!)
            }

            // OK button with slight delay before invoking the callback
            dialogueAddUser?.ivCancel?.setOnClickListener {
                alertDialog.dismiss()
                activeDialog = null
            }

            dialogueAddUser?.btnAdd?.setOnClickListener {
                alertDialog.dismiss()
                activeDialog = null

                Handler(Looper.getMainLooper()).postDelayed({
//                    reportUser(description, post.id)
                }, 300)
            }

            // Set the view and show dialog
            alertDialog.setView(dialogueAddUser?.root)
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
                alertDialog.dismiss()
                activeDialog = null

                Handler(Looper.getMainLooper()).postDelayed({
//                    reportUser(description, post.id)
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

            dialogueAddUser.ivDropDownGender.rotation = 180f

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
                showAsDropDown(dialogueAddUser.layoutSelectLocation, 0, 0)
                setOnDismissListener {
                    dialogueAddUser.ivDropDownGender.rotation = 0f
                }
            }

            val rvItems: RecyclerView = dialogView.findViewById(R.id.rv_year)
            rvItems.layoutManager = LinearLayoutManager(requireContext())
            rvItems.adapter = dropDownAdapterClient

            loadMoreData() // load first batch

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

            dropDownAdapterClient.getClientType {
                val newCustomer = Customer(
                    customer = it.id,
                    objective = dialogueAddUser.etDescription.text.toString(),
                    name = it.fullname,
                    email = it.address
                )
                userList.add(newCustomer)

                outCityCustomerVisitListAdapter.notifyItemInserted(userList.size - 1)
                binding.rvCustomers.scrollToPosition(userList.size - 1)

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


            // OK button with slight delay before invoking the callback
            dialogueTrains.ivCancel.setOnClickListener {
                alertDialog.dismiss()
                activeDialog = null
            }

            dialogueTrains.btnAdd.setOnClickListener {
                alertDialog.dismiss()
                activeDialog = null

                Handler(Looper.getMainLooper()).postDelayed({
//                    reportUser(description, post.id)
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
            activeDialog?.dismiss()  // Dismiss any existing dialog before showing a new one

            // Inflate layout using ViewBinding
            val dialogueLodge = DialogAddLodgingExpenseBinding.inflate(LayoutInflater.from(context))

            val alertDialog = AlertDialog.Builder(context).create()
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            // Store reference to active dialog
            activeDialog = alertDialog


            // OK button with slight delay before invoking the callback
            dialogueLodge.ivCancel.setOnClickListener {
                alertDialog.dismiss()
                activeDialog = null
            }

            dialogueLodge.btnAdd.setOnClickListener {
                alertDialog.dismiss()
                activeDialog = null

                Handler(Looper.getMainLooper()).postDelayed({
//                    reportUser(description, post.id)
                }, 300)
            }

            // Set the view and show dialog
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
                alertDialog.dismiss()
                activeDialog = null

                Handler(Looper.getMainLooper()).postDelayed({
//                    reportUser(description, post.id)
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