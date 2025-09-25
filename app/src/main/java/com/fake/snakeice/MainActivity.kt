package com.fake.snakeice.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.fake.snakeice.GameView
import com.fake.snakeice.databinding.ActivityMainBinding
import com.fake.snakeice.ui.leaderboard.LeaderboardActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity(), GameView.GameOverListener {

    private lateinit var binding: ActivityMainBinding

    // Non-KTX Firebase (matches your Gradle dependencies)
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.gameView.setGameOverListener(this)

        // D-Pad controls
        binding.btnUp.setOnClickListener { binding.gameView.setDirection(GameView.Dir.UP) }
        binding.btnDown.setOnClickListener { binding.gameView.setDirection(GameView.Dir.DOWN) }
        binding.btnLeft.setOnClickListener { binding.gameView.setDirection(GameView.Dir.LEFT) }
        binding.btnRight.setOnClickListener { binding.gameView.setDirection(GameView.Dir.RIGHT) }
    }

    override fun onPause() {
        super.onPause()
        binding.gameView.pauseGame()
    }

    override fun onResume() {
        super.onResume()
        binding.gameView.resumeGame()
    }

    override fun onGameOver(score: Int) {
        AlertDialog.Builder(this)
            .setTitle("Game Over")
            .setMessage("Score: $score")
            .setCancelable(false)
            .setPositiveButton("Save & Leaderboard") { _, _ ->
                saveScoreThenShowLeaderboard(score)
            }
            .setNegativeButton("Play Again") { _, _ ->
                binding.gameView.startGame()
            }
            .setNeutralButton("Exit") { _, _ -> finish() }
            .show()
    }

    private fun saveScoreThenShowLeaderboard(score: Int) {
        val user = auth.currentUser
        val username = user?.displayName ?: "Guest"
        val uid = user?.uid ?: "guest"

        val data = hashMapOf(
            "uid" to uid,
            "username" to username,
            "score" to score,
            "ts" to System.currentTimeMillis()
        )

        db.collection("scores").add(data).addOnCompleteListener {
            startActivity(Intent(this, LeaderboardActivity::class.java))
        }
    }
}
