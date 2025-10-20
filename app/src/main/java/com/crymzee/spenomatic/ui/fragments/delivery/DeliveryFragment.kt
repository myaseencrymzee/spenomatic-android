package com.crymzee.spenomatic.ui.fragments.delivery

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
import com.crymzee.spenomatic.adapter.DeliveryListAdapter
import com.crymzee.spenomatic.base.BaseFragment
import com.crymzee.spenomatic.databinding.FragmentDeliveryBinding
import com.crymzee.spenomatic.state.Resource
import com.crymzee.spenomatic.utils.SpenoMaticLogger
import com.crymzee.spenomatic.utils.extractFirstErrorMessage
import com.crymzee.spenomatic.utils.visible
import com.crymzee.spenomatic.viewModel.VisitsViewModel
import com.example.flowit.abstracts.PaginationScrollListener


class DeliveryFragment : BaseFragment() {
    private lateinit var binding: FragmentDeliveryBinding
    private lateinit var scheduleAdapter: DeliveryListAdapter
    private var isLastPage: Boolean = false
    private var isLoading: Boolean = false
    private val layoutManager by lazy { getLinearLayoutManager() }
    private val visitViewModel: VisitsViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        if (!::binding.isInitialized) {
            binding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_delivery,
                container,
                false
            )
            showBottomNav()
            viewInit()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        visitViewModel.page = 1
        visitViewModel.perPage = 10
        visitViewModel.selectedTab = "assigned"
        observePublicPosts()

    }


    private fun viewInit() {
        initAdapter()
        binding.apply {
            tvSchedule.setOnClickListener {
                rvSchedule.visible()
                selectTab(tvSchedule)
            }
            tvVisited.setOnClickListener {
                rvSchedule.visible()
                selectTab(tvVisited)
            }
        }
    }

    private fun selectTab(selected: View) {
        val defaultTextColor = ContextCompat.getColor(requireContext(), R.color.black_1717)
        val views = listOf(binding.tvSchedule, binding.tvVisited)

        views.forEach { tv ->
            tv.setBackgroundResource(0)
            tv.setTextColor(defaultTextColor)
        }

        (selected as? TextView)?.apply {
            setBackgroundResource(R.drawable.rounded_12_border_black)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

            when (id) {
                binding.tvSchedule.id -> {
                    visitViewModel.page = 1
                    visitViewModel.selectedTab = "assigned"
                    visitViewModel.getAllDelivery( visitViewModel.selectedTab )

                }

                binding.tvVisited.id -> {
                    visitViewModel.page = 1
                    visitViewModel.selectedTab = "delivered"
                    visitViewModel.getAllDelivery( visitViewModel.selectedTab )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        visitViewModel.page = 1
        visitViewModel.perPage = 10
        visitViewModel.getAllDelivery( visitViewModel.selectedTab )

    }
    private fun observePublicPosts() {
        visitViewModel.getAllDeliveryLiveData.removeObservers(viewLifecycleOwner)
        visitViewModel.getAllDeliveryLiveData.observe(viewLifecycleOwner) { response ->
            binding.loader.isVisible = response is Resource.Loading<*>

            when (response) {
                is Resource.Error -> {
                    val errorMessage = extractFirstErrorMessage(response.throwable)
                    SpenoMaticLogger.logErrorMsg("Error", errorMessage.description)
                }

                is Resource.Loading -> {}
                is Resource.Success -> {
                    response.data?.let { posts ->
                        val isFirstPage = visitViewModel.page == 1
                        val isEmptyList =
                            posts.data.isEmpty() && visitViewModel.page == 1
                        binding.tvNoData.isVisible = isEmptyList
                        if(visitViewModel.selectedTab == "pending,checked_in"){
                            binding.tvNoData.text = "No Assigned delivery yet"
                        }else{
                            binding.tvNoData.text = "No delivery yet"
                        }

                        if (isFirstPage) {
                            scheduleAdapter.clearList(posts.data)
                        } else {
                            scheduleAdapter.addPaginatedDataToList(posts.data)
                        }

                        val hasNextPage = !posts.pagination.links.next.isNullOrEmpty()
                        isLastPage = !hasNextPage
                        isLoading = false

                        if (hasNextPage) {
                            visitViewModel.page++
                        }
                    }
                }
            }
        }
    }

    private fun initAdapter() {
        scheduleAdapter = DeliveryListAdapter(requireContext())
        binding.rvSchedule.apply {
            adapter = scheduleAdapter
            layoutManager = this@DeliveryFragment.layoutManager
            addOnScrollListener(object :
                PaginationScrollListener(this@DeliveryFragment.layoutManager, 10) {
                override fun isLastPage(): Boolean = this@DeliveryFragment.isLastPage
                override fun isLoading(): Boolean = this@DeliveryFragment.isLoading

                override fun loadMoreItems() {
                    if (!isLastPage && !isLoading) {
                        isLoading = true
                        visitViewModel.getAllVisit( visitViewModel.selectedTab)
                    }
                }
            })
        }

        scheduleAdapter.getVisitId {delivery->
            val bundle = Bundle()
            bundle.putInt("deliveryId", delivery.id)
            bundle.putString("location", delivery.location)
            navigate(R.id.action_deliveryFragment_to_deliveryMapFragment, bundle)
        }

    }
    override fun onDestroy() {
        super.onDestroy()
        visitViewModel.selectedTab = "assigned"
    }
}
