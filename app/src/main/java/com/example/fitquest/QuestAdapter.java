package com.example.fitquest;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class QuestAdapter extends RecyclerView.Adapter<QuestAdapter.QuestViewHolder> {

    private final Context context;
    private List<QuestModel> quests;

    public QuestAdapter(Context context, List<QuestModel> quests) {
        this.context = context;
        this.quests = quests;
    }

    public void updateQuests(List<QuestModel> newQuests) {
        this.quests = newQuests;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public QuestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.quest_items, parent, false);
        return new QuestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestViewHolder holder, int position) {
        QuestModel quest = quests.get(position);

        // Set quest title
        holder.title.setText(quest.getTitle());

        // Build rewards string
        QuestReward r = quest.getReward();
        StringBuilder rewards = new StringBuilder();
        if (r.getXp() > 0) rewards.append("XP: ").append(r.getXp()).append(" ");
        if (r.getCoins() > 0) rewards.append("Coins: ").append(r.getCoins()).append(" ");
        if (r.getArmPoints() > 0) rewards.append("Arms: ").append(r.getArmPoints()).append(" ");
        if (r.getLegPoints() > 0) rewards.append("Legs: ").append(r.getLegPoints()).append(" ");
        if (r.getChestPoints() > 0) rewards.append("Chest: ").append(r.getChestPoints()).append(" ");
        if (r.getBackPoints() > 0) rewards.append("Back: ").append(r.getBackPoints()).append(" ");
        if (r.getPhysiquePoints() > 0) rewards.append("Physique: ").append(r.getPhysiquePoints()).append(" ");
        if (r.getAttributePoints() > 0) rewards.append("Attr: ").append(r.getAttributePoints());
        holder.rewards.setText(rewards.toString().trim());

        // Progress
        holder.progressText.setText(quest.getProgress() + "/" + quest.getTarget());
        holder.progressBar.setMax(quest.getTarget());
        holder.progressBar.setProgress(quest.getProgress());

        int percent = quest.getProgressPercentage();
        holder.progressPercent.setText(percent + "%");

        // Button logic
        holder.actionButton.setEnabled(!quest.isCompleted());
        holder.actionButton.setText(quest.isCompleted() ? "COMPLETED" : "DO QUEST");

        holder.actionButton.setOnClickListener(v -> {
            if (!quest.isCompleted()) {
                // Increment progress
                //quest.addProgress(1);

                Context ctx = v.getContext();
                Intent intent = new Intent(ctx, ExerciseTrackingActivity.class);
                intent.putExtra("EXERCISE_TYPE", quest.getExerciseType());
                intent.putExtra("MAX_PROGRESS", quest.getTarget());
                intent.putExtra("DIFFICULTY_LEVEL", "beginner"); // optional
                intent.putExtra("QUEST_ID", quest.getId()); // this links the quest for auto-claim
                ctx.startActivity(intent);

                // Check if quest completed
                if (quest.isCompleted()) {
                    // Apply rewards
                    boolean leveledUp = new QuestRewardManager().applyRewards(context, quest);

                    // Show reward popup
                    new QuestRewardManager().showRewardPopup(context, quest.getReward());

                    // Optionally show level-up
                    if (leveledUp) {
                        new QuestRewardManager().showLevelUpPopup(context,
                                AvatarManager.loadAvatarOffline(context).getLevel(),
                                AvatarManager.loadAvatarOffline(context).getRank());
                    }
                }

                // Refresh the item
                notifyItemChanged(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return quests.size();
    }

    static class QuestViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView rewards;
        TextView progressText;
        TextView progressPercent;
        ProgressBar progressBar;
        Button actionButton;

        public QuestViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.quest_title);
            rewards = itemView.findViewById(R.id.quest_rewards);
            progressText = itemView.findViewById(R.id.progress_text);
            progressPercent = itemView.findViewById(R.id.progress_percentage);
            progressBar = itemView.findViewById(R.id.quest_progress_bar);
            actionButton = itemView.findViewById(R.id.btn_action);
        }
    }
}
