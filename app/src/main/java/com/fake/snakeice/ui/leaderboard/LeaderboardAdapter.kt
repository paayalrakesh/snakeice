package com.fake.snakeice.ui.leaderboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fake.snakeice.databinding.ItemLeaderboardRowBinding

class LeaderboardAdapter : RecyclerView.Adapter<LeaderboardAdapter.VH>() {

    private val data = mutableListOf<ScoreEntry>()

    fun submit(items: List<ScoreEntry>) {
        data.clear()
        data.addAll(items)
        notifyDataSetChanged()
    }

    inner class VH(val b: ItemLeaderboardRowBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemLeaderboardRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = data[position]
        holder.b.tvRank.text = "${position + 1}."
        holder.b.tvUsername.text = item.username
        holder.b.tvScore.text = item.score.toString()
    }
}
