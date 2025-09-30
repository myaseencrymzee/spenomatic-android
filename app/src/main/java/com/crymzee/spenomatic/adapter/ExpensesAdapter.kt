package com.crymzee.spenomatic.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.crymzee.spenomatic.R
import java.text.SimpleDateFormat
import java.util.Locale
import com.crymzee.spenomatic.databinding.ItemFuelVoucherBinding
import com.crymzee.spenomatic.databinding.ItemLocalSalesVisitBinding
import com.crymzee.spenomatic.databinding.ItemOtherLunchExpensesBinding
import com.crymzee.spenomatic.databinding.ItemOutstationVisitBinding
import com.crymzee.spenomatic.model.response.expenses.Data

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
            TYPE_LOCAL -> LocalSalesViewHolder(ItemLocalSalesVisitBinding.inflate(inflater, parent, false))
            TYPE_OUTSTATION -> OutstationViewHolder(ItemOutstationVisitBinding.inflate(inflater, parent, false))
            TYPE_OTHER -> OtherViewHolder(ItemOtherLunchExpensesBinding.inflate(inflater, parent, false))
            else -> throw IllegalArgumentException("Invalid view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = list[position]
        Log.d("ExpensesAdapter", "Binding item: id=${item.id}, type=${item.type}, amount=${item.amount}")

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
            binding.tvMeterReading.text = item.fuel_voucher_details?.start_meter_reading.toString()
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
        fun bind(item: Data) {
            binding.tvPrice.text = "\$${item.amount}"

            val customerNames = item.customers
                ?.mapNotNull { it.customer?.fullname }
                ?.joinToString(", ")
                ?: "No Customers"

            val transportLocations = item.transport_expenses
                ?.mapNotNull { it.to_location }
                ?.joinToString(", ")
                ?: "No Locations"

            val miscellaneous = item.miscellaneous_expenses
                ?.mapNotNull { it.objective }
                ?.joinToString(", ")
                ?: "No Miscellaneous"

            binding.tvMeterReading.text = customerNames
            binding.tvVichelNo.text = transportLocations
            binding.tvFuelKts.text = miscellaneous

            when (item.status) {
                "pending" -> binding.ivNotifications.setImageResource(R.drawable.ic_pending)
                "approved" -> binding.ivNotifications.setImageResource(R.drawable.ic_approved)
                "rejected" -> binding.ivNotifications.setImageResource(R.drawable.ic_rejceted)
            }
        }
    }

    class OutstationViewHolder(private val binding: ItemOutstationVisitBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val dateTimeInput = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        private val dateTimeOutput = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
        private val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        private val outputFormat = SimpleDateFormat("MMM dd", Locale.getDefault())

        fun bind(item: Data) {
            val customerNames = item.customers
                ?.mapNotNull { it.customer?.fullname }
                ?.joinToString(", ")
                ?: "No Customers"

            val transportLocations = item.transport_expenses
                ?.mapNotNull { it.to_location }
                ?.joinToString(", ")
                ?: "No Locations"

            val miscellaneous = item.miscellaneous_expenses
                ?.mapNotNull { it.objective }
                ?.joinToString(", ")
                ?: "No Miscellaneous"

            val lodgingRange = item.lodging_boarding_expenses
                ?.firstOrNull()
                ?.let {
                    val from = it.from_date?.let { d: String -> inputFormat.parse(d) }?.let(outputFormat::format)
                    val to = it.to_date?.let { d: String -> inputFormat.parse(d) }?.let(outputFormat::format)
                    if (from != null && to != null) "$from - $to" else null
                } ?: "No Lodging"

            val busTimings = item.bus_train_expenses
                ?.mapNotNull { exp ->
                    val dateTimeString = "${exp.date} ${exp.time}"
                    dateTimeInput.parse(dateTimeString)?.let { parsed ->
                        dateTimeOutput.format(parsed)
                    }
                }
                ?.joinToString(", ")
                ?: "No Bus/Train"

            binding.tvPrice.text = "\$${item.amount}"
            binding.tvLastVisit.text = customerNames
            binding.tvFrequency.text = transportLocations
            binding.tvTotalVisit.text = miscellaneous
            binding.tvMeterReading.text = lodgingRange
            binding.tvVichelNo.text = busTimings

            when (item.status) {
                "pending" -> binding.ivNotifications.setImageResource(R.drawable.ic_pending)
                "approved" -> binding.ivNotifications.setImageResource(R.drawable.ic_approved)
                "rejected" -> binding.ivNotifications.setImageResource(R.drawable.ic_rejceted)
            }
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
