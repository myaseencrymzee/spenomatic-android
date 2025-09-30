package com.crymzee.drivetalk.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class RoundedCornerImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatImageView(context, attrs, defStyle) {

    private val path = Path()
    private val paint = Paint()

    // Radius for the rounded corners
    private var cornerRadius: Float = 50f // Adjust this value as needed

    init {
        paint.isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas) {
        val width = width.toFloat()
        val height = height.toFloat()

        // Create a path with rounded corners
        path.reset()
        path.moveTo(0f, height) // Start at the bottom left corner
        path.lineTo(0f, 0f) // Move to the top left corner
        path.lineTo(width, 0f) // Move to the top right corner
        path.lineTo(width, height) // Move to the bottom right corner

        // Create rounded corners at the bottom left and bottom right
        path.lineTo(
            width,
            height - cornerRadius
        ) // Move to the point before the bottom right corner
        path.arcTo(
            RectF(width - cornerRadius * 2, height - cornerRadius * 2, width, height),
            0f,
            90f
        ) // Bottom right corner
        path.lineTo(cornerRadius, height) // Move to the point before the bottom left corner
        path.arcTo(
            RectF(0f, height - cornerRadius * 2, cornerRadius * 2, height),
            90f,
            90f
        ) // Bottom left corner
        path.close() // Close the path

        canvas.clipPath(path) // Clip the canvas to the path
        super.onDraw(canvas) // Draw the image
    }

    private val externalWatchers = mutableListOf<TextWatcher>()

    fun addTextChangedListener(watcher: TextWatcher) {
        externalWatchers.add(watcher)
    }
    fun setCornerRadius(radius: Float) {
        cornerRadius = radius
        invalidate() // Redraw the view with the new corner radius
    }
}