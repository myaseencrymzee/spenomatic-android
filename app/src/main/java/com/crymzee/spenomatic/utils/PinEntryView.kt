package com.crymzee.spenomatic.utils

import android.content.Context
import android.graphics.Typeface
import android.text.*
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.crymzee.spenomatic.R

class PinEntryView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val pinLength = 4
    private val editTexts = mutableListOf<EditText>()
    private val digits = CharArray(pinLength) { ' ' }

    private var textColor: Int = ContextCompat.getColor(context, R.color.black)

    var onPinComplete: ((String) -> Unit)? = null

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER // center all boxes in parent

        context.theme.obtainStyledAttributes(attrs, R.styleable.PinEntryView, 0, 0).apply {
            try {
                textColor = getColor(
                    R.styleable.PinEntryView_pinTextColor,
                    ContextCompat.getColor(context, R.color.black)
                )
            } finally {
                recycle()
            }
        }

        setupPinBoxes()
    }

    private fun setupPinBoxes() {
        for (i in 0 until pinLength) {
            val editText = createPinBox(i)
            editTexts.add(editText)
            addView(editText)
        }
    }

    private fun createPinBox(index: Int): EditText {
        val editText = EditText(context).apply {
            layoutParams = LayoutParams(dpToPx(50), dpToPx(50)).apply {
                marginEnd = if (index < pinLength - 1) dpToPx(12) else 0
            }

            background = ContextCompat.getDrawable(context, R.drawable.pin_box_bg)
            filters = arrayOf(InputFilter.LengthFilter(1))
            inputType = InputType.TYPE_CLASS_NUMBER
            imeOptions = EditorInfo.IME_ACTION_NEXT
            textAlignment = TEXT_ALIGNMENT_CENTER
            textSize = 18f
            setTypeface(typeface, Typeface.BOLD)
            isCursorVisible = false
            hint = "*" // placeholder
            setTextColor(textColor)
            gravity = Gravity.CENTER

            setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN &&
                    keyCode == KeyEvent.KEYCODE_DEL &&
                    text.isEmpty() &&
                    index > 0
                ) {
                    editTexts[index - 1].apply {
                        setText("")
                        digits[index - 1] = ' '
                        requestFocus()
                    }
                    true
                } else {
                    false
                }
            }
        }

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrEmpty()) {
                    val inputChar = s.last()
                    digits[index] = inputChar

                    if (index < pinLength - 1) {
                        editTexts[index + 1].requestFocus()
                    }
                    checkCompletion()
                    notifyTextWatchers()
                }
            }
        }

        editText.addTextChangedListener(textWatcher)
        return editText
    }

    private fun checkCompletion() {
        val pin = getPin()
        if (pin.length == pinLength && !pin.contains(' ')) {
            onPinComplete?.invoke(pin)
        }
    }

    fun clearPin() {
        digits.fill(' ')
        editTexts.forEach { it.setText("") }
        editTexts.firstOrNull()?.requestFocus()
    }

    fun getPin(): String {
        return String(digits).replace(" ", "")
    }

    // Custom text watcher support
    private val externalWatchers = mutableListOf<TextWatcher>()

    fun addTextChangedListener(watcher: TextWatcher) {
        externalWatchers.add(watcher)
    }

    private fun notifyTextWatchers() {
        val currentPin = getPin()
        val editable = Editable.Factory.getInstance().newEditable(currentPin)
        externalWatchers.forEach {
            it.afterTextChanged(editable)
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }
}
