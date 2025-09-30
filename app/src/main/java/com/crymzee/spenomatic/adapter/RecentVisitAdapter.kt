package com.crymzee.spenomatic.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.crymzee.spenomatic.R
import com.crymzee.spenomatic.databinding.ItemRecentVisitBinding
import com.crymzee.spenomatic.databinding.ItemScheduleListBinding
import com.crymzee.spenomatic.model.response.visitsList.Data


class RecentVisitAdapter(val context: Context) :
    RecyclerView.Adapter<RecentVisitAdapter.RecyclerViewHolder>() {


    var list = mutableListOf<Data>()

    var lastSelectedItem = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        RecyclerViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_recent_visit,
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
            labelLeaveType.text =data.schedule_date
            when (data.type) {
                "sales" -> {
                    icLeaveStatus.setImageResource(R.drawable.type_sales)
                }

                "service" -> {
                    icLeaveStatus.setImageResource(R.drawable.types_service)
                }
            }
        }

        holder.binding.executePendingBindings()
    }


    class RecyclerViewHolder(val binding: ItemRecentVisitBinding) :
        RecyclerView.ViewHolder(binding.root)

}