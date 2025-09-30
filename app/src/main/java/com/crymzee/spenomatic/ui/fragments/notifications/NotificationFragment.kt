package com.crymzee.spenomatic.ui.fragments.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.crymzee.spenomatic.R
import com.crymzee.spenomatic.adapter.NotificationsListAdapter
import com.crymzee.spenomatic.base.BaseFragment
import com.crymzee.spenomatic.databinding.FragmentNotificationsBinding
import com.crymzee.spenomatic.utils.goBack

class NotificationFragment : BaseFragment() {

    private lateinit var binding: FragmentNotificationsBinding
    private lateinit var notificationsListAdapter: NotificationsListAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        if (!::binding.isInitialized) {
            binding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_notifications,
                container,
                false
            )
            viewInit()
        }


        return binding.root
    }

    private fun viewInit() {
        setupAdapters()
        binding.ivBack.setOnClickListener { goBack() }
    }

    private fun setupAdapters() {
        val dummyList = listOf(
            "",
            "",
            "",
            "",
            "",
            "",
            "",
        )
        notificationsListAdapter = NotificationsListAdapter(dummyList) {
        }


        binding.rvSchedule.adapter = notificationsListAdapter
    }
}