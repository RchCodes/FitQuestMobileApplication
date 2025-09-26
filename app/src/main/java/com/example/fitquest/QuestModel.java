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
    private boolean isCompleted;
    private long lastCompletedTime;
    private boolean claimed = false;

    public QuestModel() {} // for Firebase/Gson

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
        this.isCompleted = false;
        this.claimed = false;
        this.lastCompletedTime = 0;
        this.exerciseType = exerciseType;
    }


    // --- Getters ---
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public QuestReward getReward() { return reward; }
    public QuestCategory getCategory() { return category; }
    public int getProgress() { return progress; }
    public int getTarget() { return target; }
    public boolean isCompleted() { return isCompleted; }
    public long getLastCompletedTime() { return lastCompletedTime; }

    // --- Progress update ---
    public boolean addProgress(int amount) {
        if (isCompleted) return false;
        progress = Math.min(target, progress + Math.max(0, amount));
        if (progress >= target) {
            complete();
            return true;
        }
        return false;
    }

    public void setProgress(int p) {
        progress = Math.max(0, Math.min(target, p));
        if (progress >= target) complete();
    }

    public void complete() {
        isCompleted = true;
        lastCompletedTime = System.currentTimeMillis();
    }

    public void reset() {
        isCompleted = false;
        progress = 0;
        lastCompletedTime = 0;
        this.claimed = false;
    }


    // Reset quest

    public int getProgressPercentage() {
        if (target <= 0) return isCompleted ? 100 : 0;
        return (int)((progress * 100f) / target);
    }

    private boolean isClaimed = false;

    public boolean isClaimed() {
        return isClaimed;
    }

    public void setClaimed(boolean claimed) {
        this.isClaimed = claimed;
    }

    private String exerciseType; // e.g., "pushups", "squats", "plank"

    public String getExerciseType() {
        return exerciseType;
    }

    public void setExerciseType(String exerciseType) {
        this.exerciseType = exerciseType;
    }

    // --- Progress Methods ---
    // Increment progress
    public void incrementProgress() {
        if (!isCompleted()) {   // use your existing isCompleted()
            this.progress++;
        }
    }

    public void resetProgress() {
        progress = 0;
        claimed = false;
    }
}
