package com.crymzee.spenomatic.ui.fragments.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.crymzee.spenomatic.R
import com.crymzee.spenomatic.adapter.CustomerListAdapter
import com.crymzee.spenomatic.base.BaseFragment
import com.crymzee.spenomatic.databinding.FragmentCustomerBinding
import com.crymzee.spenomatic.state.Resource
import com.crymzee.spenomatic.utils.SpenoMaticLogger
import com.crymzee.spenomatic.utils.confirmationPopUp
import com.crymzee.spenomatic.utils.extractFirstErrorMessage
import com.crymzee.spenomatic.utils.showErrorPopup
import com.crymzee.spenomatic.viewModel.CustomersViewModel
import com.example.flowit.abstracts.PaginationScrollListener
import kotlinx.coroutines.launch

class CustomerFragment : BaseFragment() {
    private lateinit var binding: FragmentCustomerBinding
    private lateinit var customerListAdapter: CustomerListAdapter
    private var isLastPage: Boolean = false
    private var isLoading: Boolean = false
    private val layoutManager by lazy { getLinearLayoutManager() }
    private val customersViewModel: CustomersViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        if (!::binding.isInitialized) {
            binding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_customer,
                container,
                false
            )
            viewInit()
        }


        return binding.root
    }

    private fun viewInit() {

        initAdapter()
        binding.apply {
            icAdd.setOnClickListener {
                val bundle = Bundle()
                bundle.putInt("customerId", 0)
                bundle.putBoolean("isEditable", false)
                navigate(R.id.action_customerFragment_to_addCustomerFragment, bundle)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        customersViewModel.page = 1
        customersViewModel.perPage = 10
        observePublicPosts()


    }

    override fun onResume() {
        super.onResume()
        customersViewModel.getAllCustomers()
    }

    private fun observePublicPosts() {
        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                customersViewModel.getAllCustomersLiveData.observe(viewLifecycleOwner) { response ->
                    binding.loader.isVisible = response is Resource.Loading<*>

                    when (response) {
                        is Resource.Error -> {
                            val errorMessage = extractFirstErrorMessage(response.throwable)
                            SpenoMaticLogger.logErrorMsg("Error", errorMessage.description)
                        }

                        is Resource.Loading -> {}

                        is Resource.Success -> {

                            response.data?.let { posts ->
                                val isFirstPage = customersViewModel.page == 1
                                val isEmptyList =
                                    posts.data.isEmpty() && customersViewModel.page == 1
                                binding.tvNoData.isVisible = isEmptyList

                                binding.tvEarnings.text = posts.stats.today_visits.toString()
                                binding.tvReviews.text = posts.stats.weekly_visits.toString()


                                if (isFirstPage) {
                                    customerListAdapter.clearList(posts.data)
                                } else {
                                    customerListAdapter.addPaginatedDataToList(posts.data)
                                }

                                val hasNextPage = !posts.pagination.links.next.isNullOrEmpty()
                                isLastPage = !hasNextPage
                                isLoading = false

                                if (hasNextPage) {
                                    customersViewModel.page++
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun initAdapter() {
        customerListAdapter = CustomerListAdapter(requireContext())
        binding.rvCustomers.apply {
            adapter = customerListAdapter
            layoutManager = this@CustomerFragment.layoutManager
            addOnScrollListener(object :
                PaginationScrollListener(this@CustomerFragment.layoutManager, 10) {
                override fun isLastPage(): Boolean = this@CustomerFragment.isLastPage
                override fun isLoading(): Boolean = this@CustomerFragment.isLoading

                override fun loadMoreItems() {
                    if (!isLastPage && !isLoading) {
                        isLoading = true
                        customersViewModel.getAllCustomers()
                    }
                }
            })
        }
        customerListAdapter.getDeleteCustomerId {
            confirmationPopUp(
                requireContext(),
                heading = "Confirm Deletion",
                description = "Are you sure you want to delete this item? This action cannot be undone.",
                icon = R.drawable.ic_delete_item,
                onConfirm = {
                    deletePostComment(it)
                })
        }
        customerListAdapter.getCustomerId {
            val bundle = Bundle()
            bundle.putInt("customerId", it)
            bundle.putBoolean("isEditable", true)
            navigate(R.id.action_customerFragment_to_addCustomerFragment, bundle)
        }

    }


    private fun deletePostComment(id: Int) {
        customersViewModel.deleteCustomer(id).observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Error -> {
                    val errorMessage = extractFirstErrorMessage(response.throwable)
                    SpenoMaticLogger.logErrorMsg("Error", errorMessage.description)
                    showErrorPopup(requireContext(), "", errorMessage.description)
                }

                is Resource.Loading -> {}
                is Resource.Success -> {
                    customerListAdapter.deleteAction(id)

                }
            }
        }
    }
}