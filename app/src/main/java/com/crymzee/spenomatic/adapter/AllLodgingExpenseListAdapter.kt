package com.crymzee.spenomatic.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.crymzee.spenomatic.databinding.ItemLodgingListBinding
import com.crymzee.spenomatic.model.request.createOutsideExpense.LodgingBoardingExpense
import java.text.SimpleDateFormat
import java.util.Locale

class AllLodgingExpenseListAdapter(
    private val list: MutableList<LodgingBoardingExpense>,
) : RecyclerView.Adapter<AllLodgingExpenseListAdapter.FriendViewHolder>() {

    inner class FriendViewHolder(val binding: ItemLodgingListBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val binding = ItemLodgingListBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FriendViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val item = list[position]
        holder.binding.apply {
            labelLeaveType.text = formatRangeDate(item.from_date, item.to_date)
            labelNights.text = "${item.nights_stayed}"
            tvAmountPerDay.text = "Amount/day: \$${item.per_night_amount}"
            tvTotal.text = "Total: \$${item.total_amount}"
            ivDelete.setOnClickListener {
                item.nights_stayed.let { name -> visitId?.invoke(name) }

            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun formatRangeDate(fromDate: String, toDate: String): String {
        return try {
            // Incoming format: 2025-10-03
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            // Desired format: Oct 3
            val outputFormat = SimpleDateFormat("MMM d", Locale.getDefault())

            val from = inputFormat.parse(fromDate)
            val to = inputFormat.parse(toDate)

            if (from != null && to != null) {
                "${outputFormat.format(from)} - ${outputFormat.format(to)}"
            } else {
                "$fromDate - $toDate"
            }
        } catch (e: Exception) {
            "$fromDate - $toDate"
        }
    }



    private var visitId: ((String) -> Unit)? = null

    fun getVisitId(listener: (String) -> Unit) {
        visitId = listener
    }


    fun deleteAction(actionId: String) {
        val index = list.indexOfFirst { item ->
            item.nights_stayed == actionId
        }
        if (index != -1) {
            list.removeAt(index)
            notifyItemRemoved(index)
        }
    }
    override fun getItemCount(): Int = list.size
}
