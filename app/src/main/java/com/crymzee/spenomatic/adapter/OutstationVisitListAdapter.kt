package com.crymzee.spenomatic.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.crymzee.spenomatic.databinding.ItemOustationVisitListBinding
import com.crymzee.spenomatic.model.response.expenses.Visit
import java.text.SimpleDateFormat
import java.util.Locale

class OutstationVisitListAdapter :
    RecyclerView.Adapter<OutstationVisitListAdapter.VisitViewHolder>() {

    private val visitList = mutableListOf<Visit>()
    private val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val outputFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
    private val dateTimeInput = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val dateTimeOutput = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())

    fun submitList(data: List<Visit>?) {
        visitList.clear()
        if (!data.isNullOrEmpty()) visitList.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisitViewHolder {
        val binding = ItemOustationVisitListBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VisitViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VisitViewHolder, position: Int) {
        holder.bind(visitList[position])
    }

    override fun getItemCount(): Int = visitList.size

    inner class VisitViewHolder(private val binding: ItemOustationVisitListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(visit: Visit) = with(binding) {
            // ✅ Clients
            tvLastVisit.text = visit.visit?.customer?.fullname ?: "N/A"

            // ✅ Transport
            tvFrequency.text = visit.transport_expenses
                ?.takeIf { it.isNotEmpty() }
                ?.joinToString(", ") { exp ->
                    listOfNotNull(exp.from_location, exp.to_location)
                        .joinToString(" to ")
                } ?: "No Transport Info"

            // ✅ Miscellaneous
            tvTotalVisit.text = visit.miscellaneous_expenses
                ?.takeIf { it.isNotEmpty() }
                ?.joinToString(", ") { it.objective ?: "" }
                ?.ifEmpty { "No Miscellaneous" } ?: "No Miscellaneous"

            // ✅ Lodging & Boarding
            tvMeterReading.text = visit.lodging_boarding_expenses?.firstOrNull()?.let {
                val from = it.from_date?.let { d -> inputFormat.parse(d) }?.let(outputFormat::format)
                val to = it.to_date?.let { d -> inputFormat.parse(d) }?.let(outputFormat::format)
                if (from != null && to != null) "$from - $to" else "No Lodging Info"
            } ?: "No Lodging Info"

            // ✅ Bus/Train Timing
            tvVichelNo.text = visit.bus_train_expenses?.mapNotNull { exp ->
                val dateTime = "${exp.date} ${exp.time}"
                runCatching { dateTimeInput.parse(dateTime) }
                    .getOrNull()
                    ?.let(dateTimeOutput::format)
            }?.joinToString(", ") ?: "No Bus/Train Info"
        }
    }
}

