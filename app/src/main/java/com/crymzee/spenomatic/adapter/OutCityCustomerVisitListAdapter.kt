package com.crymzee.spenomatic.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.crymzee.spenomatic.databinding.ItemOutCityCustomerVisitListBinding
import com.crymzee.spenomatic.model.request.createLocalExpense.Customer

class OutCityCustomerVisitListAdapter(
    private val list: MutableList<Customer>,
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
        holder.binding.apply {
            labelLeaveType.text = item.name
            labelLeaveDate.text = item.email
            etEmail.text = item.objective
            ivDelete.setOnClickListener {
                item.name?.let { name -> visitId?.invoke(name) }
            }

        }

    }

    private var visitId: ((String) -> Unit)? = null

    fun getVisitId(listener: (String) -> Unit) {
        visitId = listener
    }


    fun deleteAction(actionId: String) {
        val index = list.indexOfFirst { item ->
            item.name == actionId
        }
        if (index != -1) {
            list.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    override fun getItemCount(): Int = list.size
}
