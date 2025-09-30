package com.crymzee.spenomatic.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.crymzee.spenomatic.R
import com.crymzee.spenomatic.databinding.ItemDropDownAddServiceBinding
import com.crymzee.spenomatic.model.DropDownClientType
import com.crymzee.spenomatic.utils.hide
import com.crymzee.spenomatic.utils.visible

class AddServiceDropDownAdapter(val context: Context) :
    RecyclerView.Adapter<AddServiceDropDownAdapter.RecyclerViewHolder>() {


    var list = mutableListOf<DropDownClientType>()

    var lastSelectedItem = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        RecyclerViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_drop_down_add_service,
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


    fun clearList() {
        this.list.clear()
        this.notifyDataSetChanged()
    }

    fun addAll(list: List<DropDownClientType>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val itemModel = list[position]

        holder.binding.tvName.text = String.format(" %s", itemModel.name)
        if (position == itemCount - 1) {
            // If it is the last item, hide the divider
            holder.binding.layoutDivider.hide()
        } else {
            // For other items, show the divider
            holder.binding.layoutDivider.visible()
        }
        holder.binding.root.setOnClickListener {
            itemModel.name.let { it1 -> clientType?.invoke(it1) }
        }

        holder.binding.executePendingBindings()
    }


    class RecyclerViewHolder(val binding: ItemDropDownAddServiceBinding) :
        RecyclerView.ViewHolder(binding.root)

    private var clientType: ((String) -> Unit)? = null

    fun getClientType(listener: (String) -> Unit) {
        clientType = listener
    }
}
