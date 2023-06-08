package com.example.serverttest

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.serverttest.webSocket.WebSocketManager
import java.util.*
import kotlin.math.*

private var isJoystickPressed = false

class JoystickView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private val paint = Paint()
    private val innerCirclePosition = PointF(0f, 0f)
    private val outerCircleRadius: Float = 150f
    private val innerCircleRadius: Float = 50f

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        // Draw the outer circle
        paint.color = Color.GRAY
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 10f
        canvas?.drawCircle(width/2f, height/2f, outerCircleRadius, paint)

        // Draw the inner circle
        paint.color = Color.BLUE
        paint.style = Paint.Style.FILL
        canvas?.drawCircle(innerCirclePosition.x + width/2f, innerCirclePosition.y + height/2f, innerCircleRadius, paint)
    }
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            when (it.action) {
                MotionEvent.ACTION_DOWN -> {
                    isJoystickPressed = true
                    startSendingJoystickData()
                }
                MotionEvent.ACTION_MOVE -> {
                    // Update the inner circle position based on touch position
                    val x = it.x - width/2f
                    val y = it.y - height/2f
                    val distance = hypot(x, y)
                    if (distance <= outerCircleRadius - innerCircleRadius) {
                        innerCirclePosition.set(x, y)
                    } else {
                        val angle = atan2(y, x)
                        innerCirclePosition.set(
                            (outerCircleRadius - innerCircleRadius) * cos(angle),
                            (outerCircleRadius - innerCircleRadius) * sin(angle)
                        )
                    }
                    invalidate()
                }

                MotionEvent.ACTION_UP -> {
                    isJoystickPressed = false
                    stopSendingJoystickData()
                    innerCirclePosition.set(0f, 0f)
                    invalidate()
                }

            }
        }
        return true
    }
    private var joystickDataSender: Timer? = null

    private fun startSendingJoystickData() {
        joystickDataSender?.cancel()
        joystickDataSender = Timer().apply {
            scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    if (isJoystickPressed) {
                        WebSocketManager.sendJoystickData(innerCirclePosition.x / 100, innerCirclePosition.y / 100, "joystick")
                    } else {
                        joystickDataSender?.cancel()
                        joystickDataSender = null
                    }
                }
            }, 0, 50) // send joystick data every 50 milliseconds
        }
    }

    private fun stopSendingJoystickData() {
        joystickDataSender?.cancel()
        joystickDataSender = null
    }
}



