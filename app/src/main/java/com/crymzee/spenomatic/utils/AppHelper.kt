package com.example.flowit.utils


import android.app.Activity
import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.Display
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.math.RoundingMode
import java.text.DecimalFormat


class AppHelper {

    companion object {

        fun makeInnerStringColored(
            shouldUnderline: Boolean,
            string: String,
            from: Int,
            to: Int,
            context: Context,
            colorId: Int
        ): Spannable {

            val spannable: Spannable = SpannableString(string)
            if (shouldUnderline) spannable.setSpan(UnderlineSpan(), from, to, 0)
            spannable.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(context, colorId)),
                from,
                to,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            return spannable
        }

        fun EditText.focus() {
            requestFocus()
            setSelection(length())
        }

        fun getFirstWord(input: String): String {
            var firstWord = input
            if (firstWord.contains(" ")) {
                return firstWord.substring(0, firstWord.indexOf(" "))
            }
            return input
        }


        fun addChar(url: String?, position: Int, str: String): String? {
            if (url.isNullOrEmpty()) {
                return " "
            }
            val sb = StringBuilder(url)
            sb.insert(position, str)
            return sb.toString()
        }

        fun slideUp(view: View) {
            view.visibility = View.VISIBLE
//            val animate = TranslateAnimation(
//                0F,  // fromXDelta
//                0F,  // toXDelta
//                view.height.toFloat(),  // fromYDelta
//                0F
//            ) // toYDelta
//            animate.duration = 500
//            animate.fillAfter = true
//            view.startAnimation(animate)
        }

        // slide the view from its current position to below itself
        fun slideDown(view: View) {
            view.visibility = View.GONE
//            val animate = TranslateAnimation(
//                0F,  // fromXDelta
//                0F,  // toXDelta
//                0F,  // fromYDelta
//                view.height.toFloat()
//            ) // toYDelta
//            animate.duration = 500
//            animate.fillAfter = true
//            view.startAnimation(animate)
        }


        fun isNullOrEmpty(str: String?): Boolean {
            return !(str != null && !str.isEmpty())
        }

        fun isNullOrEmpty(list: List<Any>?): Boolean {
            return !(list != null && !list.isEmpty())
        }

        fun getDayNumber(day: String): Int {
            when (day) {
                "monday" -> return 0
                "tuesday" -> return 1
                "wednesday" -> return 2
                "thursday" -> return 3
                "friday" -> return 4
                "saturday" -> return 5
                "sunday" -> return 6

            }
            return 0
        }


        fun toTwoDecimal(number: Double): String {
            val df = DecimalFormat("#.###")
            df.roundingMode = RoundingMode.CEILING
            return df.format(number)
        }

        //This funtion is to reduce the code of line for initialization of layout manager
        fun getLayoutManager(activity: Activity): RecyclerView.LayoutManager {
            var layoutManager = LinearLayoutManager(activity)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            return layoutManager
        }

        //This funtion is to reduce the code of line for initialization of layout manager
        fun getLayoutManager(context: Context): RecyclerView.LayoutManager {
            var layoutManager = LinearLayoutManager(context)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            return layoutManager
        }

        fun getLinearLayoutManager(context: Context): LinearLayoutManager {
            var layoutManager = LinearLayoutManager(context)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            return layoutManager
        }

        fun getHorizontalLayoutManager(context: Context): RecyclerView.LayoutManager {
            var layoutManager = LinearLayoutManager(context)
            layoutManager.orientation = LinearLayoutManager.HORIZONTAL
            return layoutManager
        }


        fun get_span_count(activity: Activity): Int {
            val display: Display = activity.windowManager.defaultDisplay
            val outMetrics = DisplayMetrics()
            display.getMetrics(outMetrics)

            val density: Float = activity.resources.displayMetrics.density
            val dpWidth = outMetrics.widthPixels / density
            val columns = Math.round(dpWidth / 300)
            return columns
        }

        fun dpToPx(context: Context, valueInDp: Float): Float {
            val metrics = context.resources.displayMetrics
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics)
        }


        /*This method receives bitmap and create intent to share the bitmap file to other applications

         */


        fun getMimeType(url: String): String? {
            var type: String? = null
            val extension = MimeTypeMap.getFileExtensionFromUrl(url)
            if (extension != null) {
                type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            }
            return type
        }
    }


}