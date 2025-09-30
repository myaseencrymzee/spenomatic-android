package com.crymzee.spenomatic.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.crymzee.spenomatic.databinding.ItemBusTrainListBinding
import com.crymzee.spenomatic.model.request.createOutsideExpense.LodgingBoardingExpense

class AllBusesTrainExpenseListAdapter(
    private val list: MutableList<LodgingBoardingExpense>,
    private val onViewDetailClick: (String) -> Unit // callback for click
) : RecyclerView.Adapter<AllBusesTrainExpenseListAdapter.FriendViewHolder>() {

    inner class FriendViewHolder(val binding: ItemBusTrainListBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val binding = ItemBusTrainListBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FriendViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val item = list[position]

    }

    override fun getItemCount(): Int = list.size
}
