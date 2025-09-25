package com.fake.snakeice.ui.leaderboard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.fake.snakeice.databinding.ActivityLeaderboardBinding

data class ScoreEntry(val username: String = "Player", val score: Int = 0)

class LeaderboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLeaderboardBinding
    private val db by lazy { Firebase.firestore }
    private val adapter = LeaderboardAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLeaderboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvLeaderboard.layoutManager = LinearLayoutManager(this)
        binding.rvLeaderboard.adapter = adapter

        db.collection("scores")
            .orderBy("score", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { qs ->
                val items = qs.documents.map {
                    ScoreEntry(
                        username = it.getString("username") ?: "Player",
                        score = (it.getLong("score") ?: 0L).toInt()
                    )
                }
                adapter.submit(items)
            }
    }
}
