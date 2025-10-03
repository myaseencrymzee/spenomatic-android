package com.crymzee.spenomatic.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.crymzee.spenomatic.databinding.ItemOutCityCustomerVisitListBinding
import com.crymzee.spenomatic.model.request.VisitModelRequest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OutCityCustomerVisitListAdapter(
    private val list: MutableList<VisitModelRequest>,
) : RecyclerView.Adapter<OutCityCustomerVisitListAdapter.FriendViewHolder>() {

    inner class FriendViewHolder(val binding: ItemOutCityCustomerVisitListBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val binding = ItemOutCityCustomerVisitListBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FriendViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val item = list[position]
        holder.binding.apply {
            labelLeaveType.text = item.name
            labelLeaveDate.text = item.address
            ivDelete.setOnClickListener {
                item.name.let { name -> visitId?.invoke(name) }
            }
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

            val parsedDate: Date? = inputFormat.parse(item.date)
            tvVisitDate.text = parsedDate?.let { outputFormat.format(it) } ?: item.date
            tvVisitRemark.text = item.remark
            // Clear EditText whenever new data is bound
            etEmail.setText("")
            // Remove old listeners to prevent multiple triggers
            etEmail.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // Send typed text to typeObject callback
                    typeObject?.invoke(s.toString())
                }

                override fun afterTextChanged(s: Editable?) {}
            })

        }

    }

    private var visitId: ((String) -> Unit)? = null

    fun getVisitId(listener: (String) -> Unit) {
        visitId = listener
    }


    private var typeObject: ((String) -> Unit)? = null

    fun getTypeObject(listener: (String) -> Unit) {
        typeObject = listener
    }


    fun deleteAction(actionId: String) {
        val index = list.indexOfFirst { item ->
            item.name == actionId
        }
        if (index != -1) {
            list.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    override fun getItemCount(): Int = list.size
}
