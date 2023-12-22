package com.example.androidmandala

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.activity.ComponentActivity
import com.example.androidmandala.databinding.ActivityMainBinding



class DrawableView : View {
    private val mainCirclePaint : Paint = Paint().apply {
        color = Color.YELLOW
        style = Paint.Style.STROKE
        strokeWidth = 3.0f
        isAntiAlias = true
    }
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs : AttributeSet?) : super(context, attrs)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerX = width / 2.0f
        val centerY = height / 2.0f

        val radius = width * 0.40f

        canvas.drawColor(Color.BLACK)
        canvas.drawCircle(centerX, centerY, radius, mainCirclePaint)
    }
}


class MainActivity : ComponentActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
