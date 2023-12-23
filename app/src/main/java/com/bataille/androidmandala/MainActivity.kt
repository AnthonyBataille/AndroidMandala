package com.bataille.androidmandala

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.widget.SeekBar
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bataille.androidmandala.databinding.ActivityMainBinding
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.min


object Constants {
    const val BASE_DEFAULT = 10
    const val MULTIPLIER_DEFAULT = 2
}

class SharedViewModel : ViewModel() {
    var multiplier = 0
    var base = 0
}

class DrawableView : View {
    // Paint for the main circle
    private val mainCirclePaint: Paint = Paint().apply {
        color = Color.YELLOW
        style = Paint.Style.STROKE
        strokeWidth = 3.0f
        isAntiAlias = true
    }

    // Paint for dots on the main circle
    private val smallCirclePaint: Paint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    // Paint the lines connecting dots
    private val linePaint: Paint = Paint().apply {
        color = Color.WHITE
        isAntiAlias = true
    }
    private var centerX = 0.0f
    private var centerY = 0.0f

    private var radius = 0.0f
    private var smallRadius = 0.0f

    private var base = Constants.BASE_DEFAULT // Number of dots on the main circle
    private var multiplier = Constants.MULTIPLIER_DEFAULT // Multiplier that will define the connected dots

    private val pointsCoordinates: MutableList<FloatArray> =
        mutableListOf() // List of coordinates of the dots on the main circle

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    // When size is changed (or during initialization) update the dimensions of the view
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val xPadding = (paddingLeft + paddingRight).toFloat()
        val yPadding = (paddingTop + paddingBottom).toFloat()

        val ww = w.toFloat() - xPadding
        val hh = h.toFloat() - yPadding

        centerX = ww / 2.0f
        centerY = hh / 2.0f

        radius = min(ww, hh) * 0.40f
        smallRadius = 0.03f * radius
    }

    private fun genPointsCoordinates() {
        if (base > 0) {
            pointsCoordinates.clear()
            for (i in 0..<base) {
                val pointX = centerX + cos(PI - 2.0 * PI * i / base).toFloat() * radius
                val pointY = centerY + sin(PI - 2.0 * PI * i / base).toFloat() * radius
                pointsCoordinates.add(floatArrayOf(pointX, pointY))
            }
        }
    }

    fun updateData(viewModel: SharedViewModel) {
        multiplier = viewModel.multiplier
        base = viewModel.base
        genPointsCoordinates()
    }

    // Compute data for first drawing of mandala at startup
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        centerX = width / 2.0f
        centerY = height / 2.0f
        radius = min(width, height).toFloat() * 0.40f
        smallRadius = 0.03f * radius
        genPointsCoordinates()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Color background in black
        canvas.drawColor(Color.BLACK)
        // Draw main circle
        canvas.drawCircle(centerX, centerY, radius, mainCirclePaint)

        if (base > 0) {
            // Draw dots on the main circle
            for (point in pointsCoordinates) {
                val pointX = point[0]
                val pointY = point[1]
                canvas.drawCircle(pointX, pointY, smallRadius, smallCirclePaint)
            }
        }
        if (multiplier > 0 && base > 0) {
            // Connects the dots based on the modulo rule and the multiplier
            for (i in 0..<base) {
                val startPointX = pointsCoordinates[i][0]
                val startPointY = pointsCoordinates[i][1]
                val endPointX = pointsCoordinates[i * multiplier % base][0]
                val endPointY = pointsCoordinates[i * multiplier % base][1]
                canvas.drawLine(startPointX, startPointY, endPointX, endPointY, linePaint)
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: SharedViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        // The viewModel contains the base and the multiplier which are shared to other classes
        viewModel = ViewModelProvider(this).get(SharedViewModel::class.java)
        viewModel.base = Constants.BASE_DEFAULT
        viewModel.multiplier = Constants.MULTIPLIER_DEFAULT
        binding.viewModel = viewModel

        binding.seekBarBase.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            // When the progress bar is changed, update the value of base and update the drawable view
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                viewModel.base = progress
                binding.drawableViewMain.updateData(viewModel)
                binding.drawableViewMain.invalidate()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        binding.seekBarMultiplier.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            // When the multiplier bar is changed, update the value of multiplier and update the drawable view
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                viewModel.multiplier = progress
                binding.drawableViewMain.updateData(viewModel)
                binding.drawableViewMain.invalidate()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        setContentView(binding.root)
    }
}
