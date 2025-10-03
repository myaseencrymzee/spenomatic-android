package com.crymzee.spenomatic.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.crymzee.spenomatic.databinding.ItemCustomerListBinding
import com.crymzee.spenomatic.databinding.ItemCustomerVisitListBinding
import com.crymzee.spenomatic.model.request.VisitModelRequest
import com.crymzee.spenomatic.model.request.pendingVisits.Data
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CustomerVisitListAdapter(
    private val list: MutableList<VisitModelRequest>,   // ✅ make mutable
    private val onViewDetailClick: (String) -> Unit
) : RecyclerView.Adapter<CustomerVisitListAdapter.FriendViewHolder>() {

    inner class FriendViewHolder(val binding: ItemCustomerVisitListBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val binding = ItemCustomerVisitListBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FriendViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val item = list[position]
        holder.binding.apply {
            labelLeaveType.text = item.name
            labelLeaveDate.text = item.address
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

            val parsedDate: Date? = inputFormat.parse(item.date)
            tvVisitDate.text = parsedDate?.let { outputFormat.format(it) } ?: item.date
            tvVisitRemark.text = item.remark
            ivDelete.setOnClickListener {
                item.id.let { name -> visitId?.invoke(name) }
            }
        }


    }
    private var visitId: ((Int) -> Unit)? = null

    fun getVisitId(listener: (Int) -> Unit) {
        visitId = listener
    }


    fun deleteAction(actionId: Int) {
        val index = list.indexOfFirst { item ->
            item.id == actionId
        }
        if (index != -1) {
            list.removeAt(index)
            notifyItemRemoved(index)
        }
    }
    override fun getItemCount(): Int = list.size
}
