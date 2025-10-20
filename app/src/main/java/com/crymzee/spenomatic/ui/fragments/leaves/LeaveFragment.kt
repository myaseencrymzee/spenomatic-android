package com.crymzee.spenomatic.ui.fragments.leaves

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.crymzee.spenomatic.R
import com.crymzee.spenomatic.adapter.LeaveListAdapter
import com.crymzee.spenomatic.base.BaseFragment
import com.crymzee.spenomatic.databinding.FragmentLeavesBinding
import com.crymzee.spenomatic.state.Resource
import com.crymzee.spenomatic.utils.SpenoMaticLogger
import com.crymzee.spenomatic.utils.extractFirstErrorMessage
import com.crymzee.spenomatic.viewModel.LeavesViewModel
import com.example.flowit.abstracts.PaginationScrollListener

class LeaveFragment : BaseFragment() {
    private lateinit var binding: FragmentLeavesBinding
    private lateinit var leaveListAdapter: LeaveListAdapter
    private var isLastPage: Boolean = false
    private var isLoading: Boolean = false
    var totalLeaves = 0.0
    private val layoutManager by lazy { getLinearLayoutManager() }
    private val leavesViewModel: LeavesViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        if (!::binding.isInitialized) {
            binding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_leaves,
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
                val bundle = Bundle()
                bundle.putDouble("totalLeaves", totalLeaves)
                navigate(R.id.action_leaveFragment_to_addLeaveFragment, bundle)
            }
            tvAll.setOnClickListener { selectTab(tvAll) }
            tvApproved.setOnClickListener { selectTab(tvApproved) }
            tvRejected.setOnClickListener { selectTab(tvRejected) }
            tvPending.setOnClickListener { selectTab(tvPending) }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        leavesViewModel.page = 1
        leavesViewModel.perPage = 10
        leavesViewModel.getAllLeaves(leavesViewModel.category)
        observePublicPosts()
    }


    override fun onDestroy() {
        super.onDestroy()
        leavesViewModel.category = ""
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
                    leavesViewModel.page = 1
                    leavesViewModel.category = ""
                    leavesViewModel.getAllLeaves(leavesViewModel.category)
                }

                binding.tvApproved.id -> {
                    leavesViewModel.page = 1
                    leavesViewModel.category = "approved"
                    leavesViewModel.getAllLeaves(leavesViewModel.category)
                }

                binding.tvRejected.id -> {
                    leavesViewModel.page = 1
                    leavesViewModel.category = "rejected"
                    leavesViewModel.getAllLeaves(leavesViewModel.category)
                }

                binding.tvPending.id -> {
                    leavesViewModel.page = 1
                    leavesViewModel.category = "pending"
                    leavesViewModel.getAllLeaves(leavesViewModel.category)
                }
            }
        }

    }

    private fun observePublicPosts() {
        leavesViewModel.getAllLeavesLiveData.removeObservers(viewLifecycleOwner)
        leavesViewModel.getAllLeavesLiveData.observe(viewLifecycleOwner) { response ->
            binding.loader.isVisible = response is Resource.Loading<*>

            when (response) {
                is Resource.Error -> {
                    val errorMessage = extractFirstErrorMessage(response.throwable)
                    SpenoMaticLogger.logErrorMsg("Error", errorMessage.description)
                }

                is Resource.Loading -> {}

                is Resource.Success -> {

                    response.data?.let { posts ->
                        val isFirstPage = leavesViewModel.page == 1
                        val isEmptyList =
                            posts.data.isEmpty() && leavesViewModel.page == 1
                        binding.tvNoData.isVisible = isEmptyList
                        if(leavesViewModel.category == ""){
                            binding.tvNoData.text = "No leaves have been created yet"

                        }else {
                            binding.tvNoData.text = "No ${leavesViewModel.category} leaves yet"

                        }

                        totalLeaves = posts.stats.leaves_left

                        if (isFirstPage) {
                            leaveListAdapter.clearList(posts.data)
                        } else {
                            leaveListAdapter.addPaginatedDataToList(posts.data)
                        }

                        val hasNextPage = !posts.pagination.links.next.isNullOrEmpty()
                        isLastPage = !hasNextPage
                        isLoading = false

                        if (hasNextPage) {
                            leavesViewModel.page++
                        }
                    }
                }
            }
        }
    }

    private fun initAdapter() {
        leaveListAdapter = LeaveListAdapter(requireContext())
        binding.rvLeaves.apply {
            adapter = leaveListAdapter
            layoutManager = this@LeaveFragment.layoutManager
            addOnScrollListener(object :
                PaginationScrollListener(this@LeaveFragment.layoutManager, 10) {
                override fun isLastPage(): Boolean = this@LeaveFragment.isLastPage
                override fun isLoading(): Boolean = this@LeaveFragment.isLoading

                override fun loadMoreItems() {
                    if (!isLastPage && !isLoading) {
                        isLoading = true
                        leavesViewModel.getAllLeaves(leavesViewModel.category)
                    }
                }
            })
        }


    }
}
