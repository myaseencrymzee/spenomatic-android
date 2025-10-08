package com.crymzee.spenomatic.adapter

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.crymzee.spenomatic.R
import com.crymzee.spenomatic.databinding.ItemOutCityCustomerVisitListBinding
import com.crymzee.spenomatic.model.request.pendingVisits.Data
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OutCityCustomerVisitListAdapter(private val context: Context) :
    RecyclerView.Adapter<OutCityCustomerVisitListAdapter.RecyclerViewHolder>() {

    private val list = mutableListOf<Data>()
    private val objectiveMap = mutableMapOf<Int, String>() // objective per visit
    private var selectedPosition: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        RecyclerViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_out_city_customer_visit_list,
                parent,
                false
            )
        )

    override fun getItemCount(): Int = list.size

    fun getSelectedItem(): Data? = list.getOrNull(selectedPosition)

    fun setData(data: List<Data>) {
        list.clear()
        list.addAll(data)
        Log.d("OutCityAdapter", "setData called with ${data.size} items")

        selectedPosition = if (list.isNotEmpty()) 0 else -1
        notifyDataSetChanged()

        if (selectedPosition != -1) {
            itemClick?.invoke(list[0])
        }
    }

    fun addPaginatedData(data: List<Data>) {
        val uniqueItems = data.filterNot { newItem ->
            list.any { it.id == newItem.id }
        }
        val start = list.size
        list.addAll(uniqueItems)
        notifyItemRangeInserted(start, uniqueItems.size)

        if (selectedPosition == -1 && list.isNotEmpty()) {
            selectedPosition = 0
            notifyItemChanged(0)
            itemClick?.invoke(list[0])
        }
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val item = list[position]
        val binding = holder.binding

        binding.labelLeaveType.text = item.customer.fullname
        binding.labelLeaveDate.text = item.customer.address

        // Format date safely
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        val parsedDate: Date? = runCatching { inputFormat.parse(item.schedule_date) }.getOrNull()
        binding.tvVisitDate.text = parsedDate?.let { outputFormat.format(it) } ?: item.schedule_date

        // === Prevent duplicate text watcher callbacks ===
        val oldWatcher = binding.etEmail.getTag(R.id.tag_text_watcher) as? TextWatcher
        if (oldWatcher != null) binding.etEmail.removeTextChangedListener(oldWatcher)

        // Restore objective
        val savedObjective = objectiveMap[item.id] ?: ""
        if (binding.etEmail.text.toString() != savedObjective) {
            binding.etEmail.setText(savedObjective)
        }

        // === New TextWatcher with dynamic position lookup ===
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val pos = holder.bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val currentItem = list[pos]
                    objectiveMap[currentItem.id] = s.toString()

                    if (pos == selectedPosition) {
                        typeObject?.invoke(s.toString())
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        binding.etEmail.addTextChangedListener(watcher)
        binding.etEmail.setTag(R.id.tag_text_watcher, watcher)

        binding.ivDelete.setOnClickListener {
            visitId?.invoke(item.id)
        }

        // Highlight selected
        binding.root.background = if (position == selectedPosition) {
            ContextCompat.getDrawable(context, R.drawable.bg_rounded_12_blue_outline)
        } else {
            ContextCompat.getDrawable(context, R.drawable.bg_rounded_12)
        }

        binding.root.setOnClickListener {
            val currentPos = holder.bindingAdapterPosition
            if (currentPos != RecyclerView.NO_POSITION) {
                val previousPos = selectedPosition
                selectedPosition = currentPos

                if (previousPos != -1 && previousPos != currentPos) notifyItemChanged(previousPos)
                notifyItemChanged(currentPos)

                itemClick?.invoke(list[currentPos])
            }
        }
    }


    class RecyclerViewHolder(val binding: ItemOutCityCustomerVisitListBinding) :
        RecyclerView.ViewHolder(binding.root)

    // === Callbacks ===
    private var visitId: ((Int) -> Unit)? = null
    private var itemClick: ((Data) -> Unit)? = null
    private var typeObject: ((String) -> Unit)? = null

    fun setOnDeleteClick(listener: (Int) -> Unit) { visitId = listener }
    fun setOnItemClick(listener: (Data) -> Unit) { itemClick = listener }
    fun getTypeObject(listener: (String) -> Unit) { typeObject = listener }

    fun deleteAction(actionId: Int) {
        val index = list.indexOfFirst { it.id == actionId }
        if (index != -1) {
            list.removeAt(index)
            objectiveMap.remove(actionId)
            notifyItemRemoved(index)

            if (index == selectedPosition) {
                selectedPosition = if (list.isNotEmpty()) 0 else -1
                notifyDataSetChanged()
                if (selectedPosition != -1) itemClick?.invoke(list[0])
            }
        }
    }

    fun setSelectedVisit(visitId: Int, objective: String?) {
        if (!objective.isNullOrEmpty()) {
            objectiveMap[visitId] = objective
        }

        val index = list.indexOfFirst { it.id == visitId }
        if (index != -1) {
            val prevPos = selectedPosition
            selectedPosition = index
            if (prevPos != -1 && prevPos != index) notifyItemChanged(prevPos)
            notifyItemChanged(index)
        }
    }

    fun getObjectiveForVisit(id: Int): String = objectiveMap[id] ?: ""
    fun setObjectiveForVisit(id: Int, text: String) {
        objectiveMap[id] = text
        val index = list.indexOfFirst { it.id == id }
        if (index != -1) notifyItemChanged(index)
    }
    fun getAllObjectives(): Map<Int, String> = objectiveMap.toMap()
}
