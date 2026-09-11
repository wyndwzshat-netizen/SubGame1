package com.example.subwaygame

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import kotlin.random.Random

class GameView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    // ---- Bitmaps ----
    private val playerRun1 = decode(R.drawable.player_run1)
    private val playerRun2 = decode(R.drawable.player_run2)
    private val playerJump = decode(R.drawable.player_jump)
    private val obstacleBmp = decode(R.drawable.obstacle)
    private val roadTile = decode(R.drawable.road_tile)
    private val grassTile = decode(R.drawable.grass_tile)

    private fun decode(id: Int): Bitmap = BitmapFactory.decodeResource(resources, id)

    // ---- Layout (computed once view is sized) ----
    private var roadLeft = 0f
    private var roadRight = 0f
    private var laneWidth = 0f
    private fun laneX(lane: Int) = roadLeft + laneWidth * (lane + 0.5f)

    // ---- Game state ----
    private val laneCount = 3
    private var laneIndex = 1
    private var groundY = 0f
    private var playerY = 0f
    private var jumping = false
    private var jumpVelocity = 0f
    private var legPhase = 0f
    private var runFrameToggle = false

    private data class Obstacle(var lane: Int, var y: Float)
    private val obstacles = mutableListOf<Obstacle>()
    private var spawnTimer = 0
    private var spawnInterval = 70

    private var speed = 6f
    private var score = 0
    private var gameOver = false
    private var roadScroll = 0f

    var onScoreChanged: ((Int) -> Unit)? = null
    var onGameOver: (() -> Unit)? = null

    // ---- Paint ----
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 60f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }
    private val overlayPaint = Paint().apply {
        color = Color.argb(180, 0, 0, 0)
    }

    // ---- Game loop ----
    private val handler = Handler(Looper.getMainLooper())
    private val frameDelayMs = 16L
    private val loopRunnable = object : Runnable {
        override fun run() {
            update()
            invalidate()
            handler.postDelayed(this, frameDelayMs)
        }
    }

    init {
        handler.postDelayed(loopRunnable, frameDelayMs)
    }

    // ---- Touch controls: swipe left/right/up, tap to restart ----
    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent) = true

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            if (gameOver) resetGame()
            return true
        }

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (gameOver) {
                resetGame()
                return true
            }
            val dx = (e2.x - (e1?.x ?: e2.x))
            val dy = (e2.y - (e1?.y ?: e2.y))
            if (kotlin.math.abs(dx) > kotlin.math.abs(dy)) {
                if (dx > 100) moveRight() else if (dx < -100) moveLeft()
            } else {
                if (dy < -100) jump()
            }
            return true
        }
    })

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return true
    }

    fun moveLeft() {
        if (!gameOver) laneIndex = (laneIndex - 1).coerceAtLeast(0)
    }

    fun moveRight() {
        if (!gameOver) laneIndex = (laneIndex + 1).coerceAtMost(laneCount - 1)
    }

    fun jump() {
        if (!gameOver && !jumping) {
            jumping = true
            jumpVelocity = -32f
        }
    }

    // ---- Sizing ----
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        roadLeft = w * 0.15f
        roadRight = w * 0.85f
        laneWidth = (roadRight - roadLeft) / laneCount
        groundY = h * 0.78f
        resetGame()
    }

    private fun resetGame() {
        laneIndex = 1
        playerY = groundY
        jumping = false
        jumpVelocity = 0f
        obstacles.clear()
        spawnTimer = 0
        speed = 6f
        score = 0
        gameOver = false
        onScoreChanged?.invoke(0)
    }

    // ---- Update ----
    private fun update() {
        if (gameOver) return

        spawnTimer++
        if (spawnTimer > spawnInterval) {
            obstacles.add(Obstacle(Random.nextInt(laneCount), -120f))
            spawnTimer = 0
            spawnInterval = (70 - score / 8).coerceAtLeast(32)
        }

        if (jumping) {
            playerY += jumpVelocity
            jumpVelocity += 2.0f
            if (playerY >= groundY) {
                playerY = groundY
                jumping = false
                jumpVelocity = 0f
            }
        } else {
            legPhase += 0.35f
        }

        roadScroll = (roadScroll + speed) % roadTile.height.toFloat()

        val iterator = obstacles.iterator()
        while (iterator.hasNext()) {
            val o = iterator.next()
            o.y += speed
            if (o.y > height + 150) {
                iterator.remove()
                continue
            }
            if (o.lane == laneIndex) {
                val obstacleTop = o.y
                val obstacleBottom = o.y + obstacleBmp.height
                val playerTop = playerY - 130
                val playerBottom = playerY
                val overlapY = obstacleBottom > playerTop + 40 && obstacleTop < playerBottom
                val clearedByJump = jumping && playerY < groundY - 70
                if (overlapY && !clearedByJump) {
                    gameOver = true
                    onGameOver?.invoke()
                }
            }
        }

        speed += 0.003f
        score++
        onScoreChanged?.invoke(score / 5)
    }

    // ---- Draw ----
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawRoad(canvas)
        drawObstacles(canvas)
        drawPlayer(canvas)
        if (gameOver) drawGameOver(canvas)
    }

    private fun drawRoad(canvas: Canvas) {
        // grass sides
        var gy = -grassTile.height + (roadScroll % grassTile.height)
        while (gy < height) {
            canvas.drawBitmap(scaleTo(grassTile, roadLeft.toInt(), grassTile.height), 0f, gy, null)
            canvas.drawBitmap(
                scaleTo(grassTile, (width - roadRight).toInt(), grassTile.height),
                roadRight, gy, null
            )
            gy += grassTile.height
        }
        // road
        var ry = -roadTile.height + (roadScroll % roadTile.height)
        val scaledRoad = scaleTo(roadTile, (roadRight - roadLeft).toInt(), roadTile.height)
        while (ry < height) {
            canvas.drawBitmap(scaledRoad, roadLeft, ry, null)
            ry += roadTile.height
        }
    }

    private val scaleCache = HashMap<String, Bitmap>()
    private fun scaleTo(src: Bitmap, w: Int, h: Int): Bitmap {
        if (w <= 0 || h <= 0) return src
        val key = "${System.identityHashCode(src)}_${w}_${h}"
        return scaleCache.getOrPut(key) { Bitmap.createScaledBitmap(src, w, h, true) }
    }

    private fun drawObstacles(canvas: Canvas) {
        for (o in obstacles) {
            val x = laneX(o.lane) - obstacleBmp.width / 2f
            canvas.drawBitmap(obstacleBmp, x, o.y, null)
        }
    }

    private fun drawPlayer(canvas: Canvas) {
        val x = laneX(laneIndex) - playerRun1.width / 2f
        val bmp = when {
            jumping -> playerJump
            else -> {
                runFrameToggle = (legPhase.toInt() / 4) % 2 == 0
                if (runFrameToggle) playerRun1 else playerRun2
            }
        }
        canvas.drawBitmap(bmp, x, playerY - bmp.height, null)
    }

    private fun drawGameOver(canvas: Canvas) {
        val rect = RectF(0f, height / 2f - 120, width.toFloat(), height / 2f + 120)
        canvas.drawRect(rect, overlayPaint)
        canvas.drawText("انتهت اللعبة", width / 2f, height / 2f - 20, textPaint)
        canvas.drawText("النقاط: ${score / 5}", width / 2f, height / 2f + 50, textPaint)
    }

    fun stopLoop() {
        handler.removeCallbacks(loopRunnable)
    }
}
