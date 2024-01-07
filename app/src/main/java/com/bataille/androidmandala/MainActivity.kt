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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.bataille.androidmandala.databinding.ActivityMainBinding
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.min


object Constants {
    const val MONTH_DEFAULT = 10
    const val DAY_DEFAULT = 2
    const val AGE_DEFAULT = 20
    const val NUM_POINTS_MIN_VALUE = 20
    const val NUM_POINTS_MAX_VALUE = 60
}

fun hash(value: Int, minValue: Int, maxValue: Int): Int {
    var res = value
        .and(0xFF)
        .xor(0xAA)
    res = res % (maxValue - minValue) + minValue
    return res
}

class SharedViewModel : ViewModel() {
    private val _day = MutableLiveData<Int>(Constants.DAY_DEFAULT)
    private val _month = MutableLiveData<Int>(Constants.MONTH_DEFAULT)
    private val _age = MutableLiveData<Int>(Constants.AGE_DEFAULT)

    val day: LiveData<Int>
        get() = _day
    val month: LiveData<Int>
        get() = _month

    val age: LiveData<Int>
        get() = _age

    fun updateDay(newValue: Int) {
        _day.value = newValue
    }

    fun updateMonth(newValue: Int) {
        _month.value = newValue
    }

    fun updateAge(newValue: Int) {
        _age.value = newValue
    }
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

    private var month: Int? = Constants.MONTH_DEFAULT // Number of dots on the main circle
    private var day: Int? =
        Constants.DAY_DEFAULT // Multiplier that will define the connected dots
    private var age: Int? = Constants.AGE_DEFAULT
    private var numPoints: Int =
        age ?: ((Constants.NUM_POINTS_MIN_VALUE + Constants.NUM_POINTS_MAX_VALUE) / 2)

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
        if (numPoints > 0) {
            pointsCoordinates.clear()
            for (i in 0..<numPoints) {
                val pointX = centerX + cos(PI - 2.0 * PI * i / numPoints).toFloat() * radius
                val pointY = centerY + sin(PI - 2.0 * PI * i / numPoints).toFloat() * radius
                pointsCoordinates.add(floatArrayOf(pointX, pointY))
            }
        }
    }

    fun updateData(viewModel: SharedViewModel) {
        day = viewModel.day.value
        month = viewModel.month.value
        age = viewModel.age.value
        numPoints = age ?: ((Constants.NUM_POINTS_MIN_VALUE + Constants.NUM_POINTS_MAX_VALUE) / 2)
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

        val daySafe = day ?: 0
        val monthSafe = month ?: 0

        // Draw dots on the main circle
        for (point in pointsCoordinates) {
            val pointX = point[0]
            val pointY = point[1]
            canvas.drawCircle(pointX, pointY, smallRadius, smallCirclePaint)
        }

        val targetPoint = daySafe * monthSafe
        // Connects the dots based on the modulo rule and the multiplier
        for (i in 0..<numPoints) {
            val startPointX = pointsCoordinates[i][0]
            val startPointY = pointsCoordinates[i][1]
            val endPointX = pointsCoordinates[(i * targetPoint) % numPoints][0]
            val endPointY = pointsCoordinates[(i * targetPoint) % numPoints][1]
            canvas.drawLine(startPointX, startPointY, endPointX, endPointY, linePaint)
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
        viewModel.month.observe(this, Observer { newValue ->
            binding.textViewMonthValue.text = "$newValue"
        })
        viewModel.day.observe(this, Observer { newValue ->
            binding.textViewDayValue.text = "$newValue"
        })
        viewModel.age.observe(this, Observer { newValue ->
            binding.textViewAgeValue.text = "$newValue"
        })
        binding.viewModel = viewModel

        binding.seekBarMonth.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            // When the progress bar is changed, update the value of base and update the drawable view
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                viewModel.updateMonth(progress)
                binding.drawableViewMain.updateData(viewModel)
                binding.drawableViewMain.invalidate()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        binding.seekBarDay.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            // When the multiplier bar is changed, update the value of multiplier and update the drawable view
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                viewModel.updateDay(progress)
                binding.drawableViewMain.updateData(viewModel)
                binding.drawableViewMain.invalidate()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        binding.seekBarAge.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            // When the multiplier bar is changed, update the value of multiplier and update the drawable view
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                viewModel.updateAge(progress)
                binding.drawableViewMain.updateData(viewModel)
                binding.drawableViewMain.invalidate()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        setContentView(binding.root)
    }
}
