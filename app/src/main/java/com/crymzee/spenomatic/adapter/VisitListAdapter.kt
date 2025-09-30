package com.crymzee.spenomatic.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.crymzee.spenomatic.databinding.ItemVisitedListBinding

class VisitListAdapter(
    private val list: List<String>,
    private val onViewDetailClick: (String) -> Unit // callback for click
) : RecyclerView.Adapter<VisitListAdapter.FriendViewHolder>() {

    inner class FriendViewHolder(val binding: ItemVisitedListBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val binding = ItemVisitedListBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FriendViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val item = list[position]


        // Handle button click
        holder.binding.btnViewDetail.setOnClickListener {
            onViewDetailClick(item)
        }
    }

    override fun getItemCount(): Int = list.size
}
