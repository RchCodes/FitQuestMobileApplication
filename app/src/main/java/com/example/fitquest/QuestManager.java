package com.example.fitquest;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class QuestManager {

    private static final String PREFS = "FitQuestPrefs";
    private static final String RESET_HOUR_KEY = "quest_reset_hour";
    private static final int DEFAULT_RESET_HOUR = 4;

    private static List<QuestModel> quests = null;

    public static int lastReportedDailyTotal = 0;

    public static List<QuestModel> getAllQuests(Context context) {
        ensureLoaded(context);
        return quests;
    }

    // Listener for UI updates
    public interface QuestProgressListener {
        void onQuestProgressUpdated(QuestModel quest);
        void onQuestCompleted(QuestModel quest, boolean leveledUp);

        // NEW: fires whenever rewards change the avatar (xp, coins, stats)
        void onAvatarUpdated(AvatarModel updatedAvatar);
    }


    private static QuestProgressListener progressListener;

    public static void setQuestProgressListener(QuestProgressListener listener) {
        progressListener = listener;
    }

    // Lazy load quests from storage
    private static void ensureLoaded(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        lastReportedDailyTotal = prefs.getInt("last_reported_steps", 0);

        if (quests != null) return;
        quests = QuestStorage.loadQuestsOffline(ctx);
        if (quests == null || quests.isEmpty()) {
            quests = createDefaultQuests();
            persistAll(ctx);
        }
        resetQuestsIfNeeded(ctx);
    }

    // Public getters
    public static List<QuestModel> getAll(Context ctx) {
        ensureLoaded(ctx);
        return quests;
    }

    public static List<QuestModel> getByCategory(Context ctx, QuestCategory category) {
        ensureLoaded(ctx);
        List<QuestModel> out = new ArrayList<>();
        for (QuestModel q : quests) {
            if (q.getCategory() == category) out.add(q);
        }
        return out;
    }

    public static List<QuestModel> getDailyQuests(Context ctx) { return getByCategory(ctx, QuestCategory.DAILY); }
    public static List<QuestModel> getWeeklyQuests(Context ctx) { return getByCategory(ctx, QuestCategory.WEEKLY); }
    public static List<QuestModel> getMonthlyQuests(Context ctx) { return getByCategory(ctx, QuestCategory.MONTHLY); }

    // --- Report exercise result ---
    public static void reportExerciseResult(Context ctx, String exerciseType, int amount) {
        ensureLoaded(ctx);
        boolean changed = false;

        for (QuestModel q : quests) {
            if (q.isCompleted()) continue;

            // Compare directly with quest's exerciseType
            if (q.getExerciseType() != null &&
                    q.getExerciseType().equalsIgnoreCase(exerciseType)) {

                int oldProgress = q.getProgress();
                boolean wasCompleted = q.isCompleted();
                
                q.addProgress(amount);
                changed = true;

                Log.d("QuestManager", "Quest " + q.getId() + " progress: " + oldProgress + " -> " + q.getProgress() + " (target: " + q.getTarget() + ")");

                if (progressListener != null) {
                    progressListener.onQuestProgressUpdated(q);
                    
                    // Check if quest was just completed
                    if (!wasCompleted && q.isCompleted()) {
                        progressListener.onQuestCompleted(q, false);
                    }
                }
            }
        }

        if (changed) {
            persistAll(ctx);
            for (QuestModel q : quests) {
                checkGoalsOnExercise(ctx, q);
            }
        }
    }

    // --- Claim quest manually ---
    public static boolean claimQuest(Context ctx, QuestModel quest) {
        if (quest == null || !quest.isCompleted() || quest.isClaimed()) return false;

        // Apply rewards through QuestRewardManager
        boolean leveledUp = QuestRewardManager.applyRewards(ctx, quest);

        quest.setClaimed(true);
        persistAll(ctx);

        // 🔥 Notify listener
        if (progressListener != null) {
            // Send avatar update
            AvatarModel avatar = AvatarManager.loadAvatarOffline(ctx);
            if (avatar != null) {
                progressListener.onAvatarUpdated(avatar);
            }

            // Still notify quest completion
            progressListener.onQuestCompleted(quest, leveledUp);
        }

        // --- NEW: trigger day-trained reporting for weekly/monthly completion quests ---
        if ("q_daily_quests_5".equals(quest.getId())) {
            reportDayTrained(ctx); // Increment weekly/monthly day-count quests
        }

        // Update "Complete 5 quests" quest when any other quest is claimed
        updateQuestCompletionQuest(ctx);

        return leveledUp;
    }


    // --- Persistence ---
    private static void persistAll(Context ctx) {
        QuestStorage.saveQuestsOffline(ctx, quests);
        QuestStorage.saveQuestsOnline(quests);
    }

    // --- Reset quests by category ---
    public static void resetQuestsIfNeeded(Context ctx) {
        ensureLoaded(ctx);
        int resetHour = getResetHour(ctx);
        Calendar now = Calendar.getInstance();

        for (QuestModel q : quests) {
            if (!q.isCompleted()) continue;

            long last = q.getLastCompletedTime();
            if (last <= 0) continue;

            Calendar lastCal = Calendar.getInstance();
            lastCal.setTimeInMillis(last);

            boolean shouldReset = false;
            switch (q.getCategory()) {
                case DAILY:
                    Calendar todayReset = Calendar.getInstance();
                    todayReset.set(Calendar.HOUR_OF_DAY, resetHour);
                    todayReset.set(Calendar.MINUTE, 0);
                    todayReset.set(Calendar.SECOND, 0);
                    todayReset.set(Calendar.MILLISECOND, 0);
                    if (last < todayReset.getTimeInMillis() && now.getTimeInMillis() >= todayReset.getTimeInMillis()) {
                        shouldReset = true;
                    }
                    break;
                case WEEKLY:
                    lastCal.add(Calendar.DAY_OF_YEAR, 7);
                    if (now.after(lastCal)) shouldReset = true;
                    break;
                case MONTHLY:
                    lastCal.add(Calendar.DAY_OF_YEAR, 30);
                    if (now.after(lastCal)) shouldReset = true;
                    break;
            }

            if (shouldReset) q.reset();
        }
        persistAll(ctx);
    }

    // --- Reset hour preference ---
    public static int getResetHour(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return prefs.getInt(RESET_HOUR_KEY, DEFAULT_RESET_HOUR);
    }

    public static void setResetHour(Context ctx, int hour) {
        if (hour < 0 || hour > 23) return;
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs.edit().putInt(RESET_HOUR_KEY, hour).apply();
    }

    // --- Default quest creation ---
    private static List<QuestModel> createDefaultQuests() {
        List<QuestModel> list = new ArrayList<>();

        // Daily
        list.add(new QuestModel("q_daily_push_10", "Complete 10 Push-ups", "Complete 10 pushups in one session",
                new QuestReward(50,50,1,0,0,0,1,0), QuestCategory.DAILY,10,"pushups"));
        list.add(new QuestModel("q_daily_squat_10", "Execute 10 Squats", "Do 10 squats in one session",
                new QuestReward(15,40,0,1,0,0,1,0), QuestCategory.DAILY,10,"squats"));
        list.add(new QuestModel("q_daily_plank_20", "Hold Plank 20 Seconds", "Maintain a plank for 20 seconds",
                new QuestReward(25,50,0,0,1,1,1,1), QuestCategory.DAILY,20,"plank"));
        list.add(new QuestModel("q_daily_crunches_10", "Do 10 Crunches", "Complete 10 crunches in one session",
                new QuestReward(15,40,0,0,1,1,1,0), QuestCategory.DAILY,10,"crunches"));
        list.add(new QuestModel("q_daily_steps_2500", "Walk 2,500 Steps", "Accumulate 2,500 steps today using your device's step counter",
                new QuestReward(40, 40, 0, 0, 0, 0, 0, 0), QuestCategory.DAILY, 2500, "steps"));
        list.add(new QuestModel("q_daily_quests_5", "Complete 5 Quests", "Complete 5 quests in today",
                new QuestReward(100, 50, 0, 0, 0, 0, 0, 5), QuestCategory.DAILY, 5, "completion"));
        list.add(new QuestModel("q_daily_jj_20", "Do 20 Jumping Jacks", "Perform 20 jumping jacks in one session",
                new QuestReward(20,30,1,0,0,0,1,0), QuestCategory.DAILY,20,"jumpingjacks"));
        list.add(new QuestModel("q_daily_tree_20", "Hold Tree Pose 20s", "Maintain Tree Pose for 20 seconds",
                new QuestReward(25,30,0,1,0,0,1,0), QuestCategory.DAILY,20,"treepose"));
        list.add(new QuestModel("q_daily_situp_15", "Complete 15 Sit-ups", "Do 15 sit-ups in one session",
                new QuestReward(20,30,0,0,1,0,1,0), QuestCategory.DAILY,15,"situps"));
        list.add(new QuestModel("q_daily_lunge_10", "Do 10 Lunges", "Perform 10 lunges (total) in one session",
                new QuestReward(20,30,0,1,0,0,1,0), QuestCategory.DAILY,10,"lunges"));
        // Weekly
        list.add(new QuestModel("q_weekly_push_50", "Accumulate 50 Push-ups", "Do 50 pushups this week",
                new QuestReward(100,200,0,0,0,0,0,3), QuestCategory.WEEKLY,50,"pushups"));
        list.add(new QuestModel("q_weekly_squat_50", "Accumulate 50 Squats", "Do 50 squats this week",
                new QuestReward(90,180,0,0,0,0,0,2), QuestCategory.WEEKLY,50,"squats"));
        list.add(new QuestModel("q_weekly_plank_100", "Accumulate 100 Plank Seconds", "Maintain a plank for 100 seconds this week",
                new QuestReward(75,60,0,0,0,0,0,3), QuestCategory.WEEKLY,100,"plank"));
        list.add(new QuestModel("q_weekly_crunches_50", "Accumulate 50 Crunches", "Complete 50 crunches this week",
                new QuestReward(60,40,0,0,0,0,0,2), QuestCategory.WEEKLY,50,"crunches"));
        list.add(new QuestModel("q_weekly_steps_12500", "Walk 12,500 Steps", "Accumulate 12,500 this week using your device's step counter",
                new QuestReward(250, 150, 0, 0, 0, 0, 0, 2), QuestCategory.WEEKLY, 12500, "steps"));
        list.add(new QuestModel("q_weekly_quests_5", "Train 5 Days this week", "Log exercise on 5 separate days",
                new QuestReward(250, 50, 0, 0, 0, 0, 0, 5), QuestCategory.WEEKLY, 5, "completion"));
        list.add(new QuestModel("q_weekly_jj_100", "Accumulate 100 Jumping Jacks", "Do 100 jumping jacks this week",
                new QuestReward(80,120,0,0,0,0,0,3), QuestCategory.WEEKLY,100,"jumpingjacks"));
        list.add(new QuestModel("q_weekly_tree_100", "Hold Tree Pose 100s", "Maintain Tree Pose cumulatively 100 seconds this week",
                new QuestReward(80,100,0,0,1,0,0,3), QuestCategory.WEEKLY,100,"treepose"));
        list.add(new QuestModel("q_weekly_situp_100", "Accumulate 100 Sit-ups", "Complete 100 sit-ups this week",
                new QuestReward(80,120,0,0,1,0,0,3), QuestCategory.WEEKLY,100,"situps"));
        list.add(new QuestModel("q_weekly_lunge_50", "Accumulate 50 Lunges", "Perform 50 lunges this week",
                new QuestReward(80,120,0,0,1,0,0,3), QuestCategory.WEEKLY,50,"lunges"));
        // Monthly
        list.add(new QuestModel("q_monthly_20days", "Train 20 Days This Month", "Log exercise on 20 separate days",
                new QuestReward(300,500,0,0,0,0,0,4), QuestCategory.MONTHLY,20,"completion"));
        list.add(new QuestModel("q_monthly_200push", "Accumulate 200 Push-ups This Month", "Do 200 pushups this month",
                new QuestReward(350,250,0,0,0,0,0,5), QuestCategory.MONTHLY,200,"pushups"));
        list.add(new QuestModel("q_monthly_squat_200", "Accumulate 200 Squats", "Do 200 squats this month",
                new QuestReward(90,180,0,0,0,0,0,7), QuestCategory.MONTHLY,200,"squats"));
        list.add(new QuestModel("q_monthly_plank_400", "Accumulate 400 Plank Seconds", "Maintain a plank for 400 seconds this month",
                new QuestReward(75,60,0,0,0,0,0,10), QuestCategory.MONTHLY,400,"plank"));
        list.add(new QuestModel("q_monthly_crunches_200", "Accumulate 400 Crunches", "Complete 400 crunches this month",
                new QuestReward(60,40,0,0,0,0,0,8), QuestCategory.MONTHLY,200,"crunches"));
        list.add(new QuestModel("q_monthly_steps_50000", "Walk 50,000 Steps", "Accumulate 50,000 this month using your device's step counter",
                new QuestReward(900, 300, 0, 0, 0, 0, 0, 10), QuestCategory.MONTHLY, 50000, "steps"));
        list.add(new QuestModel("q_monthly_jj_500", "Accumulate 500 Jumping Jacks", "Perform 500 jumping jacks this month",
                new QuestReward(300,400,0,0,0,0,0,5), QuestCategory.MONTHLY,500,"jumpingjacks"));
        list.add(new QuestModel("q_monthly_tree_300", "Hold Tree Pose 300s", "Maintain Tree Pose cumulatively 300 seconds this month",
                new QuestReward(300,350,0,0,1,0,0,5), QuestCategory.MONTHLY,300,"treepose"));
        list.add(new QuestModel("q_monthly_situp_400", "Accumulate 400 Sit-ups", "Complete 400 sit-ups this month",
                new QuestReward(300,400,0,0,1,0,0,5), QuestCategory.MONTHLY,400,"situps"));
        list.add(new QuestModel("q_monthly_lunge_200", "Accumulate 200 Lunges", "Perform 200 lunges this month",
                new QuestReward(300,400,0,0,1,0,0,5), QuestCategory.MONTHLY,200,"lunges"));
        return list;
    }

    private static void checkGoalsOnExercise(Context ctx, QuestModel quest) {
        AvatarModel avatar = AvatarManager.loadAvatarOffline(ctx);
        if (avatar == null) return;

        // Example: Push-up goals
        int totalPushUps = getTotalProgress("pushups");
        if (totalPushUps >= 100 && avatar.getGoalState("PUSHUP_100") == GoalState.PENDING) {
            avatar.setGoalState("PUSHUP_100", GoalState.COMPLETED);
        }

        int totalSquats = getTotalProgress("squats");
        if (totalSquats >= 100 && avatar.getGoalState("SQUAT_100") == GoalState.PENDING) {
            avatar.setGoalState("SQUAT_100", GoalState.COMPLETED);
        }

        int totalSteps = getTotalProgress("steps");
        if (totalSteps >= 50000 && avatar.getGoalState("STEPS_50000") == GoalState.PENDING) {
            avatar.setGoalState("STEPS_50000", GoalState.COMPLETED);
        }

        // Level goals
        if (avatar.getLevel() >= 10 && avatar.getGoalState("LEVEL_10") == GoalState.PENDING) {
            avatar.setGoalState("LEVEL_10", GoalState.COMPLETED);
        }
        if (avatar.getLevel() >= 20 && avatar.getGoalState("LEVEL_30") == GoalState.PENDING) {
            avatar.setGoalState("LEVEL_30", GoalState.COMPLETED);
        }
        if (avatar.getLevel() >= 30 && avatar.getGoalState("LEVEL_50") == GoalState.PENDING) {
            avatar.setGoalState("LEVEL_50", GoalState.COMPLETED);
        }
        if (avatar.getLevel() >= 100 && avatar.getGoalState("LEVEL_100") == GoalState.PENDING) {
            avatar.setGoalState("LEVEL_100", GoalState.COMPLETED);
        }

        AvatarManager.saveAvatarOffline(ctx, avatar);
    }

    // Helper: Sum all completed progress for an exercise type
    private static int getTotalProgress(String exerciseType) {
        int total = 0;
        for (QuestModel q : quests) {
            if (exerciseType.equals(q.getExerciseType())) {
                total += q.getProgress();
            }
        }
        return total;
    }


    // In QuestManager.java
    public static int getIncompleteQuestCount(Context ctx) {
        ensureLoaded(ctx);
        int count = 0;
        for (QuestModel q : quests) {
            if (!q.isCompleted()) count++;
        }
        return count;
    }

    public static void reportSteps(Context ctx, int currentDailyTotal) {
        ensureLoaded(ctx);
        int delta = currentDailyTotal - lastReportedDailyTotal;
        if (delta > 0) {
            addToStepQuest(delta);
            lastReportedDailyTotal = currentDailyTotal;

            // Save updated lastReportedDailyTotal
            SharedPreferences prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            prefs.edit().putInt("last_reported_steps", lastReportedDailyTotal).apply();

            persistAll(ctx);

            for (QuestModel q : quests) {
                checkGoalsOnExercise(ctx, q);
            }
        }
    }

    static void addToStepQuest(int delta) {
        for (QuestModel q : quests) {
            if (q.getId().equals("q_daily_steps_2500") && !q.isCompleted()) {
                q.addProgress(delta);

                // Notify listener
                if (progressListener != null) {
                    progressListener.onQuestProgressUpdated(q);
                }
            }
        }
    }

    public static void resetDailyStepCounterIfNeeded(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        long lastReset = prefs.getLong("last_daily_reset", 0);

        Calendar now = Calendar.getInstance();
        Calendar resetTime = Calendar.getInstance();
        resetTime.set(Calendar.HOUR_OF_DAY, getResetHour(ctx));
        resetTime.set(Calendar.MINUTE, 0);
        resetTime.set(Calendar.SECOND, 0);
        resetTime.set(Calendar.MILLISECOND, 0);

        if (lastReset < resetTime.getTimeInMillis() && now.getTimeInMillis() >= resetTime.getTimeInMillis()) {
            lastReportedDailyTotal = 0;
            prefs.edit().putLong("last_daily_reset", now.getTimeInMillis()).apply();
        }
    }

    public static void reportDayTrained(Context ctx) {
        ensureLoaded(ctx);
        for (QuestModel q : quests) {
            if (!q.isCompleted() && "completion".equals(q.getExerciseType())) {
                // Only for weekly/monthly "day count" quests
                if (q.getId().equals("q_weekly_quests_5") || q.getId().equals("q_monthly_20days")) {
                    q.addProgress(1);
                    if (progressListener != null) {
                        progressListener.onQuestProgressUpdated(q);
                    }
                }
            }
        }
        persistAll(ctx);
    }

    /**
     * Update the "Complete 5 quests" quest progress based on claimed daily quests
     */
    private static void updateQuestCompletionQuest(Context ctx) {
        ensureLoaded(ctx);
        
        // Find the "Complete 5 quests" quest
        QuestModel completionQuest = null;
        for (QuestModel q : quests) {
            if ("q_daily_quests_5".equals(q.getId())) {
                completionQuest = q;
                break;
            }
        }
        
        if (completionQuest == null || completionQuest.isCompleted()) {
            return;
        }
        
        // Count completed daily quests (excluding the completion quest itself)
        int completedCount = 0;
        for (QuestModel q : quests) {
            if (q.getCategory() == QuestCategory.DAILY && 
                !"q_daily_quests_5".equals(q.getId()) && 
                q.isCompleted()) {
                completedCount++;
            }
        }
        
        // Update progress to match completed count
        if (completedCount != completionQuest.getProgress()) {
            completionQuest.setProgress(completedCount);
            
            if (progressListener != null) {
                progressListener.onQuestProgressUpdated(completionQuest);
            }
            
            persistAll(ctx);
        }
    }


}