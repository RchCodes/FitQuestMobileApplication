package com.example.fitquest;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class BattleHistoryAdapter extends RecyclerView.Adapter<BattleHistoryAdapter.ViewHolder> {

    private List<BattleHistoryModel> historyList;

    public BattleHistoryAdapter(List<BattleHistoryModel> historyList) {
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.battle_history_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BattleHistoryModel item = historyList.get(position);

        holder.leftName.setText(item.getLeftName());
        holder.leftLevel.setText("LV. " + item.getLeftLevel());
        holder.leftIcon.setImageResource(item.getLeftIconRes());

        holder.rightName.setText(item.getRightName());
        holder.rightLevel.setText("LV. " + item.getRightLevel());
        holder.rightIcon.setImageResource(item.getRightIconRes());

        int score = item.getScoreChange();
        if (score > 0) {
            holder.scoreChange.setText("+" + score);
            holder.scoreChange.setTextColor(Color.parseColor("#4CAF50")); // Green
        } else {
            holder.scoreChange.setText(String.valueOf(score));
            holder.scoreChange.setTextColor(Color.parseColor("#F44336")); // Red
        }
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView leftIcon, rightIcon;
        TextView leftName, leftLevel, rightName, rightLevel, scoreChange;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            leftIcon = itemView.findViewById(R.id.playerLeftIcon);
            rightIcon = itemView.findViewById(R.id.playerRightIcon);
            leftName = itemView.findViewById(R.id.playerLeftName);
            leftLevel = itemView.findViewById(R.id.playerLeftLevel);
            rightName = itemView.findViewById(R.id.playerRightName);
            rightLevel = itemView.findViewById(R.id.playerRightLevel);
            scoreChange = itemView.findViewById(R.id.scoreChange);
        }
    }
}
