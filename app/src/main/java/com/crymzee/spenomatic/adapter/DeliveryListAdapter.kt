package com.crymzee.spenomatic.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.crymzee.spenomatic.R
import com.crymzee.spenomatic.databinding.ItemDeliveryListBinding
import com.crymzee.spenomatic.model.response.delivery.Data
import com.crymzee.spenomatic.utils.hide
import com.crymzee.spenomatic.utils.visible
import java.util.Locale

class DeliveryListAdapter(val context: Context) :
    RecyclerView.Adapter<DeliveryListAdapter.RecyclerViewHolder>() {


    var list = mutableListOf<Data>()

    var lastSelectedItem = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        RecyclerViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_delivery_list,
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

    fun addPaginatedDataToList(data1: List<Data>) {
        val uniqueItems = data1.filterNot { newItem ->
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

            labelHello.text = data.customer?.fullname ?:""

            tvLastVisit.text = data.customer?.contacts?.firstOrNull()?.phone?:""

            tvFrequency.text = data.address
            tvTotalVisit.text = formatDateToDDMMYYYY(data.expected_date)

            btnDetail.setOnClickListener {
                data.let { it1 -> visitId?.invoke(it1) }
            }
            if(data.status == "delivered"){
                btnDetail.hide()
            }else{
                btnDetail.visible()
            }


        }

        holder.binding.executePendingBindings()
    }


    fun formatDateToDDMMYYYY(inputDate: String): String {
        return try {
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = java.text.SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val date = inputFormat.parse(inputDate)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            inputDate // return original if parsing fails
        }
    }
    class RecyclerViewHolder(val binding: ItemDeliveryListBinding) :
        RecyclerView.ViewHolder(binding.root)

    private var visitId: ((Data) -> Unit)? = null

    fun getVisitId(listener: (Data) -> Unit) {
        visitId = listener
    }

}
