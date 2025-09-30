package com.crymzee.spenomatic.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.crymzee.spenomatic.databinding.ItemContactListBinding
import com.crymzee.spenomatic.model.response.customerDetail.Contact
import com.crymzee.spenomatic.utils.toCamelCase

class CustomerContactListAdapter(
    private val list: MutableList<Contact>
) : RecyclerView.Adapter<CustomerContactListAdapter.FriendViewHolder>() {

    inner class FriendViewHolder(val binding: ItemContactListBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val binding = ItemContactListBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FriendViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val item = list[position]
        holder.binding.apply {
            labelHello.text = item.fullname.toCamelCase()
            tvLastVisit.text = item.designation ?: "NA"
            tvTotalVisit.text = item.email
            tvFrequency.text = item.phone
            btnDelete.setOnClickListener {
                item.fullname?.let { name -> visitId?.invoke(name) }

            }
        }

    }


    private var visitId: ((String) -> Unit)? = null

    fun getVisitId(listener: (String) -> Unit) {
        visitId = listener
    }

    fun addContact(contact: Contact) {
        list.add(contact)
        notifyItemInserted(list.size - 1)
    }

    fun deleteAction(actionId: String) {
        val index = list.indexOfFirst { item ->
            item.fullname == actionId
        }
        if (index != -1) {
            list.removeAt(index)
            notifyItemRemoved(index)
        }
    }


    override fun getItemCount(): Int = list.size
}
