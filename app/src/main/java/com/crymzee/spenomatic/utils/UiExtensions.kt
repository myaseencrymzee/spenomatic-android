package com.crymzee.spenomatic.utils

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.SystemClock
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.crymzee.spenomatic.R
import com.crymzee.spenomatic.databinding.ToastErrorBannerBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import com.crymzee.spenomatic.databinding.ConfirmationPopupBinding
import com.crymzee.spenomatic.databinding.ToastSuccessBannerBinding

fun View.visible(isVisible: Boolean) {
    visibility = if (isVisible) View.VISIBLE else View.GONE
}

fun Fragment.goBack() = findNavController().popBackStack()

fun Fragment.goBackWithResult(key: String, value: Any) {
    findNavController().previousBackStackEntry
        ?.savedStateHandle
        ?.set(key, value)
    findNavController().popBackStack()
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun EditText.clearError() {
    error = null
}

fun Fragment.navigateTo(directions: NavDirections) = findNavController().navigate(directions)


fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

fun View.enabled() {
    isEnabled = true
}

fun View.disabled() {
    isEnabled = false

}

fun View.setSingleClickListener(interval: Long = 600L, onSingleClick: (View) -> Unit) {
    var lastClickTime = 0L
    setOnClickListener { v ->
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime >= interval) {
            lastClickTime = currentTime
            onSingleClick(v)
        }
    }
}

//fun Activity.showErrorPopup(
//    heading: String? = null,
//    description: String,
//) {
//    val binding: ToastErrorBannerBinding = DataBindingUtil.inflate(
//        layoutInflater,
//        R.layout.toast_error_banner,
//        null,
//        false
//    )
//
//    // If heading is null or blank → fallback to "Error"
//    binding.tvDescription.text = heading?.takeIf { it.isNotBlank() } ?: "Error"
//    binding.descriptionLabel.text = description
//
//    val dialog = AlertDialog.Builder(this)
//        .setView(binding.root)
//        .setCancelable(false)
//        .create()
//
//    // Handle dismiss
//    binding.btnAdd.setOnClickListener {
//        dialog.dismiss()
//    }
//
//    dialog.show()
//}

fun Activity.showErrorPopup(
    heading: String? = null,
    description: String,
    onConfirm: (() -> Unit)? = null
) {
    val binding: ToastErrorBannerBinding = DataBindingUtil.inflate(
        layoutInflater,
        R.layout.toast_error_banner,
        null,
        false
    )

    // Apply heading + description
    binding.tvDescription.text = heading?.takeIf { it.isNotBlank() } ?: "Error!"
    binding.descriptionLabel.text = description

    val dialog = AlertDialog.Builder(this)
        .setView(binding.root)
        .setCancelable(false)
        .create()

    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

    // Cancel button
    binding.btnAdd.setOnClickListener {
        dialog.dismiss()
    }

    // OK button
    binding.btnAdd.setOnClickListener {
        dialog.dismiss()
        Handler(Looper.getMainLooper()).postDelayed({
            onConfirm?.invoke()
        }, 300)
    }

    dialog.show()
}


fun Fragment.showErrorPopup(
    context: Context,
    heading: String? = null,
    description: String,
    onConfirm: (() -> Unit)? = null
) {
    val binding: ToastErrorBannerBinding = DataBindingUtil.inflate(
        layoutInflater,
        R.layout.toast_error_banner,
        null,
        false
    )

    // Apply heading + description
    binding.tvDescription.text = heading?.takeIf { it.isNotBlank() } ?: "Error!"
    binding.descriptionLabel.text = description

    val dialog = AlertDialog.Builder(context)
        .setView(binding.root)
        .setCancelable(false)
        .create()

    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

    // Cancel button
    binding.btnAdd.setOnClickListener {
        dialog.dismiss()
    }

    // OK button
    binding.btnAdd.setOnClickListener {
        dialog.dismiss()
        Handler(Looper.getMainLooper()).postDelayed({
            onConfirm?.invoke()
        }, 300)
    }

    dialog.show()
}



fun Activity.showSuccessPopup(
    heading: String? = null,
    description: String,
    onConfirm:  () -> Unit
) {
    val binding: ToastSuccessBannerBinding = DataBindingUtil.inflate(
        layoutInflater,
        R.layout.toast_success_banner,
        null,
        false
    )

    // Apply heading + description
    binding.tvDescription.text = heading?.takeIf { it.isNotBlank() } ?: "Error!"
    binding.descriptionLabel.text = description

    val dialog = AlertDialog.Builder(this)
        .setView(binding.root)
        .setCancelable(false)
        .create()

    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

    // Cancel button
    binding.btnAdd.setOnClickListener {
        dialog.dismiss()
    }

    // OK button
    binding.btnAdd.setOnClickListener {
        dialog.dismiss()
        Handler(Looper.getMainLooper()).postDelayed({
            onConfirm?.invoke()
        }, 300)
    }

    dialog.show()
}


fun Fragment.showSuccessPopup(
    context: Context,
    heading: String? = null,
    description: String,
    onConfirm: () -> Unit
) {
    val binding: ToastSuccessBannerBinding = DataBindingUtil.inflate(
        layoutInflater,
        R.layout.toast_success_banner,
        null,
        false
    )

    binding.tvDescription.text = heading?.takeIf { it.isNotBlank() } ?: "Success!"
    binding.descriptionLabel.text = description

    val dialog = AlertDialog.Builder(context)
        .setView(binding.root)
        .setCancelable(false)
        .create()

    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


    // OK button
    binding.btnAdd.setOnClickListener {
        dialog.dismiss()
        Handler(Looper.getMainLooper()).postDelayed({
            onConfirm()
        }, 300)
    }

    dialog.show()
}



fun Fragment.confirmationPopUp(
    context: Context,
    heading: String? = null,
    description: String,
    icon: Int,
    onConfirm:  () -> Unit
) {
    val binding: ConfirmationPopupBinding = DataBindingUtil.inflate(
        layoutInflater,
        R.layout.confirmation_popup,
        null,
        false
    )

    // Apply heading + description
    binding.tvDescription.text = heading?.takeIf { it.isNotBlank() } ?: "Confirmation"
    binding.descriptionLabel.text = description
    binding.titleTv.setImageResource(icon)

    val dialog = AlertDialog.Builder(context)
        .setView(binding.root)
        .setCancelable(false)
        .create()

    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

    // Cancel button
    binding.btnNo.setOnClickListener {
        dialog.dismiss()
    }

    // OK button
    binding.btnYes.setOnClickListener {
        dialog.dismiss()
        Handler(Looper.getMainLooper()).postDelayed({
            onConfirm.invoke()
        }, 300)
    }

    dialog.show()
}



fun RecyclerView.horizontalLayout() {
    layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
}

fun RecyclerView.horizontalReverseLayout() {
    layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, true)
}

fun RecyclerView.verticalLayout() {
    layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)
}

fun RecyclerView.verticalReverseLayout() {
    layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, true)
}

fun RecyclerView.gridLayout(spanCount: Int) {
    layoutManager = GridLayoutManager(this.context, spanCount)
}

fun RecyclerView.gridLayoutHorizontal(spanCount: Int) {
    layoutManager = GridLayoutManager(this.context, spanCount)
    (layoutManager as GridLayoutManager).orientation = LinearLayoutManager.HORIZONTAL

}

fun <T : ViewDataBinding> T.executeWithAction(action: T.() -> Unit) {
    action()
    executePendingBindings()
}

fun EditText.acceptNamesOnly() {
    val regex = Regex("[a-zA-Z .]")
    filters = arrayOf(InputFilter { source, _, _, _, _, _ ->
        source.filter { regex.matches(it.toString()) }
    })
}

fun EditText.acceptNumOnly(length: Int = Int.MAX_VALUE) {
    val regex = Regex("[0-9]")
    filters = arrayOf(InputFilter { source, _, _, _, _, _ ->

        source.filter { regex.matches(it.toString()) }
    }, LengthFilter(length))

}

fun TextInputEditText.acceptNamesOnly() {
    val regex = Regex("[a-zA-Z .]")
    filters = arrayOf(InputFilter { source, _, _, _, _, _ ->
        source.filter { regex.matches(it.toString()) }
    })
}


fun View.setOnSingleClickListener(debounceTime: Long = 4000L, action: () -> Unit) {
    this.setOnClickListener(object : View.OnClickListener {
        private var lastClickTime: Long = 0

        override fun onClick(v: View) {
            val timeNow = SystemClock.elapsedRealtime()
            val elapsedTimeSinceLastClick = timeNow - lastClickTime
            if (elapsedTimeSinceLastClick < debounceTime) return
            else action()

            lastClickTime = SystemClock.elapsedRealtime()
        }
    })
}

/*fun TextView.animateText(textList: List<String>, handler: Handler) {
    var count = 0
    var mIndex = 0
    try {


        handler.post(object : Runnable {
            override fun run() {
                text = "Search for " + textList[count].subSequence(0, mIndex++)

                val text = textList[count]
                if (mIndex <= text.length) {
                    handler.postDelayed(this, 200)
                } else {

                    handler.postDelayed(this, 1000)

                    if (count <= textList.size) {
                        mIndex = 0
                        count++
                        if (count == textList.size) {
                            count = 0
                        }
                    }

                }


            }

        })
    } catch (e: Exception) {
        HwLogger.logErrorMsg("ANIMATETEXT", "Animate Text: ${e.message}")
    }
}*/

fun MaterialTextView.clipboard(label: String) {
    val clipboardManager =
        this.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clipData = ClipData.newPlainText(label, text)
    clipboardManager.setPrimaryClip(clipData)
}

fun TextView.clipboard(label: String) {
    val clipboardManager =
        this.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clipData = ClipData.newPlainText(label, text)
    clipboardManager.setPrimaryClip(clipData)
}

/*

fun TextView.nonDecimal(value: Double = 0.0) {
    if ((value).equals(0.0)) {
        text = this.context.resources.getString(R.string.int_price_formatter, 0)
    } else {
        text = this.context.resources.getString(R.string.float_price_formatter, value)

    }
}
*/

fun TextView.setString(format: Int, value: Any?) {
    this.context.resources.getString(format, value)
}


fun Fragment.showToast(msg: String, length: Int = Toast.LENGTH_SHORT) {
    activity?.showToast(msg, length)
}

fun Activity.showToast(msg: String, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, msg, length).show()
}


fun ImageView.load(imageUrl: Uri) {
    Glide.with(this).load(imageUrl)
        .into(this)
}

fun EditText.getQueryTextChangeStateFlow(): StateFlow<String> {

    val query = MutableStateFlow("")

    addTextChangedListener(afterTextChanged = {
        query.value = it.toString()

    })

    return query

}












