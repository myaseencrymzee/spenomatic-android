package com.crymzee.spenomatic.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.crymzee.spenomatic.R
import com.crymzee.spenomatic.databinding.ItemCustomerListBinding
import com.crymzee.spenomatic.model.response.allCustomers.Data
import com.crymzee.spenomatic.utils.toCamelCase

class CustomerListAdapter(val context: Context) :
    RecyclerView.Adapter<CustomerListAdapter.RecyclerViewHolder>() {


    var list = mutableListOf<Data>()

    var lastSelectedItem = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        RecyclerViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_customer_list,
                parent,
                false
            )
        )


    override fun getItemCount(): Int {
        return list.size
    }

    private fun getItemLastCount(): Int {
        return (list.size - 1)
    }

    fun addPaginatedDataToList(data: List<Data>) {
        val uniqueItems = data.filterNot { newItem ->
            list.any { it.id == newItem.id }
        }
        val start = list.size
        list.addAll(uniqueItems)
        notifyItemRangeInserted(start, uniqueItems.size)
    }

    fun clearList(data: List<Data>) {
        this.list.clear()
        this.list.addAll(data)
        this.notifyDataSetChanged()
    }

    fun addAll(followers: List<Data>) {
        this.list.clear()
        this.list.addAll(followers)
        notifyDataSetChanged()
    }
    fun deleteAction(actionId: Int) {
        val index = list.indexOfFirst { it.id == actionId } // Find index of the item
        if (index != -1) {
            list.removeAt(index) // Remove item from list
            notifyItemRemoved(index) // Notify RecyclerView about the change
        }
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val data = list[position]
        val mContext = holder.binding.root


        holder.binding.apply {

            labelLeaveType.text = data.fullname.toCamelCase()
            labelLeaveDate.text = data.address

            icEdit.setOnClickListener {
                data.let { it1 -> customerId?.invoke(it1.id) }
            }
            btnDelete.setOnClickListener {
                data.let { it1 -> deleteCustomerId?.invoke(it1.id) }
            }
        }

        holder.binding.executePendingBindings()
    }


    class RecyclerViewHolder(val binding: ItemCustomerListBinding) :
        RecyclerView.ViewHolder(binding.root)

    private var customerId: ((Int) -> Unit)? = null

    fun getCustomerId(listener: (Int) -> Unit) {
        customerId = listener
    }

    private var deleteCustomerId: ((Int) -> Unit)? = null

    fun getDeleteCustomerId(listener: (Int) -> Unit) {
        deleteCustomerId = listener
    }
}