package com.crymzee.spenomatic.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.crymzee.spenomatic.R
import com.crymzee.spenomatic.databinding.ItemDropDownClientBinding
import com.crymzee.spenomatic.model.response.allCustomers.Data
import com.crymzee.spenomatic.utils.hide
import com.crymzee.spenomatic.utils.visible

class DropDownAdapterClient(private val context: Context) :
    RecyclerView.Adapter<DropDownAdapterClient.RecyclerViewHolder>() {

    private var list = mutableListOf<Data>()
    private var clientType: ((Data) -> Unit)? = null
    private var categoryId: ((Data) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        RecyclerViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_drop_down_client,
                parent,
                false
            )
        )

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val itemModel = list[position]

        holder.binding.tvName.text = itemModel.fullname

        // Hide divider for the last item
        if (position == list.size - 1) {
            holder.binding.layoutDivider.hide()
        } else {
            holder.binding.layoutDivider.visible()
        }

        holder.binding.root.setOnClickListener {
            clientType?.invoke(itemModel)
            categoryId?.invoke(itemModel)
        }

        holder.binding.executePendingBindings()
    }

    fun clearList() {
        list.clear()
        notifyDataSetChanged()
    }

    fun addAll(newList: List<Data>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }


    fun addMore(newItems: List<Data>) {
        val uniqueItems = newItems.filterNot { newItem ->
            list.any { it.id == newItem.id }
        }
        val start = list.size
        list.addAll(uniqueItems)
        notifyItemRangeInserted(start, uniqueItems.size)
    }

    fun getClientType(listener: (Data) -> Unit) {
        clientType = listener
    }

    fun getCategoryId(listener: (Data) -> Unit) {
        categoryId = listener
    }

    class RecyclerViewHolder(val binding: ItemDropDownClientBinding) :
        RecyclerView.ViewHolder(binding.root)
}
