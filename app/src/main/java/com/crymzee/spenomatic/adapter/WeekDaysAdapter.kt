package com.crymzee.spenomatic.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.crymzee.spenomatic.R
import com.crymzee.spenomatic.databinding.ItemDayCardBinding
import com.crymzee.spenomatic.model.WeekDay
import com.crymzee.spenomatic.model.response.attendenceList.Data
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class WeekDaysAdapter(
    private val days: List<WeekDay>,
    private val attendanceMap: Map<String, Data>
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
        val today = Calendar.getInstance()

        val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val itemDateStr = apiDateFormat.format(item.calendar.time)
        val attendance = attendanceMap[itemDateStr]

        with(holder.binding) {
            tvDay.text = item.dayName
            tvDate.text = item.dateLabel

            // Default style
            mainContainer.setCardBackgroundColor(Color.WHITE)
            tvDate.setTextColor(Color.parseColor("#84878C"))
            tvDay.setTextColor(Color.parseColor("#343434"))
            ivStatus.visibility = View.GONE

            val isToday = item.calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    item.calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)

            val isFuture = item.calendar.after(today)

            when {
                // 🔵 Rule 1: Current day
                isToday -> {
                    mainContainer.setCardBackgroundColor(context.getColor(R.color.theme_blue))
                    tvDate.setTextColor(Color.WHITE)
                    tvDay.setTextColor(Color.WHITE)
                    ivStatus.visibility = View.VISIBLE
                    ivStatus.setImageResource(R.drawable.ic_present)
                }

                // ⚪ Rule 3: Future date
                isFuture -> {
                    mainContainer.setCardBackgroundColor(Color.WHITE)
                    tvDate.setTextColor(Color.parseColor("#84878C"))
                    tvDay.setTextColor(Color.parseColor("#343434"))
                    ivStatus.visibility = View.GONE
                }

                // 🔴 Rule 2: check_in null
                attendance != null && attendance.check_in.isNullOrEmpty() -> {
                    mainContainer.setCardBackgroundColor(Color.parseColor("#FF0101"))
                    tvDate.setTextColor(Color.WHITE)
                    tvDay.setTextColor(Color.WHITE)
                    ivStatus.visibility = View.VISIBLE
                    ivStatus.setImageResource(R.drawable.ic_absent_attendance)
                }

                // ✅ Otherwise, day has valid check_in (present)
                attendance != null -> {
                    mainContainer.setCardBackgroundColor(Color.parseColor("#17C726")) // green
                    tvDate.setTextColor(Color.WHITE)
                    tvDay.setTextColor(Color.WHITE)
                    ivStatus.visibility = View.VISIBLE
                    ivStatus.setImageResource(R.drawable.ic_present)
                }
            }
        }
    }

    override fun getItemCount(): Int = days.size
}

