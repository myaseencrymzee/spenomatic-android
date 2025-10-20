package com.crymzee.spenomatic.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.crymzee.spenomatic.R
import com.crymzee.spenomatic.databinding.ItemScheduleListBinding
import com.crymzee.spenomatic.model.response.visitsList.Data
import com.crymzee.spenomatic.utils.toCamelCase
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class ScheduleListAdapter(val context: Context) :
    RecyclerView.Adapter<ScheduleListAdapter.RecyclerViewHolder>() {


    var list = mutableListOf<Data>()

    var lastSelectedItem = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        RecyclerViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_schedule_list,
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

    fun addPaginatedDataToList(data: List<Data>) {
        val uniqueItems = data.filterNot { newItem ->
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

            labelHello.text = data.customer.fullname
            labelExplore.text = data.customer.industry_type

            tvLastVisit.text = formatRelativeDate(data.customer.latest_visit_date)

            tvAvgVisit.text = "${data.customer.avg_visit_time} min"

            tvFrequency.text = data.customer.visit_frequency.toCamelCase()
            tvTotalVisit.text = data.customer.number_of_visits.toString()

            btnViewDetail.setOnClickListener {
                data.let { it1 -> visitId?.invoke(it1.id, it1.customer.id) }
            }


            btnCheckIn.setOnClickListener {
                if (data.status == "pending") {
                    data.let { it1 -> visitIdCheckIn?.invoke(it1.id) }
                } else if (data.status == "checked_in") {
                    data.let { it1 -> visitIdCheckOut?.invoke(it1.id) }
                }

            }
            when (data.status) {
                "checked_in" -> {
                    btnCheckIn.setImageResource(R.drawable.btn_check_outs)
                }

                "pending" -> {
                    btnCheckIn.setImageResource(R.drawable.btn_check_in)
                }

                "visited" -> {
                    btnCheckIn.setImageResource(R.drawable.btn_visiteds)
                }
            }

            when (data.type) {
                "sales" -> {
                    ivNotifications.setImageResource(R.drawable.type_sales)
                }

                "service" -> {
                    ivNotifications.setImageResource(R.drawable.types_service)
                }
            }
        }

        holder.binding.executePendingBindings()
    }
    @SuppressLint("SimpleDateFormat")
    fun formatRelativeDate(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return ""

        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(dateString) ?: return ""

            val now = System.currentTimeMillis()
            val diff = now - date.time
            val days = TimeUnit.MILLISECONDS.toDays(diff)

            when {
                days < 0 -> {
                    // Future date — show "X days ahead"
                    val aheadDays = kotlin.math.abs(days)
                    if (aheadDays == 1L) "Tomorrow"
                    else "$aheadDays days ahead"
                }
                days < 1 -> "Today"
                days == 1L -> "Yesterday"
                else -> "$days days ago"
            }
        } catch (e: Exception) {
            ""
        }
    }



    class RecyclerViewHolder(val binding: ItemScheduleListBinding) :
        RecyclerView.ViewHolder(binding.root)

    private var visitId: ((Int, Int) -> Unit)? = null

    fun getVisitId(listener: (Int, Int) -> Unit) {
        visitId = listener
    }

    private var visitIdCheckIn: ((Int) -> Unit)? = null

    fun getVisitIdCheckIn(listener: (Int) -> Unit) {
        visitIdCheckIn = listener
    }

    private var visitIdCheckOut: ((Int) -> Unit)? = null

    fun getVisitIdCheckOut(listener: (Int) -> Unit) {
        visitIdCheckOut = listener
    }
}
