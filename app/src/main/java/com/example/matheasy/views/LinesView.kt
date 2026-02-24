package com.example.matheasy

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat

class LineView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private val paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.fons)
        strokeWidth = 8f
        style = Paint.Style.STROKE
    }

    private val lines = mutableListOf<Pair<Pair<Float, Float>, Pair<Float, Float>>>()

    fun addLine(start: Pair<Float, Float>, end: Pair<Float, Float>) {
        lines.add(Pair(start, end))
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (line in lines) {
            val (start, end) = line
            canvas.drawLine(start.first, start.second, end.first, end.second, paint)
        }
    }
}