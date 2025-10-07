package com.example.fitquest;

import java.io.Serializable;

public class QuestModel implements Serializable {

    private String id;
    private String title;
    private String description;
    private QuestReward reward;
    private QuestCategory category;

    // Progress
    private int progress;
    private int target;
    private boolean completed;
    private boolean claimed;
    private long lastCompletedTime;

    private String exerciseType; // e.g., "pushups", "squats", "plank"
    private QuestCompletionType completionType; // SINGLE, ACCUMULATED

    public QuestModel() {
        // Default constructor for Firebase/Gson
    }

    public QuestModel(String id, String title, String description,
                      QuestReward reward, QuestCategory category,
                      int target, String exerciseType) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.reward = reward;
        this.category = category;
        this.target = Math.max(1, target);
        this.progress = 0;
        this.completed = false;
        this.claimed = false;
        this.lastCompletedTime = 0;
        this.exerciseType = exerciseType;
        this.completionType = category == QuestCategory.DAILY ? QuestCompletionType.SINGLE : QuestCompletionType.ACCUMULATED;
    }

    // --- Getters ---
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public QuestReward getReward() { return reward; }
    public QuestCategory getCategory() { return category; }
    public int getProgress() { return progress; }
    public int getTarget() { return target; }
    public boolean isCompleted() { return completed; }
    public boolean isClaimed() { return claimed; }
    public long getLastCompletedTime() { return lastCompletedTime; }
    public String getExerciseType() { return exerciseType; }
    public QuestCompletionType getCompletionType() { return completionType; }

    // --- Setters (for Firebase or manual updates) ---
    public void setClaimed(boolean claimed) { this.claimed = claimed; }
    public void setExerciseType(String exerciseType) { this.exerciseType = exerciseType; }
    public void setCompletionType(QuestCompletionType completionType) { this.completionType = completionType; }

    // --- Progress update ---
    public boolean addProgress(int amount) {
        if (completed) return false;

        progress = Math.min(target, progress + Math.max(0, amount));
        if (progress >= target) {
            complete();
            return true;
        }
        return false;
    }

    public void incrementProgress() {
        addProgress(1);
    }

    public void setProgress(int value) {
        progress = Math.max(0, Math.min(target, value));
        if (progress >= target) complete();
    }

    public void complete() {
        completed = true;
        lastCompletedTime = System.currentTimeMillis();
    }

    public void reset() {
        completed = false;
        claimed = false;
        progress = 0;
        lastCompletedTime = 0;
    }

    // --- Helper methods ---
    public int getProgressPercentage() {
        if (target <= 0) return completed ? 100 : 0;
        return (int)((progress * 100f) / target);
    }
}
