package com.example.fitquest;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitquest.AvatarModel;
import com.example.fitquest.R;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private List<AvatarModel> players;

    public LeaderboardAdapter(List<AvatarModel> players) {
        this.players = players;
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
        AvatarModel player = players.get(position);

        holder.tvName.setText(player.getUsername());
        holder.tvLevel.setText("Lv. " + player.getLevel());
        holder.tvScore.setText(String.valueOf(player.getXp()));

        int rank = position + 1; // ranking based on sorted list

        if (rank <= 3) {
            holder.tvRank.setText("");
            switch (rank) {
                case 1:
                    holder.tvRank.setBackgroundResource(R.drawable.ic_rank_gold);
                    break;
                case 2:
                    holder.tvRank.setBackgroundResource(R.drawable.ic_rank_silver);
                    break;
                case 3:
                    holder.tvRank.setBackgroundResource(R.drawable.ic_rank_bronze);
                    break;
            }
        } else {
            holder.imgRank.setBackgroundResource(R.drawable.bg_rank_generic);
            holder.tvRank.setText(String.valueOf(rank));
        }

        // TODO: Render avatar (bodyStyle, outfit, etc.) into imgAvatar if needed
    }

    @Override
    public int getItemCount() {
        return players.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvName, tvLevel, tvScore;
        ImageView imgAvatar;
        View imgRank;

        ViewHolder(View itemView) {
            super(itemView);
            imgRank = itemView.findViewById(R.id.img_avatar);
            tvRank = itemView.findViewById(R.id.tv_rank);
            imgAvatar = itemView.findViewById(R.id.img_avatar);
            tvName = itemView.findViewById(R.id.tv_name);
            tvLevel = itemView.findViewById(R.id.tv_level);
            tvScore = itemView.findViewById(R.id.tv_score);
        }
    }
}
