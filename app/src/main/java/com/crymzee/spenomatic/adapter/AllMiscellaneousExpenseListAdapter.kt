package com.crymzee.spenomatic.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.crymzee.spenomatic.databinding.ItemCustomerListBinding
import com.crymzee.spenomatic.databinding.ItemCustomerVisitListBinding
import com.crymzee.spenomatic.databinding.ItemMiscellaneousListBinding
import com.crymzee.spenomatic.model.request.createLocalExpense.MiscellaneousExpense

class AllMiscellaneousExpenseListAdapter(
    private val list: MutableList<MiscellaneousExpense>,
    private val onViewDetailClick: (String) -> Unit // callback for click
) : RecyclerView.Adapter<AllMiscellaneousExpenseListAdapter.FriendViewHolder>() {

    inner class FriendViewHolder(val binding: ItemMiscellaneousListBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val binding = ItemMiscellaneousListBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FriendViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val item = list[position]

        holder.binding.apply {
            labelLeaveType.text = item.objective
            labelLeaveDate.text = item.description
            tvAmount.text = "\$${item.amount}"
            ivDelete.setOnClickListener {
                item.objective?.let { name -> visitId?.invoke(name) }

            }
        }

    }

    private var visitId: ((String) -> Unit)? = null

    fun getVisitId(listener: (String) -> Unit) {
        visitId = listener
    }


    fun deleteAction(actionId: String) {
        val index = list.indexOfFirst { item ->
            item.objective == actionId
        }
        if (index != -1) {
            list.removeAt(index)
            notifyItemRemoved(index)
        }
    }
    override fun getItemCount(): Int = list.size
}
