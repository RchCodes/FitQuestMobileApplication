package com.example.fitquest;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private List<RankLeaderboardEntry> players;
    private String currentUserId; // Add this

    public LeaderboardAdapter(List<RankLeaderboardEntry> players, String currentUserId) {
        this.players = players;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public LeaderboardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.leaderboard_items, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaderboardAdapter.ViewHolder holder, int position) {
        RankLeaderboardEntry player = players.get(position);

        holder.tvName.setText(player.getUsername());
        holder.tvLevel.setText("Lv. " + player.getLevel());
        holder.tvScore.setText(player.getScoreLabel());

        int rank = position + 1;
        holder.tvRank.setText(rank <= 3 ? "" : String.valueOf(rank));

        switch(rank) {
            case 1: holder.tvRank.setBackgroundResource(R.drawable.ic_rank_gold); break;
            case 2: holder.tvRank.setBackgroundResource(R.drawable.ic_rank_silver); break;
            case 3: holder.tvRank.setBackgroundResource(R.drawable.ic_rank_bronze); break;
            default: holder.tvRank.setBackgroundResource(R.drawable.bg_rank_generic); break;
        }

        // Highlight current user
        if (player.getUserId().equals(currentUserId)) {
            holder.itemView.setBackgroundResource(R.drawable.bg_current_user_highlight);
        } else {
            holder.itemView.setBackgroundResource(R.drawable.bg_lb_item); // normal
        }
    }

    @Override
    public int getItemCount() {
        return players.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvName, tvLevel, tvScore;
        ImageView imgAvatar;

        ViewHolder(View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tv_rank);
            imgAvatar = itemView.findViewById(R.id.img_avatar);
            tvName = itemView.findViewById(R.id.tv_name);
            tvLevel = itemView.findViewById(R.id.tv_level);
            tvScore = itemView.findViewById(R.id.tv_score);
        }
    }
}
