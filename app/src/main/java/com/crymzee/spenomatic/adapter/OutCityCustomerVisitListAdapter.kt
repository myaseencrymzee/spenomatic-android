package com.crymzee.spenomatic.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.crymzee.spenomatic.databinding.ItemOutCityCustomerVisitListBinding
import com.crymzee.spenomatic.model.request.createLocalExpense.Customer

class OutCityCustomerVisitListAdapter(
    private val list: MutableList<Customer>,
    private val onViewDetailClick: (String) -> Unit // callback for click
) : RecyclerView.Adapter<OutCityCustomerVisitListAdapter.FriendViewHolder>() {

    inner class FriendViewHolder(val binding: ItemOutCityCustomerVisitListBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val binding = ItemOutCityCustomerVisitListBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FriendViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val item = list[position]

    }

    override fun getItemCount(): Int = list.size
}
