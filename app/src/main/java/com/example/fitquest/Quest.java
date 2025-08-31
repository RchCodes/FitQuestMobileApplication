package com.example.fitquest;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class Quest {

    private final Dialog dialog;
    private final LinearLayout questList;
    private final Context context;
    private final List<QuestItem> dailyQuests;
    private final List<QuestItem> weeklyQuests;
    private final List<QuestItem> monthlyQuests;

    public Quest(Context context) {
        this.context = context;
        
        // Initialize quest lists
        dailyQuests = createDailyQuests();
        weeklyQuests = createWeeklyQuests();
        monthlyQuests = createMonthlyQuests();

        // Inflate quest layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.quest, null);

        // Set up the dialog
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Reference the quest list
        questList = view.findViewById(R.id.quest_list);

        // Set up tab buttons
        setupTabButtons(view);

        // Show daily quests by default
        showDailyQuests();
    }

    private void setupTabButtons(View view) {
        Button dailyButton = view.findViewById(R.id.daily_button);
        Button weeklyButton = view.findViewById(R.id.weekly_button);
        Button monthlyButton = view.findViewById(R.id.monthly_button);

        // Set initial active state
        dailyButton.setBackgroundResource(android.R.color.holo_blue_dark);
        weeklyButton.setBackgroundResource(android.R.color.holo_blue_light);
        monthlyButton.setBackgroundResource(android.R.color.holo_blue_light);

        dailyButton.setOnClickListener(v -> {
            showDailyQuests();
            dailyButton.setBackgroundResource(android.R.color.holo_blue_dark);
            weeklyButton.setBackgroundResource(android.R.color.holo_blue_light);
            monthlyButton.setBackgroundResource(android.R.color.holo_blue_light);
        });

        weeklyButton.setOnClickListener(v -> {
            showWeeklyQuests();
            dailyButton.setBackgroundResource(android.R.color.holo_blue_light);
            weeklyButton.setBackgroundResource(android.R.color.holo_blue_dark);
            monthlyButton.setBackgroundResource(android.R.color.holo_blue_light);
        });

        monthlyButton.setOnClickListener(v -> {
            showMonthlyQuests();
            dailyButton.setBackgroundResource(android.R.color.holo_blue_light);
            weeklyButton.setBackgroundResource(android.R.color.holo_blue_light);
            monthlyButton.setBackgroundResource(android.R.color.holo_blue_dark);
        });
    }

    private void showDailyQuests() {
        questList.removeAllViews();
        for (QuestItem quest : dailyQuests) {
            addQuestToView(quest);
        }
    }

    private void showWeeklyQuests() {
        questList.removeAllViews();
        for (QuestItem quest : weeklyQuests) {
            addQuestToView(quest);
        }
    }

    private void showMonthlyQuests() {
        questList.removeAllViews();
        for (QuestItem quest : monthlyQuests) {
            addQuestToView(quest);
        }
    }

    private void addQuestToView(QuestItem quest) {
        View questItem = LayoutInflater.from(context).inflate(R.layout.quest_items, questList, false);

        TextView titleText = questItem.findViewById(R.id.quest_title);
        TextView progressText = questItem.findViewById(R.id.progress_text);
        TextView progressPercentage = questItem.findViewById(R.id.progress_percentage);
        TextView rewardText = questItem.findViewById(R.id.quest_reward);
        ProgressBar progressBar = questItem.findViewById(R.id.quest_progress_bar);
        Button doItButton = questItem.findViewById(R.id.do_it_button);

        titleText.setText(quest.title);
        progressBar.setMax(quest.maxProgress);
        progressBar.setProgress(quest.currentProgress);
        progressText.setText(quest.currentProgress + "/" + quest.maxProgress);

        // Calculate and display percentage
        int percentage = quest.maxProgress > 0 ? (quest.currentProgress * 100) / quest.maxProgress : 0;
        progressPercentage.setText(percentage + "%");

        // Set reward based on quest type
        String reward = getRewardForQuest(quest);
        rewardText.setText(reward);

        // Update button text and state based on completion
        if (quest.currentProgress >= quest.maxProgress) {
            doItButton.setText("✅ COMPLETED");
            doItButton.setBackgroundResource(android.R.color.holo_green_dark);
            doItButton.setEnabled(false);
            progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#4CAF50")));
        } else {
            doItButton.setText("DO IT");
            doItButton.setBackgroundResource(android.R.color.holo_blue_dark);
            doItButton.setEnabled(true);
            progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#2196F3")));
        }

        doItButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, ExerciseTrackingActivity.class);
            intent.putExtra("EXERCISE_TYPE", quest.exerciseType);
            intent.putExtra("MAX_PROGRESS", quest.maxProgress);
            intent.putExtra("QUEST_TITLE", quest.title);
            context.startActivity(intent);
            dialog.dismiss();
        });

        questList.addView(questItem);
    }

    private String getRewardForQuest(QuestItem quest) {
        // Determine reward based on quest type and difficulty
        if (quest.exerciseType.equals("streak")) {
            return "🏆 500 XP";
        } else if (quest.maxProgress >= 100) {
            return "⭐ 200 XP";
        } else if (quest.maxProgress >= 50) {
            return "⭐ 100 XP";
        } else {
            return "⭐ 50 XP";
        }
    }

    private List<QuestItem> createDailyQuests() {
        List<QuestItem> quests = new ArrayList<>();
        quests.add(new QuestItem("Execute 10 Push-ups", 10, "pushups", 0));
        quests.add(new QuestItem("Hold a 30-second Plank", 1, "plank", 0));
        quests.add(new QuestItem("Execute 15 Squats", 15, "squats", 0));
        quests.add(new QuestItem("Do 25 Jumping Jacks", 25, "jumping_jacks", 0));
        quests.add(new QuestItem("Complete 5 Burpees", 5, "burpees", 0));
        return quests;
    }

    private List<QuestItem> createWeeklyQuests() {
        List<QuestItem> quests = new ArrayList<>();
        quests.add(new QuestItem("Complete 50 Push-ups", 50, "pushups", 0));
        quests.add(new QuestItem("Hold Plank for 5 minutes total", 5, "plank", 0));
        quests.add(new QuestItem("Execute 100 Squats", 100, "squats", 0));
        quests.add(new QuestItem("Do 200 Jumping Jacks", 200, "jumping_jacks", 0));
        quests.add(new QuestItem("Complete 25 Burpees", 25, "burpees", 0));
        quests.add(new QuestItem("Run 5km", 1, "running", 0));
        return quests;
    }

    private List<QuestItem> createMonthlyQuests() {
        List<QuestItem> quests = new ArrayList<>();
        quests.add(new QuestItem("Complete 200 Push-ups", 200, "pushups", 0));
        quests.add(new QuestItem("Hold Plank for 30 minutes total", 30, "plank", 0));
        quests.add(new QuestItem("Execute 500 Squats", 500, "squats", 0));
        quests.add(new QuestItem("Do 1000 Jumping Jacks", 1000, "jumping_jacks", 0));
        quests.add(new QuestItem("Complete 100 Burpees", 100, "burpees", 0));
        quests.add(new QuestItem("Run 50km", 50, "running", 0));
        quests.add(new QuestItem("Complete 30 days of exercise", 30, "streak", 0));
        return quests;
    }

    public void show() {
        dialog.show();
    }

    // Inner class to represent a quest item
    private static class QuestItem {
        String title;
        int maxProgress;
        String exerciseType;
        int currentProgress;

        QuestItem(String title, int maxProgress, String exerciseType, int currentProgress) {
            this.title = title;
            this.maxProgress = maxProgress;
            this.exerciseType = exerciseType;
            this.currentProgress = currentProgress;
        }
    }
}
