package com.crymzee.spenomatic.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.crymzee.spenomatic.R
import com.crymzee.spenomatic.databinding.ItemLeavesListBinding
import com.crymzee.spenomatic.model.response.allLeaves.Data

class LeaveListAdapter(val context: Context) :
    RecyclerView.Adapter<LeaveListAdapter.RecyclerViewHolder>() {


    var list = mutableListOf<Data>()

    var lastSelectedItem = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        RecyclerViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_leaves_list,
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

    fun clearList(data1: List<Data>) {
        this.list.clear()
        this.list.addAll(data1)
        this.notifyDataSetChanged()
    }

    fun addAll(followers: List<Data>) {
        this.list.clear()
        this.list.addAll(followers)
        notifyDataSetChanged()
    }


    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val data = list[position]
        val mContext = holder.binding.root


        holder.binding.apply {
            if(data.type == "half_day"){
                labelLeaveType.text = "Half Day"
            }else if(data.type == "full_day"){
                labelLeaveType.text = "Full Day"
            }
            labelLeaveDate.text = "${data.start_date} - ${data.end_date}"

            when (data.status) {
                "pending" -> {
                    icLeaveStatus.setImageResource(R.drawable.ic_pending)
                }

                "approved" -> {
                    icLeaveStatus.setImageResource(R.drawable.ic_approved)
                }

                "rejected" -> {
                    icLeaveStatus.setImageResource(R.drawable.ic_rejceted)
                }
            }
        }

        holder.binding.executePendingBindings()
    }


    class RecyclerViewHolder(val binding: ItemLeavesListBinding) :
        RecyclerView.ViewHolder(binding.root)


}