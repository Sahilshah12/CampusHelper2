package com.campushelper.app.ui.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.LinearGradient
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import kotlin.random.Random

class StarfieldView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val stars = mutableListOf<Star>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val numStars = 80

    init {
        paint.style = Paint.Style.FILL
        paint.strokeCap = Paint.Cap.ROUND
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        initStars()
    }

    private fun initStars() {
        stars.clear()
        repeat(numStars) {
            stars.add(
                Star(
                    x = Random.nextFloat() * width,
                    y = Random.nextFloat() * height,
                    length = Random.nextFloat() * 80f + 40f,
                    speed = Random.nextFloat() * 4f + 2f,
                    angle = Random.nextFloat() * 30f + 30f, // 30-60 degrees
                    alpha = Random.nextInt(150, 255),
                    thickness = Random.nextFloat() * 3f + 2f
                )
            )
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        stars.forEach { star ->
            // Calculate end point of the star tail
            val dx = Math.cos(Math.toRadians(star.angle.toDouble())).toFloat() * star.length
            val dy = Math.sin(Math.toRadians(star.angle.toDouble())).toFloat() * star.length
            
            val tailX = star.x - dx
            val tailY = star.y - dy
            
            // Create gradient for tail effect
            val gradient = LinearGradient(
                star.x, star.y,
                tailX, tailY,
                Color.argb(star.alpha, 255, 255, 255),
                Color.argb(0, 255, 255, 255),
                Shader.TileMode.CLAMP
            )
            
            paint.shader = gradient
            paint.strokeWidth = star.thickness
            paint.style = Paint.Style.STROKE
            
            // Draw the tail
            canvas.drawLine(star.x, star.y, tailX, tailY, paint)
            
            // Draw the star head (bright point)
            paint.shader = null
            paint.style = Paint.Style.FILL
            paint.alpha = star.alpha
            paint.color = Color.WHITE
            canvas.drawCircle(star.x, star.y, star.thickness * 1.5f, paint)
            
            // Move star diagonally
            val moveX = Math.cos(Math.toRadians(star.angle.toDouble())).toFloat() * star.speed
            val moveY = Math.sin(Math.toRadians(star.angle.toDouble())).toFloat() * star.speed
            
            star.x += moveX
            star.y += moveY
            
            // Reset star if it goes off screen
            if (star.x > width + star.length || star.y > height + star.length) {
                star.x = Random.nextFloat() * width
                star.y = Random.nextFloat() * -200f  // Start above screen
                star.length = Random.nextFloat() * 80f + 40f
                star.speed = Random.nextFloat() * 4f + 2f
                star.angle = Random.nextFloat() * 30f + 30f
                star.thickness = Random.nextFloat() * 3f + 2f
            }
        }
        
        // Continue animation
        postInvalidateOnAnimation()
    }

    private data class Star(
        var x: Float,
        var y: Float,
        var length: Float,
        var speed: Float,
        var angle: Float,
        val alpha: Int,
        var thickness: Float
    )
}
