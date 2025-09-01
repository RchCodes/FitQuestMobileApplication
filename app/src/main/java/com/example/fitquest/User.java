package com.example.fitquest;

import android.content.Context;
import android.content.SharedPreferences;

public class User {

    private static final String PREFS_NAME = "fitquest_prefs";
    private static final String KEY_USER_LEVEL = "user_level";

    public static int getLevel(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_USER_LEVEL, 1); // default level 1
    }

    public static void setLevel(Context context, int level) {
        int clamped = Math.max(1, Math.min(30, level));
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_USER_LEVEL, clamped).apply();
    }

    public static String getDifficultyLevel(Context context) {
        int level = getLevel(context);
        return difficultyFromLevel(level);
    }

    public static String difficultyFromLevel(int level) {
        if (level >= 30) return "master";
        if (level >= 24) return "expert";
        if (level >= 10) return "advanced";
        return "beginner"; // 1-9
    }
}
