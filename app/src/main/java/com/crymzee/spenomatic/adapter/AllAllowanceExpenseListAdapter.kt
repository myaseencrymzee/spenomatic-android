package com.crymzee.spenomatic.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.crymzee.spenomatic.databinding.ItemAllowanceListBinding
import com.crymzee.spenomatic.model.request.createOutsideExpense.TravelAllowance
import com.crymzee.spenomatic.utils.toCamelCase

class AllAllowanceExpenseListAdapter(
    private val list: MutableList<TravelAllowance>,
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
        holder.binding.apply {
            labelLeaveType.text = item.allowance_type.toCamelCase()
            labelLeaveDate.text = item.description
            tvAmount.text = "Total: \$${item.amount}"
            ivDelete.setOnClickListener {
                item.allowance_type.let { name -> visitId?.invoke(name) }

            }
        }
    }

    private var visitId: ((String) -> Unit)? = null

    fun getVisitId(listener: (String) -> Unit) {
        visitId = listener
    }


    fun deleteAction(actionId: String) {
        val index = list.indexOfFirst { item ->
            item.allowance_type == actionId
        }
        if (index != -1) {
            list.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    override fun getItemCount(): Int = list.size
}
