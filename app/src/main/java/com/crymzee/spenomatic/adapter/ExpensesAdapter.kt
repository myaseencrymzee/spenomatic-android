package com.crymzee.spenomatic.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crymzee.spenomatic.R
import com.crymzee.spenomatic.databinding.ItemFuelVoucherBinding
import com.crymzee.spenomatic.databinding.ItemLocalSalesVisitBinding
import com.crymzee.spenomatic.databinding.ItemOtherLunchExpensesBinding
import com.crymzee.spenomatic.databinding.ItemOutstationVisitBinding
import com.crymzee.spenomatic.model.response.expenses.Data
import java.util.Locale

class ExpensesAdapter(val context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var list = mutableListOf<Data>()

    companion object {
        private const val TYPE_FUEL = 0
        private const val TYPE_LOCAL = 1
        private const val TYPE_OUTSTATION = 2
        private const val TYPE_OTHER = 3
    }

    override fun getItemViewType(position: Int): Int {
        val type = list[position].type
        Log.d("ExpensesAdapter", "getItemViewType: pos=$position type=$type")
        return when (type) {
            "fuel_voucher" -> TYPE_FUEL
            "local_visit" -> TYPE_LOCAL
            "outstation_sales" -> TYPE_OUTSTATION
            "other" -> TYPE_OTHER
            else -> TYPE_OTHER // fallback
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        Log.e("ExpensesAdapter", "onCreateViewHolder: viewType=$viewType")

        return when (viewType) {
            TYPE_FUEL -> FuelViewHolder(ItemFuelVoucherBinding.inflate(inflater, parent, false))
            TYPE_LOCAL -> LocalSalesViewHolder(
                ItemLocalSalesVisitBinding.inflate(
                    inflater,
                    parent,
                    false
                )
            )

            TYPE_OUTSTATION -> OutstationViewHolder(
                ItemOutstationVisitBinding.inflate(
                    inflater,
                    parent,
                    false
                )
            )

            TYPE_OTHER -> OtherViewHolder(
                ItemOtherLunchExpensesBinding.inflate(
                    inflater,
                    parent,
                    false
                )
            )

            else -> throw IllegalArgumentException("Invalid view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = list[position]
        Log.d(
            "ExpensesAdapter",
            "Binding item: id=${item.id}, type=${item.type}, amount=${item.amount}"
        )

        when (holder) {
            is FuelViewHolder -> holder.bind(item)
            is LocalSalesViewHolder -> holder.bind(item)
            is OutstationViewHolder -> holder.bind(item)
            is OtherViewHolder -> holder.bind(item)
        }
    }

    override fun getItemCount(): Int = list.size

    // ---- Pagination & Data Methods ----
    fun addPaginatedDataToList(data: List<Data>) {
        val uniqueItems = data.filterNot { newItem ->
            list.any { it.id == newItem.id }
        }
        val start = list.size
        list.addAll(uniqueItems)
        notifyItemRangeInserted(start, uniqueItems.size)
    }

    fun clearList(data: List<Data>) {
        Log.d("ExpensesAdapter", "clearList called with size=${data.size}")

        this.list.clear()
        this.list.addAll(data)
        Log.d("ExpensesAdapter", "list updated with size=${list.size}")

        notifyDataSetChanged()
    }

    fun addAll(data: List<Data>) {
        list.clear()
        list.addAll(data)
        notifyDataSetChanged()
    }

    // ---- ViewHolders ----
    class FuelViewHolder(private val binding: ItemFuelVoucherBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Data) {
            binding.tvLastVisit.text = "\$${item.amount}"
            binding.tvFrequency.text = item.fuel_voucher_details?.fuel_type
            binding.tvTotalVisit.text = item.fuel_voucher_details?.till_number
            binding.tvAvgVisit.text = "${item.fuel_voucher_details?.km_travelled.toString()} KM"
            binding.tvMeterReading.text = "${item.fuel_voucher_details?.start_meter_reading.toString()} - ${item.fuel_voucher_details?.end_meter_reading.toString()}"
            binding.tvVichelNo.text = item.fuel_voucher_details?.vehicle_number
            binding.tvFuelKts.text = "${item.fuel_voucher_details?.fuel_in_liters.toString()} ltrs"

            when (item.status) {
                "pending" -> binding.ivNotifications.setImageResource(R.drawable.ic_pending)
                "approved" -> binding.ivNotifications.setImageResource(R.drawable.ic_approved)
                "rejected" -> binding.ivNotifications.setImageResource(R.drawable.ic_rejceted)
            }
        }
    }

    class LocalSalesViewHolder(private val binding: ItemLocalSalesVisitBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val visitAdapter = LocalVisitListAdapter()

        init {
            binding.rvVisitMap.apply {
                layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                adapter = visitAdapter
                setHasFixedSize(false)
                isNestedScrollingEnabled = false
                overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            }
        }

        fun bind(item: Data) {
            binding.tvVisitSubtitle.text = "\$${item.amount}"

            when (item.status.lowercase()) {
                "pending" -> binding.ivVisitStatus.setImageResource(R.drawable.ic_pending)
                "approved" -> binding.ivVisitStatus.setImageResource(R.drawable.ic_approved)
                "rejected" -> binding.ivVisitStatus.setImageResource(R.drawable.ic_rejceted)
            }

            val visits = item.visits ?: emptyList()

            // ✅ This fixes the "first item only shows 1" issue
            binding.rvVisitMap.post {
                visitAdapter.submitList(visits)
            }
        }
    }




    class OutstationViewHolder(private val binding: ItemOutstationVisitBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val visitAdapter = OutstationVisitListAdapter()

        init {
            binding.rvLeaves.adapter = visitAdapter
        }

        fun bind(item: Data) = with(binding) {
            tvPrice.text = "\$${item.amount ?: 0}"

            // ✅ Safe status handling
            val statusIcon = when (item.status.lowercase(Locale.getDefault())) {
                "pending" -> R.drawable.ic_pending
                "approved" -> R.drawable.ic_approved
                "rejected" -> R.drawable.ic_rejceted
                else -> R.drawable.ic_pending
            }
            ivNotifications.setImageResource(statusIcon)

            // ✅ Populate nested visit list
            visitAdapter.submitList(item.visits ?: emptyList())
        }
    }



    class OtherViewHolder(private val binding: ItemOtherLunchExpensesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Data) {
            binding.tvPrice.text = "\$${item.amount}"

            when (item.status) {
                "pending" -> binding.ivNotifications.setImageResource(R.drawable.ic_pending)
                "approved" -> binding.ivNotifications.setImageResource(R.drawable.ic_approved)
                "rejected" -> binding.ivNotifications.setImageResource(R.drawable.ic_rejceted)
            }
        }
    }
}
