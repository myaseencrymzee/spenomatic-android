package com.crymzee.spenomatic.ui.fragments.expenses

import android.os.Bundle
import android.util.Log
import android.view.Gravity
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
import com.crymzee.spenomatic.R
import com.crymzee.spenomatic.adapter.ExpensesAdapter
import com.crymzee.spenomatic.base.BaseFragment
import com.crymzee.spenomatic.databinding.FragmentExpensesBinding
import com.crymzee.spenomatic.databinding.ItemPostActionMenuPublicBinding
import com.crymzee.spenomatic.state.Resource
import com.crymzee.spenomatic.utils.SpenoMaticLogger
import com.crymzee.spenomatic.utils.extractFirstErrorMessage
import com.crymzee.spenomatic.viewModel.ExpensesViewModel
import com.example.flowit.abstracts.PaginationScrollListener
import kotlin.getValue

class ExpensesFragment : BaseFragment() {
    private lateinit var binding: FragmentExpensesBinding
    private lateinit var expressAdapter: ExpensesAdapter
    private var isLastPage: Boolean = false
    private var isLoading: Boolean = false
    private val layoutManager by lazy { getLinearLayoutManager() }
    private val expensesViewModel: ExpensesViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        if (!::binding.isInitialized) {
            binding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_expenses,
                container,
                false
            )
            showBottomNav()
            viewInit()

        }


        return binding.root
    }

    private fun viewInit() {
        initAdapter()
        binding.apply {
            icAdd.setOnClickListener {
                onMenuClick(icAdd)
            }
            tvAll.setOnClickListener { selectTab(tvAll) }
            tvApproved.setOnClickListener { selectTab(tvApproved) }
            tvRejected.setOnClickListener { selectTab(tvRejected) }
            tvPending.setOnClickListener { selectTab(tvPending) }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observePublicPosts()
    }

    override fun onResume() {
        super.onResume()
        expensesViewModel.page = 1
        expensesViewModel.perPage = 10
        expensesViewModel.getAllExpenses(expensesViewModel.category)
    }

    override fun onDestroy() {
        super.onDestroy()
        expensesViewModel.category= ""
    }


    private fun observePublicPosts() {
        expensesViewModel.getAllExpensesLiveData.removeObservers(viewLifecycleOwner)
        expensesViewModel.getAllExpensesLiveData.observe(viewLifecycleOwner) { response ->
            binding.loader.isVisible = response is Resource.Loading<*>

            when (response) {
                is Resource.Error -> {
                    val errorMessage = extractFirstErrorMessage(response.throwable)
                    SpenoMaticLogger.logErrorMsg("Error", errorMessage.description)
                }

                is Resource.Loading -> {}

                is Resource.Success -> {

                    response.data?.let { posts ->
                        val isFirstPage = expensesViewModel.page == 1
                        val isEmptyList =
                            posts.data.isEmpty() && expensesViewModel.page == 1
                        binding.tvNoData.isVisible = isEmptyList

                        if (isFirstPage) {
                            expressAdapter.clearList(posts.data)
                        } else {
                            expressAdapter.addPaginatedDataToList(posts.data)
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

    private fun initAdapter() {
        expressAdapter = ExpensesAdapter(requireContext())
        binding.rvLeaves.apply {
            adapter = expressAdapter
            layoutManager = this@ExpensesFragment.layoutManager
            addOnScrollListener(object :
                PaginationScrollListener(this@ExpensesFragment.layoutManager, 10) {
                override fun isLastPage(): Boolean = this@ExpensesFragment.isLastPage
                override fun isLoading(): Boolean = this@ExpensesFragment.isLoading

                override fun loadMoreItems() {
                    if (!isLastPage && !isLoading) {
                        isLoading = true
                        expensesViewModel.getAllExpenses(expensesViewModel.category)
                    }
                }
            })
        }


    }

    private fun selectTab(selected: View) {
        // reset all first
        val defaultTextColor = ContextCompat.getColor(requireContext(), R.color.black_1717)
        val views = listOf(binding.tvAll, binding.tvApproved, binding.tvRejected, binding.tvPending)

        views.forEach { tv ->
            tv.setBackgroundResource(0) // remove background
            tv.setTextColor(defaultTextColor)
        }

        // set selected style
        (selected as? TextView)?.apply {
            setBackgroundResource(R.drawable.rounded_12_border_black)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

            when (id) {
                binding.tvAll.id -> {
                    expensesViewModel.page = 1
                    expensesViewModel.category = ""
                    expensesViewModel.getAllExpenses(expensesViewModel.category)
                }

                binding.tvApproved.id -> {
                    expensesViewModel.page = 1
                    expensesViewModel.category = "approved"
                    expensesViewModel.getAllExpenses(expensesViewModel.category)
                }

                binding.tvRejected.id -> {
                    expensesViewModel.page = 1
                    expensesViewModel.category = "rejected"
                    expensesViewModel.getAllExpenses(expensesViewModel.category)
                }

                binding.tvPending.id -> {
                    expensesViewModel.page = 1
                    expensesViewModel.category = "pending"
                    expensesViewModel.getAllExpenses(expensesViewModel.category)
                }
            }
        }
    }


    private fun onMenuClick(icon: View) {
        try {
            val dialogBinding = ItemPostActionMenuPublicBinding.inflate(
                LayoutInflater.from(requireContext()),
                null,
                false
            )

            val popUp = PopupWindow(
                dialogBinding.root,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                false
            )

            val location = IntArray(2)
            icon.getLocationOnScreen(location)

            popUp.apply {
                isTouchable = true
                isFocusable = true
                isOutsideTouchable = true
                showAtLocation(icon, Gravity.NO_GRAVITY, location[0], location[1] + 30)
            }

            dialogBinding.tvFuelVoucher.setOnClickListener {
                popUp.dismiss()
                navigate(R.id.action_expensesFragment_to_addFuelExpensesFragment)
            }

            dialogBinding.tvLocalVisit.setOnClickListener {
                popUp.dismiss()
                navigate(R.id.action_expensesFragment_to_addLocalVisitFragment)

            }

            dialogBinding.tvOutsideVisit.setOnClickListener {
                popUp.dismiss()
                navigate(R.id.action_expensesFragment_to_addOutCityVisitFragment)


            }
            dialogBinding.tvOther.setOnClickListener {
                popUp.dismiss()
                navigate(R.id.action_expensesFragment_to_addOthersFragment)

            }

        } catch (e: Exception) {
            Log.i("EXC", "showInfoBanner: exception $e")
        }
    }

}