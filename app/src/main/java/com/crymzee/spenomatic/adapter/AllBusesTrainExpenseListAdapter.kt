package com.crymzee.spenomatic.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.crymzee.spenomatic.databinding.ItemBusTrainListBinding
import com.crymzee.spenomatic.model.request.createOutsideExpense.BusTrainExpense
import java.text.SimpleDateFormat
import java.util.Locale

class AllBusesTrainExpenseListAdapter(
    private val list: MutableList<BusTrainExpense>,
) : RecyclerView.Adapter<AllBusesTrainExpenseListAdapter.FriendViewHolder>() {

    inner class FriendViewHolder(val binding: ItemBusTrainListBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val binding = ItemBusTrainListBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FriendViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val item = list[position]
        holder.binding.apply {

            // Convert date (yyyy-MM-dd) -> Oct 1
            val inputDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputDateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
            val formattedDate = try {
                inputDateFormat.parse(item.date)?.let { outputDateFormat.format(it) } ?: item.date
            } catch (e: Exception) {
                item.date
            }

            // Convert time (HH:mm) -> 06:00 PM
            val inputTimeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val outputTimeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val formattedTime = try {
                inputTimeFormat.parse(item.time)?.let { outputTimeFormat.format(it) } ?: item.time
            } catch (e: Exception) {
                item.time
            }

            // Final string -> "Oct 1, 06:00 PM"
            labelLeaveType.text = "$formattedDate, $formattedTime"

            tvAmount.text = "Total: \$${item.amount}"

            ivDelete.setOnClickListener {
                item.date.let { name -> visitId?.invoke(name) }
            }
        }
    }


    private var visitId: ((String) -> Unit)? = null

    fun getVisitId(listener: (String) -> Unit) {
        visitId = listener
    }


    fun deleteAction(actionId: String) {
        val index = list.indexOfFirst { item ->
            item.date == actionId
        }
        if (index != -1) {
            list.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    override fun getItemCount(): Int = list.size
}
