package com.example.subwaygame

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var gameView: GameView
    private lateinit var scoreLabel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = FrameLayout(this)

        gameView = GameView(this)
        root.addView(
            gameView,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        scoreLabel = TextView(this).apply {
            text = "0"
            textSize = 22f
            setTextColor(Color.WHITE)
            setShadowLayer(6f, 0f, 0f, Color.BLACK)
        }
        val labelParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            topMargin = 60
            leftMargin = 40
        }
        root.addView(scoreLabel, labelParams)

        setContentView(root)

        gameView.onScoreChanged = { score ->
            runOnUiThread { scoreLabel.text = score.toString() }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        gameView.stopLoop()
    }
}
