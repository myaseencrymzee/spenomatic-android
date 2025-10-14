package com.crymzee.spenomatic.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.crymzee.spenomatic.databinding.ItemCustomerListBinding
import com.crymzee.spenomatic.databinding.ItemCustomerVisitListBinding
import com.crymzee.spenomatic.databinding.ItemMiscellaneousListBinding
import com.crymzee.spenomatic.databinding.ItemTransaportExpenseListBinding
import com.crymzee.spenomatic.model.request.createLocalExpense.TransportExpense
import com.crymzee.spenomatic.utils.toCamelCase

class AllTransportExpenseListAdapter(
    private val list: MutableList<TransportExpense>,
) : RecyclerView.Adapter<AllTransportExpenseListAdapter.FriendViewHolder>() {

    inner class FriendViewHolder(val binding: ItemTransaportExpenseListBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val binding = ItemTransaportExpenseListBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FriendViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val item = list[position]
        holder.binding.apply {
            labelLeaveType.text = item.from_location.toCamelCase()
            labelLeaveDate.text = item.to_location.toCamelCase()
            tvAmount.text = "\$${item.amount}"
            ivDelete.setOnClickListener {
                item.from_location?.let { name -> visitId?.invoke(name) }

            }
        }
    }


    private var visitId: ((String) -> Unit)? = null

    fun getVisitId(listener: (String) -> Unit) {
        visitId = listener
    }


    fun deleteAction(actionId: String) {
        val index = list.indexOfFirst { item ->
            item.from_location == actionId
        }
        if (index != -1) {
            list.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    override fun getItemCount(): Int = list.size
}

