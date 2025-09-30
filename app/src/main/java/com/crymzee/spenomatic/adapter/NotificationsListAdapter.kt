package com.crymzee.spenomatic.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.crymzee.spenomatic.databinding.ItemNotificationListBinding
import com.crymzee.spenomatic.databinding.ItemVisitedListBinding

class NotificationsListAdapter(
    private val list: List<String>,
    private val onViewDetailClick: (String) -> Unit // callback for click
) : RecyclerView.Adapter<NotificationsListAdapter.FriendViewHolder>() {

    inner class FriendViewHolder(val binding: ItemNotificationListBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val binding = ItemNotificationListBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FriendViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val item = list[position]


    }

    override fun getItemCount(): Int = list.size
}
