package com.crymzee.spenomatic.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.crymzee.spenomatic.R
import com.crymzee.spenomatic.databinding.ItemDayCardBinding
import com.crymzee.spenomatic.model.WeekDay
import java.util.Calendar

class WeekDaysAdapter(
    private val days: List<WeekDay>
) : RecyclerView.Adapter<WeekDaysAdapter.WeekDayViewHolder>() {

    inner class WeekDayViewHolder(val binding: ItemDayCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeekDayViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemDayCardBinding.inflate(inflater, parent, false)
        return WeekDayViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WeekDayViewHolder, position: Int) {
        val item = days[position]
        val context = holder.binding.root.context

        with(holder.binding) {
            tvDay.text = item.dayName
            tvDate.text = item.dateLabel

            // ✅ Example status/color logic (customize per day)
            when (item.calendar.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> {
                    mainContainer.setCardBackgroundColor(Color.parseColor("#FF0101"))
                    tvDate.setTextColor(Color.WHITE)
                    tvDay.setTextColor(Color.WHITE)
                    ivStatus.visibility = View.VISIBLE
                    ivStatus.setImageResource(R.drawable.ic_off)
                }

                Calendar.TUESDAY -> {
                    mainContainer.setCardBackgroundColor(Color.parseColor("#17C726"))
                    tvDate.setTextColor(Color.WHITE)
                    tvDay.setTextColor(Color.WHITE)
                    ivStatus.visibility = View.VISIBLE
                    ivStatus.setImageResource(R.drawable.ic_on)
                }

                Calendar.WEDNESDAY -> {
                    mainContainer.setCardBackgroundColor(Color.parseColor("#17C726"))
                    tvDate.setTextColor(Color.WHITE)
                    tvDay.setTextColor(Color.WHITE)
                    ivStatus.visibility = View.VISIBLE
                    ivStatus.setImageResource(R.drawable.ic_on)
                }

                Calendar.THURSDAY -> {
                    mainContainer.setCardBackgroundColor(Color.parseColor("#3E53AD"))
                    tvDate.setTextColor(Color.WHITE)
                    tvDay.setTextColor(Color.WHITE)
                    ivStatus.visibility = View.VISIBLE
                    ivStatus.setImageResource(R.drawable.ic_on)
                }

                else -> {
                    tvDate.setTextColor(Color.parseColor("#84878C"))
                    tvDay.setTextColor(Color.parseColor("#343434"))
                    mainContainer.setCardBackgroundColor(Color.WHITE)
                    ivStatus.visibility = View.GONE
                }
            }

            // ✅ Highlight today
            val today = Calendar.getInstance()
            if (item.calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) &&
                item.calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)
            ) {
                mainContainer.setCardBackgroundColor(context.getColor(R.color.theme_blue))
                tvDate.setTextColor(Color.WHITE)
                tvDay.setTextColor(Color.WHITE)
                ivStatus.visibility = View.VISIBLE
            }
        }
    }

    override fun getItemCount(): Int = days.size
}
