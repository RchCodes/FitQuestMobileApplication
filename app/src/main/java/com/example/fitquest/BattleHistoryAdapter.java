package com.example.fitquest;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

public class BattleHistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HISTORY = 1;
    private static final int TYPE_EMPTY = 0;

    private List<BattleHistoryModel> historyList;

    public BattleHistoryAdapter(List<BattleHistoryModel> historyList) {
        if (historyList == null || historyList.isEmpty()) {
            this.historyList = Collections.emptyList();
        } else {
            this.historyList = historyList;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return historyList.isEmpty() ? TYPE_EMPTY : TYPE_HISTORY;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HISTORY) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.battle_history_item, parent, false);
            return new HistoryViewHolder(view);
        } else {
            TextView emptyView = new TextView(parent.getContext());
            emptyView.setText("No battles yet.");
            emptyView.setTextSize(16f);
            emptyView.setPadding(32, 32, 32, 32);
            emptyView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            return new EmptyViewHolder(emptyView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HistoryViewHolder && !historyList.isEmpty()) {
            BattleHistoryModel item = historyList.get(position);
            ((HistoryViewHolder) holder).bind(item);

            // Fade-in animation
            holder.itemView.setAlpha(0f);
            holder.itemView.animate().alpha(1f).setDuration(300).setStartDelay(position * 50).start();
        }
    }


    @Override
    public int getItemCount() {
        return historyList.isEmpty() ? 1 : historyList.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView playerLeftName, playerLeftLevel, playerRightName, playerRightLevel, scoreChange;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            playerLeftName = itemView.findViewById(R.id.playerLeftName);
            playerLeftLevel = itemView.findViewById(R.id.playerLeftLevel);
            playerRightName = itemView.findViewById(R.id.playerRightName);
            playerRightLevel = itemView.findViewById(R.id.playerRightLevel);
            scoreChange = itemView.findViewById(R.id.scoreChange);
        }

        void bind(BattleHistoryModel model) {
            playerLeftName.setText(model.getLeftName());
            playerLeftLevel.setText("LV. " + model.getLeftLevel());
            playerRightName.setText(model.getRightName());
            playerRightLevel.setText("LV. " + model.getRightLevel());
            scoreChange.setText((model.getScoreChange() >= 0 ? "+" : "") + model.getScoreChange());
        }
    }

    static class EmptyViewHolder extends RecyclerView.ViewHolder {
        public EmptyViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
