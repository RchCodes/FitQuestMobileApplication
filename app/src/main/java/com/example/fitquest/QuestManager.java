package com.example.fitquest;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class QuestManager {

    private static final String PREFS = "FitQuestPrefs";
    private static final String RESET_HOUR_KEY = "quest_reset_hour";
    private static final int DEFAULT_RESET_HOUR = 4;

    private static List<QuestModel> quests = null;

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

                q.addProgress(amount);
                changed = true;

                if (progressListener != null) {
                    progressListener.onQuestProgressUpdated(q);
                }
            }
        }

        if (changed) {
            persistAll(ctx);
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
        list.add(new QuestModel("q_daily_squat_15", "Execute 15 Squats", "Do 15 squats in one session",
                new QuestReward(15,40,0,1,0,0,1,0), QuestCategory.DAILY,15,"squats"));
        list.add(new QuestModel("q_daily_plank_30", "Hold Plank 30 Seconds", "Maintain a plank for 30 seconds",
                new QuestReward(25,50,0,0,1,1,1,1), QuestCategory.DAILY,30,"plank"));
        list.add(new QuestModel("q_daily_crunches_20", "Do 20 Crunches", "Complete 20 crunches in one session",
                new QuestReward(15,40,0,0,1,1,1,0), QuestCategory.DAILY,20,"crunches"));
        list.add(new QuestModel("q_daily_steps_5000", "Walk 5,000 Steps", "Accumulate 5,000 steps today using your device's step counter",
                new QuestReward(40, 40, 0, 0, 0, 0, 0, 0), QuestCategory.DAILY, 5000, "steps"));
        list.add(new QuestModel("q_daily_quests_5", "Complete 5 Quests", "Complete 5 quests in today",
                new QuestReward(100, 50, 0, 0, 0, 0, 0, 5), QuestCategory.DAILY, 5, "completion"));

        // Weekly
        list.add(new QuestModel("q_weekly_push_50", "Accumulate 50 Push-ups", "Do 50 pushups this week",
                new QuestReward(100,200,0,0,0,0,0,3), QuestCategory.WEEKLY,50,"pushups"));
        list.add(new QuestModel("q_weekly_squat_75", "Accumulate 150 Squats", "Do 75 squats this week",
                new QuestReward(90,180,0,0,0,0,0,2), QuestCategory.WEEKLY,75,"squats"));
        list.add(new QuestModel("q_weekly_plank_150", "Accumulate 150 Plank Seconds", "Maintain a plank for 150 seconds this week",
                new QuestReward(75,60,0,0,0,0,0,3), QuestCategory.WEEKLY,300,"plank"));
        list.add(new QuestModel("q_weekly_crunches_100", "Accumulate 100 Crunches", "Complete 100 crunches this week",
                new QuestReward(60,40,0,0,0,0,0,2), QuestCategory.WEEKLY,100,"crunches"));
        list.add(new QuestModel("q_weekly_steps_25000", "Walk 25,000 Steps", "Accumulate 25,000 this week using your device's step counter",
                new QuestReward(250, 150, 0, 0, 0, 0, 0, 2), QuestCategory.WEEKLY, 25000, "steps"));
        list.add(new QuestModel("q_weekly_quests_5", "Train 5 Days this week", "Log exercise on 5 separate days",
                new QuestReward(250, 50, 0, 0, 0, 0, 0, 5), QuestCategory.WEEKLY, 5, "completion"));

        // Monthly
        list.add(new QuestModel("q_monthly_20days", "Train 20 Days This Month", "Log exercise on 20 separate days",
                new QuestReward(300,500,0,0,0,0,0,4), QuestCategory.MONTHLY,20,"completion"));
        list.add(new QuestModel("q_monthly_200push", "Accumulate 200 Push-ups This Month", "Do 200 pushups this month",
                new QuestReward(350,250,0,0,0,0,0,5), QuestCategory.MONTHLY,200,"pushups"));
        list.add(new QuestModel("q_monthly_squat_600", "Accumulate 600 Squats", "Do 150 squats this month",
                new QuestReward(90,180,0,0,0,0,0,7), QuestCategory.MONTHLY,600,"squats"));
        list.add(new QuestModel("q_monthly_plank_1200", "Accumulate 1200 Plank Seconds", "Maintain a plank for 1200 seconds this month",
                new QuestReward(75,60,0,0,0,0,0,10), QuestCategory.MONTHLY,1200,"plank"));
        list.add(new QuestModel("q_monthly_crunches_400", "Accumulate 400 Crunches", "Complete 400 crunches this month",
                new QuestReward(60,40,0,0,0,0,0,8), QuestCategory.MONTHLY,400,"crunches"));
        list.add(new QuestModel("q_monthly_steps_90000", "Walk 90,000 Steps", "Accumulate 90,000 this month using your device's step counter",
                new QuestReward(900, 300, 0, 0, 0, 0, 0, 10), QuestCategory.MONTHLY, 90000, "steps"));

        return list;
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




}
