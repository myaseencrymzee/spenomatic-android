package com.crymzee.spenomatic.utils

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.crymzee.spenomatic.R


class CurvedBottomNavDrawable(
    private val context: Context,
    private val curvePosition: Int
) : Drawable() {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.bottom_nav_background) // main nav color
        style = Paint.Style.FILL
    }

    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.grey_e3e3e3) // << your grey color here
        style = Paint.Style.FILL
    }

    private val path = Path()
    private val curveRadius = 90f
    private val curveCircleRadius = 120f

    override fun draw(canvas: Canvas) {
        val width = bounds.width().toFloat()
        val height = bounds.height().toFloat()
        val tabWidth = width / 5

        val centerX = tabWidth * curvePosition + tabWidth / 2
        val centerY = curveCircleRadius / 1.5f

        // Draw full bottom nav background
        path.reset()
        path.moveTo(0f, 0f)
        path.lineTo(width, 0f)
        path.lineTo(width, height)
        path.lineTo(0f, height)
        path.close()

        canvas.drawPath(path, paint)

        // Draw grey circle for the curved bump
        canvas.drawCircle(centerX, centerY, curveCircleRadius, circlePaint)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
        circlePaint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
        circlePaint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int = PixelFormat.OPAQUE
}
