package com.crymzee.spenomatic.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.crymzee.spenomatic.R
import com.crymzee.spenomatic.databinding.ItemCustomerVisitListBinding
import com.crymzee.spenomatic.model.request.pendingVisits.Data
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CustomerVisitListAdapter(private val context: Context) :
    RecyclerView.Adapter<CustomerVisitListAdapter.RecyclerViewHolder>() {

    private val list = mutableListOf<Data>()

    // -1 means no selection yet
    private var selectedPosition: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        RecyclerViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_customer_visit_list,
                parent,
                false
            )
        )

    override fun getItemCount(): Int = list.size

    /**
     * Replace list and auto-select first item if available
     */
    fun setData(data: List<Data>) {
        list.clear()
        list.addAll(data)

        if (list.isNotEmpty()) {
            selectedPosition = 0
            notifyDataSetChanged()
            itemClick?.invoke(list[0]) // auto trigger first item
        } else {
            selectedPosition = -1
            notifyDataSetChanged()
        }
    }

    /**
     * Append only unique items (pagination support)
     */
    fun addPaginatedData(data: List<Data>) {
        val uniqueItems = data.filterNot { newItem ->
            list.any { it.id == newItem.id }
        }
        val start = list.size
        list.addAll(uniqueItems)
        notifyItemRangeInserted(start, uniqueItems.size)

        // Auto-select first if nothing is selected
        if (selectedPosition == -1 && list.isNotEmpty()) {
            selectedPosition = 0
            notifyItemChanged(0)
            itemClick?.invoke(list[0])
        }
    }

    fun getSelectedItem(): Data? {
        return if (selectedPosition in list.indices) list[selectedPosition] else null
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val item = list[position]
        holder.binding.apply {
            labelLeaveType.text = item.customer.fullname
            labelLeaveDate.text = item.customer.address

            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            val parsedDate: Date? = inputFormat.parse(item.schedule_date)
            tvVisitDate.text = parsedDate?.let { outputFormat.format(it) } ?: item.schedule_date

            // ✅ highlight selection
            root.background = if (position == selectedPosition) {
                ContextCompat.getDrawable(context, R.drawable.bg_rounded_12_blue_outline)
            } else {
                ContextCompat.getDrawable(context, R.drawable.bg_rounded_12)
            }

            ivDelete.setOnClickListener {
                visitId?.invoke(item.id)
            }

            root.setOnClickListener {
                val currentPos = holder.bindingAdapterPosition
                if (currentPos != RecyclerView.NO_POSITION) {
                    val previousPos = selectedPosition
                    selectedPosition = currentPos

                    if (previousPos != -1) notifyItemChanged(previousPos)
                    notifyItemChanged(currentPos)

                    itemClick?.invoke(list[currentPos])
                }
            }
        }
    }

    class RecyclerViewHolder(val binding: ItemCustomerVisitListBinding) :
        RecyclerView.ViewHolder(binding.root)

    // callbacks
    private var visitId: ((Int) -> Unit)? = null
    private var itemClick: ((Data) -> Unit)? = null

    fun setOnDeleteClick(listener: (Int) -> Unit) {
        visitId = listener
    }

    fun setOnItemClick(listener: (Data) -> Unit) {
        itemClick = listener
    }

    fun deleteAction(actionId: Int) {
        val index = list.indexOfFirst { it.id == actionId }
        if (index != -1) {
            list.removeAt(index)
            notifyItemRemoved(index)

            // If deleted item was selected → reset to first
            if (index == selectedPosition) {
                selectedPosition = if (list.isNotEmpty()) 0 else -1
                notifyDataSetChanged()
                if (selectedPosition != -1) {
                    itemClick?.invoke(list[0])
                }
            }
        }
    }
}
