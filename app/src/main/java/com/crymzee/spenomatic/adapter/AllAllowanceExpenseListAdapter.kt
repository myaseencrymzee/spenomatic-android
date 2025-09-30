package com.crymzee.spenomatic.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.crymzee.spenomatic.databinding.ItemAllowanceListBinding
import com.crymzee.spenomatic.model.request.createOutsideExpense.LodgingBoardingExpense

class AllAllowanceExpenseListAdapter(
    private val list: MutableList<LodgingBoardingExpense>,
    private val onViewDetailClick: (String) -> Unit // callback for click
) : RecyclerView.Adapter<AllAllowanceExpenseListAdapter.FriendViewHolder>() {

    inner class FriendViewHolder(val binding: ItemAllowanceListBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val binding = ItemAllowanceListBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FriendViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val item = list[position]

    }

    override fun getItemCount(): Int = list.size
}
