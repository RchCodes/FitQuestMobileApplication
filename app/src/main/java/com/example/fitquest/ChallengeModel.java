package com.example.fitquest;

import java.io.Serializable;

public class ChallengeModel implements Serializable {

    private String id;
    private String name;
    private String exerciseType;
    private String objective;
    private int targetReps;
    private int timeLimitSeconds;
    private String difficulty;
    private int rewardBadge;
    private int rewardCoins;
    private int levelRequirement;
    private boolean isCompleted;
    private boolean isClaimed;
    private boolean strictMode;        // ❗ true = break in form ends the challenge
    private String linkedGoalId;       // 🔗 Goal to mark as complete when challenge succeeds

    public ChallengeModel() {}

    public ChallengeModel(String id, String name, String exerciseType, String objective,
                          int targetReps, int timeLimitSeconds, String difficulty,
                          int rewardBadge, int rewardCoins, int levelRequirement,
                          boolean strictMode, String linkedGoalId) {
        this.id = id;
        this.name = name;
        this.exerciseType = exerciseType;
        this.objective = objective;
        this.targetReps = targetReps;
        this.timeLimitSeconds = timeLimitSeconds;
        this.difficulty = difficulty;
        this.rewardBadge = rewardBadge;
        this.rewardCoins = rewardCoins;
        this.levelRequirement = levelRequirement;
        this.isCompleted = false;
        this.isClaimed = false;
        this.strictMode = strictMode;
        this.linkedGoalId = linkedGoalId;
    }

    public boolean isOneTime() {
        return true;
    }

    public boolean isStrictMode() {
        return strictMode;
    }

    public String getLinkedGoalId() {
        return linkedGoalId;
    }

    // ====================== Getters & Setters ======================

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getExerciseType() { return exerciseType; }
    public void setExerciseType(String exerciseType) { this.exerciseType = exerciseType; }

    public String getObjective() { return objective; }
    public void setObjective(String objective) { this.objective = objective; }

    public int getTargetReps() { return targetReps; }
    public void setTargetReps(int targetReps) { this.targetReps = targetReps; }

    public int getTimeLimitSeconds() { return timeLimitSeconds; }
    public void setTimeLimitSeconds(int timeLimitSeconds) { this.timeLimitSeconds = timeLimitSeconds; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public int getRewardBadge() { return rewardBadge; }
    public void setRewardBadge(int rewardBadge) {
        this.rewardBadge = rewardBadge;
    }
    public int getRewardCoins() { return rewardCoins; }
    public void setRewardCoins(int rewardCoins) { this.rewardCoins = rewardCoins; }

    public int getLevelRequirement() { return levelRequirement; }
    public void setLevelRequirement(int levelRequirement) { this.levelRequirement = levelRequirement; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public boolean isClaimed() { return isClaimed; }
    public void setClaimed(boolean claimed) { isClaimed = claimed; }

    // ====================== Utility ======================

    public boolean isUnlocked(int playerLevel) {
        return playerLevel >= levelRequirement;
    }

    public boolean isTimeBased() {
        String t = exerciseType.toLowerCase();
        return t.contains("plank") || t.contains("tree");
    }


    public String getFormattedTimeLimit() {
        int min = timeLimitSeconds / 60;
        int sec = timeLimitSeconds % 60;
        return String.format("%02d:%02d", min, sec);
    }

    @Override
    public String toString() {
        return name + " (" + difficulty + ") - " + objective;
    }

    public boolean isCompletedByAvatar(AvatarModel avatar) {
        if (avatar == null || id == null || id.isEmpty()) return false;

        // Avatar keeps a set of completed challenge IDs
        return avatar.isChallengeCompleted(this.id);
    }
}
