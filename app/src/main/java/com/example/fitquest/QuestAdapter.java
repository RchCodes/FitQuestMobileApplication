package com.example.fitquest;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class QuestAdapter extends RecyclerView.Adapter<QuestAdapter.QuestViewHolder> implements QuestManager.QuestProgressListener {

    private final Context context;
    private List<QuestModel> quests;

    public QuestAdapter(Context context, List<QuestModel> quests) {
        this.context = context;
        this.quests = quests;
        QuestManager.setQuestProgressListener(this);
    }

    public void updateQuests(List<QuestModel> newQuests) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return quests.size();
            }

            @Override
            public int getNewListSize() {
                return newQuests.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPos, int newItemPos) {
                return quests.get(oldItemPos).getId()
                        .equals(newQuests.get(newItemPos).getId());
            }

            @Override
            public boolean areContentsTheSame(int oldItemPos, int newItemPos) {
                QuestModel oldQ = quests.get(oldItemPos);
                QuestModel newQ = newQuests.get(newItemPos);

                return oldQ.getProgress() == newQ.getProgress()
                        && oldQ.isCompleted() == newQ.isCompleted()
                        && oldQ.isClaimed() == newQ.isClaimed()
                        && oldQ.getTarget() == newQ.getTarget()
                        && oldQ.getTitle().equals(newQ.getTitle())
                        && oldQ.getDescription().equals(newQ.getDescription());
            }
        });

        quests.clear();
        quests.addAll(newQuests);
        diffResult.dispatchUpdatesTo(this);
    }

    @Override
    public void onAvatarUpdated(AvatarModel updatedAvatar) {
        // Quests list doesn’t directly display avatar info,
        // so we don’t need to refresh anything here.
        // Leave it empty, or trigger a UI refresh if you want.
    }

    @Override
    public void onQuestProgressUpdated(QuestModel quest) {
        int pos = findQuestPosition(quest);
        if (pos >= 0) {
            quests.set(pos, quest);
            notifyItemChanged(pos); // triggers onBindViewHolder -> updates button text
        }
    }

    private int findQuestPosition(QuestModel quest) {
        for (int i = 0; i < quests.size(); i++) {
            if (quests.get(i).getId().equals(quest.getId())) return i;
        }
        return -1;
    }

    @Override
    public void onQuestCompleted(QuestModel quest, boolean leveledUp) {
        int pos = findQuestPosition(quest);
        if (pos >= 0) {
            quests.set(pos, quest);
            notifyItemChanged(pos); // rebinds and switches button to CLAIM
        }
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
        if (!quest.isCompleted()) {
            holder.actionButton.setText("DO QUEST");
        } else if (quest.isCompleted() && !quest.isClaimed()) {
            holder.actionButton.setText("CLAIM");
        } else {
            holder.actionButton.setText("COMPLETED");
            holder.actionButton.setEnabled(false);
        }


        holder.actionButton.setOnClickListener(v -> {
            if (!quest.isCompleted()) {
                // Special logic for "Complete 5 Quests" daily quest
                if ("q_daily_quests_5".equals(quest.getId())) {
                    // Find all other incomplete daily quests
                    List<QuestModel> dailyQuests = QuestManager.getDailyQuests(context);
                    List<QuestModel> incomplete = new java.util.ArrayList<>();
                    for (QuestModel q : dailyQuests) {
                        if (!q.isCompleted() && !"q_daily_quests_5".equals(q.getId())) {
                            incomplete.add(q);
                        }
                    }
                    if (!incomplete.isEmpty()) {
                        // Pick one at random
                        QuestModel randomQuest = incomplete.get(new java.util.Random().nextInt(incomplete.size()));
                        // Launch its activity
                        Intent intent = new Intent(context, ExerciseTrackingActivity.class);
                        intent.putExtra("EXERCISE_TYPE", randomQuest.getExerciseType());
                        intent.putExtra("MAX_PROGRESS", randomQuest.getTarget());
                        intent.putExtra("DIFFICULTY_LEVEL", "beginner");
                        intent.putExtra("QUEST_ID", randomQuest.getId());
                        context.startActivity(intent);
                    } else {
                        Toast.makeText(context, "No incomplete daily quests available!", Toast.LENGTH_SHORT).show();
                    }
                } else if (quest.getCompletionType() == QuestCompletionType.SINGLE) {
                    // Launch exercise activity as normal
                    Context ctx = v.getContext();
                    Intent intent = new Intent(ctx, ExerciseTrackingActivity.class);
                    intent.putExtra("EXERCISE_TYPE", quest.getExerciseType());
                    intent.putExtra("MAX_PROGRESS", quest.getTarget());
                    intent.putExtra("DIFFICULTY_LEVEL", "beginner"); // optional
                    intent.putExtra("QUEST_ID", quest.getId()); // link quest for tracking
                    ctx.startActivity(intent);
                } else {
                    // For ACCUMULATED quests (weekly/monthly), show info message
                    Toast.makeText(context, "This quest requires accumulated progress over time. Complete related daily quests to progress!", Toast.LENGTH_LONG).show();
                }
            } else if (quest.isCompleted() && !quest.isClaimed()) {
                // Claim button pressed
                boolean leveledUp = QuestManager.claimQuest(context, quest);

                // Show reward popup
                new QuestRewardManager().showRewardPopup(context, quest.getReward());

                // Optionally show level-up
                if (leveledUp) {
                    AvatarModel avatar = AvatarManager.loadAvatarOffline(context);
                    new QuestRewardManager().showLevelUpPopup(context, avatar.getLevel(), avatar.getRank());
                }

                // Refresh item
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
