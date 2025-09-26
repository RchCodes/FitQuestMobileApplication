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

    // Listener for UI updates
    public interface QuestProgressListener {
        void onQuestProgressUpdated(QuestModel quest);
        void onQuestCompleted(QuestModel quest, boolean leveledUp);
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

            String lowerTitle = q.getTitle() == null ? "" : q.getTitle().toLowerCase();
            String lowerDesc  = q.getDescription() == null ? "" : q.getDescription().toLowerCase();

            if (lowerTitle.contains(exerciseType.toLowerCase()) || lowerDesc.contains(exerciseType.toLowerCase())) {
                q.addProgress(amount); // only increment progress
                changed = true;
            }
        }

        if (changed) persistAll(ctx);
    }


    // --- Apply quest rewards ---
    public static boolean applyRewards(Context ctx, QuestModel quest) {
        AvatarModel avatar = AvatarManager.loadAvatarOffline(ctx);
        if (avatar == null) return false;

        QuestReward r = quest.getReward();
        if (r == null) return false;

        // Coins & XP
        avatar.setCoins(avatar.getCoins() + r.getCoins());
        boolean leveledUp = avatar.addXp(r.getXp());

        // Body points
        if (r.getArmPoints() > 0) avatar.addArmPoints(r.getArmPoints());
        if (r.getLegPoints() > 0) avatar.addLegPoints(r.getLegPoints());

        // Attribute points
        CharacterStats stats = new CharacterStats(ctx);
        stats.addPhysiquePoints(r.getArmPoints() + r.getLegPoints());
        stats.addAttributePoints(r.getAttributePoints());

        // Save avatar
        AvatarManager.saveAvatarOffline(ctx, avatar);
        AvatarManager.saveAvatarOnline(avatar);

        // Mark quest completed
        quest.complete();
        persistAll(ctx);

        return leveledUp;
    }

    // --- Claim quest manually ---
    public static boolean claimQuest(Context ctx, QuestModel quest) {
        if (quest == null || !quest.isCompleted() || quest.isClaimed()) return false;

        boolean leveledUp = applyRewards(ctx, quest);
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
                new QuestReward(20,50,1,0,0,0,1,0), QuestCategory.DAILY,10,"pushups"));
        list.add(new QuestModel("q_daily_squat_15", "Execute 15 Squats", "Do 15 squats in one session",
                new QuestReward(15,40,0,1,0,0,1,0), QuestCategory.DAILY,15,"squats"));
        list.add(new QuestModel("q_daily_plank_60", "Hold Plank 60 Seconds", "Maintain a plank for 60 seconds",
                new QuestReward(25,60,0,0,1,1,1,1), QuestCategory.DAILY,60,"plank"));
        list.add(new QuestModel("q_daily_crunches_20", "Do 20 Crunches", "Complete 20 crunches in one session",
                new QuestReward(15,40,0,0,1,1,1,0), QuestCategory.DAILY,20,"crunches"));

        // Weekly
        list.add(new QuestModel("q_weekly_push_100", "Accumulate 100 Push-ups", "Do 100 pushups this week",
                new QuestReward(100,200,0,0,0,0,0,0), QuestCategory.WEEKLY,100,"pushups"));
        list.add(new QuestModel("q_weekly_squat_150", "Accumulate 150 Squats", "Do 150 squats this week",
                new QuestReward(90,180,0,0,0,0,0,0), QuestCategory.WEEKLY,150,"squats"));

        // Monthly
        list.add(new QuestModel("q_monthly_20days", "Train 20 Days This Month", "Log exercise on 20 separate days",
                new QuestReward(300,500,0,0,0,0,0,0), QuestCategory.MONTHLY,20,""));
        list.add(new QuestModel("q_monthly_50push", "Accumulate 50 Push-ups This Month", "Do 50 pushups this month",
                new QuestReward(150,250,0,0,0,0,0,0), QuestCategory.MONTHLY,50,"pushups"));

        return list;
    }

    public static List<QuestModel> getQuestsByCategory(Context ctx, QuestCategory category) {
        ensureLoaded(ctx); // lazy-load quests if not already loaded
        List<QuestModel> result = new ArrayList<>();
        for (QuestModel q : quests) {
            if (q.getCategory() == category) {
                result.add(q);
            }
        }
        return result;
    }

}
