package com.example.fitquest;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ChallengeManager
 * ----------------
 * Handles loading, saving, and updating of one-time fitness challenges.
 * Challenges can be strict-mode (fail if posture breaks), time/reps based,
 * and linked to specific Goals (GoalState).
 */
public class ChallengeManager {

    private static final String PREFS_NAME = "FitQuest_Challenges";
    private static final String KEY_CHALLENGES = "Challenges_Data";
    private static final String TAG = "ChallengeManager";

    private static List<ChallengeModel> challengeCache = new ArrayList<>();
    private static final Gson gson = new Gson();

    /**
     * Initialize challenges only once or refresh from storage
     */
    public static void init(Context context) {
        challengeCache = loadFromPrefs(context);

        if (challengeCache == null || challengeCache.isEmpty()) {
            challengeCache = getDefaultChallenges();
            saveToPrefs(context, challengeCache);
            Log.i(TAG, "Default challenges loaded.");
        } else {
            Log.i(TAG, "Loaded " + challengeCache.size() + " challenges.");
        }
    }

    /**
     * Get all challenges
     */
    public static List<ChallengeModel> getAll(Context context) {
        if (challengeCache == null || challengeCache.isEmpty()) {
            init(context);
        }
        return challengeCache;
    }

    /**
     * Get challenge by ID
     */
    @Nullable
    public static ChallengeModel getById(Context context, String id) {
        for (ChallengeModel c : getAll(context)) {
            if (c.getId().equals(id)) return c;
        }
        return null;
    }

    /**
     * Mark a challenge as completed
     */
    public static void markCompleted(Context context, String challengeId) {
        ChallengeModel challenge = getById(context, challengeId);
        if (challenge != null && !challenge.isCompleted()) {
            challenge.setCompleted(true);
            saveToPrefs(context, challengeCache);
            Log.i(TAG, "Challenge completed: " + challenge.getName());
        }
    }

    /**
     * Claim a challenge reward
     */
        public static void claimReward(Context context, AvatarModel avatar, String challengeId) {
        ChallengeModel challenge = getById(context, challengeId);
        if (challenge == null || challenge.isClaimed() || !challenge.isCompleted()) return;

        avatar.addAvatarBadge(challenge.getRewardBadge());
        avatar.addCoins(challenge.getRewardCoins());
        challenge.setClaimed(true);
        saveToPrefs(context, challengeCache);
        AvatarManager.saveAvatarOffline(context, avatar);
                AvatarManager.saveAvatarOnline(avatar);

        Log.i(TAG, "Challenge reward claimed: " + challenge.getName());
    }


    private static void saveToPrefs(Context context, List<ChallengeModel> challenges) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_CHALLENGES, gson.toJson(challenges)).apply();
    }

    /**
     * Load challenges from SharedPreferences
     */
    private static List<ChallengeModel> loadFromPrefs(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_CHALLENGES, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<ChallengeModel>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    /** Default one-time strict challenges (24) */
    /**
     * Default one-time strict challenges (24 total)
     */
    static List<ChallengeModel> getDefaultChallenges() {
        List<ChallengeModel> list = new ArrayList<>();

        // Lv 1 – Beginner (Reps start at 15)
        list.add(new ChallengeModel(UUID.randomUUID().toString(), "Squat Starter", "squats",
                "Perform 15 proper squats with controlled form", 15, 60, "Beginner", R.drawable.badge_squat_100, 50, 1, true, "CHAL_SQUAT_15"));
        list.add(new ChallengeModel(UUID.randomUUID().toString(), "Push-Up Beginner", "pushups",
                "Complete 15 push-ups with full range of motion", 15, 60, "Beginner", R.drawable.badge_squat_100, 50, 1, true, "CHAL_PUSH_15"));
        list.add(new ChallengeModel(UUID.randomUUID().toString(), "Core Beginner", "crunches",
                "Do 15 controlled crunches", 15, 60, "Beginner", R.drawable.badge_squat_100, 50, 1, true, "CHAL_CRUNCH_15"));

        // Lv 2 – Balance & Cardio
        list.add(new ChallengeModel(UUID.randomUUID().toString(), "Plank Beginner", "plank",
                "Hold an elbow plank for 35 seconds without wobble", 35, 45, "Beginner", R.drawable.badge_squat_100, 60, 2, true, "CHAL_PLANK_35"));
        list.add(new ChallengeModel(UUID.randomUUID().toString(), "Jumping Start", "jumpingjacks",
                "Perform 70 jumping jacks at steady rhythm", 70, 90, "Beginner", R.drawable.badge_squat_100, 60, 2, true, "CHAL_JJ_70"));
        list.add(new ChallengeModel(UUID.randomUUID().toString(), "Tree Balance", "treepose",
                "Maintain Tree Pose for 35 seconds with stable balance", 35, 35, "Beginner", R.drawable.badge_squat_100, 60, 2, true, "CHAL_TREE_35"));

        // Lv 3 – Intermediate (Reps 25–40)
        list.add(new ChallengeModel(UUID.randomUUID().toString(), "Lunge Intermediate", "lunges",
                "Alternate lunges for a total of 20 reps (10 per leg)", 20, 90, "Intermediate", R.drawable.badge_squat_100, 75, 3, true, "CHAL_LUNGE_20"));
        list.add(new ChallengeModel(UUID.randomUUID().toString(), "Push-Up Intermediate", "pushups",
                "Complete 25 push-ups at a steady pace", 25, 90, "Intermediate", R.drawable.badge_squat_100, 75, 3, true, "CHAL_PUSH_25"));
        list.add(new ChallengeModel(UUID.randomUUID().toString(), "Core Intermediate", "situps",
                "Perform 25 sit-ups with full range of motion", 25, 90, "Intermediate", R.drawable.badge_squat_100, 75, 3, true, "CHAL_SITUP_25"));
        list.add(new ChallengeModel(UUID.randomUUID().toString(), "Cardio Boost", "jumpingjacks",
                "Complete 100 jumping jacks nonstop", 100, 120, "Intermediate", R.drawable.badge_squat_100, 90, 3, true, "CHAL_JJ_100"));

        // Lv 4 – Advanced (Reps 40–60)
        list.add(new ChallengeModel(UUID.randomUUID().toString(), "Squat Advanced", "squats",
                "Perform 40 deep squats with proper depth", 40, 90, "Advanced", R.drawable.badge_squat_100, 90, 4, true, "CHAL_SQUAT_40"));
        list.add(new ChallengeModel(UUID.randomUUID().toString(), "Plank Advanced", "plank",
                "Hold plank for 60 seconds with >90% posture accuracy", 60, 60, "Advanced", R.drawable.badge_squat_100, 100, 4, true, "CHAL_PLANK_60"));
        list.add(new ChallengeModel(UUID.randomUUID().toString(), "Tree Pose Advanced", "treepose",
                "Hold Tree Pose for 60 seconds without swaying", 60, 60, "Advanced", R.drawable.badge_squat_100, 90, 4, true, "CHAL_TREE_60"));

        // Lv 5 – Expert (Reps 60–80)
        list.add(new ChallengeModel(UUID.randomUUID().toString(), "Crunch Expert", "crunches",
                "Do 60 crunches with full control and no neck strain", 60, 120, "Expert", R.drawable.badge_squat_100, 110, 5, true, "CHAL_CRUNCH_60"));
        list.add(new ChallengeModel(UUID.randomUUID().toString(), "Sit-Up Expert", "situps",
                "Complete 60 sit-ups maintaining full range", 60, 120, "Expert", R.drawable.badge_squat_100, 110, 5, true, "CHAL_SITUP_60"));
        list.add(new ChallengeModel(UUID.randomUUID().toString(), "Squat Expert", "squats",
                "Perform 60 squats in 3 minutes with proper form", 60, 180, "Expert", R.drawable.badge_squat_100, 125, 5, true, "CHAL_SQUAT_60"));
        list.add(new ChallengeModel(UUID.randomUUID().toString(), "Push-Up Expert", "pushups",
                "Complete 60 push-ups without losing form", 60, 180, "Expert", R.drawable.badge_squat_100, 125, 5, true, "CHAL_PUSH_60"));

        // Lv 6 – Elite (Reps 80–100)
        list.add(new ChallengeModel(UUID.randomUUID().toString(), "Plank Elite", "plank",
                "Hold plank for 90 seconds with solid core engagement", 90, 90, "Elite", R.drawable.badge_squat_100, 150, 6, true, "CHAL_PLANK_90"));
        list.add(new ChallengeModel(UUID.randomUUID().toString(), "Lunge Elite", "lunges",
                "Execute 80 perfect lunges (total) without knee collapse", 80, 150, "Elite", R.drawable.badge_squat_100, 150, 6, true, "CHAL_LUNGE_80"));
        list.add(new ChallengeModel(UUID.randomUUID().toString(), "Jumping Agility Elite", "jumpingjacks",
                "Perform 100 jumping jacks nonstop", 100, 180, "Elite", R.drawable.badge_squat_100, 175, 6, true, "CHAL_JJ_100"));

        // Lv 7 – Legendary (Reps 120+)
        list.add(new ChallengeModel(UUID.randomUUID().toString(), "Tree Pose Legendary", "treepose",
                "Hold Tree Pose for 120 seconds with minimal sway", 120, 120, "Legendary", R.drawable.badge_squat_100, 175, 7, true, "CHAL_TREE_120"));
        list.add(new ChallengeModel(UUID.randomUUID().toString(), "Sit-Up Legendary", "situps",
                "Perform 100 sit-ups maintaining form throughout", 100, 300, "Legendary", R.drawable.badge_squat_100, 200, 7, true, "CHAL_SITUP_100"));

        // Lv 8–9 – Ultimate
        list.add(new ChallengeModel(UUID.randomUUID().toString(), "Ultimate Squat Challenge", "squats",
                "Perform 120 deep squats maintaining proper posture", 120, 300, "Ultimate", R.drawable.badge_squat_100, 300, 9, true, "CHAL_ULTIMATE"));
        list.add(new ChallengeModel(UUID.randomUUID().toString(), "Push-Up Ultimate", "pushups",
                "Perform 100 push-ups with perfect form", 100, 240, "Ultimate", R.drawable.badge_squat_100, 250, 8, true, "CHAL_HERO_TRIAL"));

        return list;
    }



    public static ChallengeModel getChallengeByGoalId(String goalId) {
        for (ChallengeModel c : challengeCache) {
            if (c.getLinkedGoalId().equals(goalId)) return c;
        }
        return null;
    }


}
