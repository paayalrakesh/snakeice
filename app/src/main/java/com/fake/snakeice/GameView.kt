package com.fake.snakeice

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.random.Random

class GameView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    enum class Dir { UP, DOWN, LEFT, RIGHT }

    interface GameOverListener {
        fun onGameOver(score: Int)
    }

    private val bgPaint = Paint().apply { color = Color.BLACK }
    private val snakePaint = Paint().apply { color = Color.GREEN }
    private val foodPaint = Paint().apply { color = Color.RED }
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 48f
        isAntiAlias = true
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
    }

    private val handler = Handler(Looper.getMainLooper())
    private val tickDelayMs = 120L

    private var cellSize = 40
    private var cols = 0
    private var rows = 0

    private val snake = ArrayDeque<Point>() // head = first
    private var dir = Dir.RIGHT
    private var pendingDir = Dir.RIGHT
    private var food = Point(0, 0)
    private var score = 0
    private var running = false
    private var gameOverListener: GameOverListener? = null

    // swipe
    private var startX = 0f
    private var startY = 0f
    private val swipeMin = 60f

    fun setGameOverListener(l: GameOverListener) { gameOverListener = l }

    fun startGame() {
        running = true
        score = 0
        dir = Dir.RIGHT
        pendingDir = dir
        snake.clear()

        val cx = cols / 2
        val cy = rows / 2
        snake.addFirst(Point(cx, cy))
        snake.addLast(Point(cx - 1, cy))
        snake.addLast(Point(cx - 2, cy))

        placeFood()
        handler.removeCallbacks(tickRunnable)
        handler.postDelayed(tickRunnable, tickDelayMs)
        invalidate()
    }

    fun pauseGame() { running = false; handler.removeCallbacks(tickRunnable) }
    fun resumeGame() { if (!running) { running = true; handler.postDelayed(tickRunnable, tickDelayMs) } }

    fun setDirection(newDir: Dir) {
        // prevent 180° reverse
        if ((dir == Dir.UP && newDir == Dir.DOWN) ||
            (dir == Dir.DOWN && newDir == Dir.UP) ||
            (dir == Dir.LEFT && newDir == Dir.RIGHT) ||
            (dir == Dir.RIGHT && newDir == Dir.LEFT)) return

        pendingDir = newDir
    }

    fun getScore(): Int = score

    private fun placeFood() {
        do {
            food.x = Random.nextInt(cols)
            food.y = Random.nextInt(rows)
        } while (snake.any { it.x == food.x && it.y == food.y })
    }

    private val tickRunnable = object : Runnable {
        override fun run() {
            if (!running) return
            step()
            invalidate()
            if (running) handler.postDelayed(this, tickDelayMs)
        }
    }

    private fun step() {
        dir = pendingDir
        val head = snake.first()
        val nx = when (dir) {
            Dir.LEFT -> head.x - 1
            Dir.RIGHT -> head.x + 1
            Dir.UP -> head.x
            Dir.DOWN -> head.x
        }
        val ny = when (dir) {
            Dir.UP -> head.y - 1
            Dir.DOWN -> head.y + 1
            Dir.LEFT -> head.y
            Dir.RIGHT -> head.y
        }

        // wall or self collision
        if (nx < 0 || nx >= cols || ny < 0 || ny >= rows || snake.any { it.x == nx && it.y == ny }) {
            running = false
            handler.removeCallbacks(tickRunnable)
            gameOverListener?.onGameOver(score)
            return
        }

        // move
        snake.addFirst(Point(nx, ny))
        if (nx == food.x && ny == food.y) {
            score += 10
            placeFood()
        } else {
            snake.removeLast()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        cellSize = (w.coerceAtMost(h) / 20).coerceAtLeast(30)
        cols = w / cellSize
        rows = h / cellSize
        startGame()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // food
        canvas.drawRect(
            food.x * cellSize.toFloat(),
            food.y * cellSize.toFloat(),
            (food.x + 1) * cellSize.toFloat(),
            (food.y + 1) * cellSize.toFloat(),
            foodPaint
        )

        // snake
        for (p in snake) {
            canvas.drawRect(
                p.x * cellSize.toFloat(),
                p.y * cellSize.toFloat(),
                (p.x + 1) * cellSize.toFloat(),
                (p.y + 1) * cellSize.toFloat(),
                snakePaint
            )
        }

        // HUD
        canvas.drawText("Score: $score", 16f, 56f, textPaint)
        canvas.drawText("Swipe OR use ▲◀▶▼", 16f, 56f + 48f, textPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> { startX = event.x; startY = event.y }
            MotionEvent.ACTION_UP -> {
                val dx = event.x - startX
                val dy = event.y - startY
                if (abs(dx) > abs(dy) && abs(dx) > swipeMin) {
                    setDirection(if (dx > 0) Dir.RIGHT else Dir.LEFT)
                } else if (abs(dy) > swipeMin) {
                    setDirection(if (dy > 0) Dir.DOWN else Dir.UP)
                }
            }
        }
        return true
    }
}
