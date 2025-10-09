package com.crymzee.spenomatic.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.crymzee.spenomatic.databinding.ItemLocalVisitListBinding
import com.crymzee.spenomatic.databinding.ItemOustationVisitListBinding
import com.crymzee.spenomatic.model.response.expenses.Visit
import java.text.SimpleDateFormat
import java.util.Locale

class LocalVisitListAdapter :
    RecyclerView.Adapter<LocalVisitListAdapter.VisitViewHolder>() {

    private val visitList = mutableListOf<Visit>()

    fun submitList(data: List<Visit>?) {
        visitList.clear()
        data?.let { visitList.addAll(it) }
        Log.d("listItem", "Nested submitList: ${visitList.size} visits")
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisitViewHolder {
        val binding = ItemLocalVisitListBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VisitViewHolder(binding)
    }

    override fun getItemCount() = visitList.size

    override fun onBindViewHolder(holder: VisitViewHolder, position: Int) {
        Log.d("listItem", "Nested binding ${position + 1}/${visitList.size}")
        holder.bind(visitList[position])
    }

    inner class VisitViewHolder(private val binding: ItemLocalVisitListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(visit: Visit) {
            val customerName = visit.visit.customer.fullname
            val transportLocations =
                visit.transport_expenses?.joinToString(", ") { "${it.from_location} to ${it.to_location}" } ?: "-"
            val miscellaneous =
                visit.miscellaneous_expenses?.joinToString(", ") { it.objective } ?: "-"

            binding.tvClientsToVisit.text = customerName
            binding.tvTransport.text = transportLocations
            binding.tvMisc.text = miscellaneous
        }
    }
}

