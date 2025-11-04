package com.example.fitquest;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Calendar;

/**
 * Manages daily battle limits and tracking
 */
public class DailyBattleManager {
    
    private static final String TAG = "DailyBattleManager";
    private static final String PREFS_NAME = "daily_battle_prefs";
    private static final String KEY_BATTLE_COUNT = "battle_count";
    private static final String KEY_LAST_BATTLE_DATE = "last_battle_date";
    private static final int MAX_DAILY_BATTLES = 10;
    
    /**
     * Check if player can battle today
     */
    public static boolean canBattleToday(Context context) {
        int battlesToday = getBattlesToday(context);
        return battlesToday < MAX_DAILY_BATTLES;
    }
    
    /**
     * Get number of battles fought today
     */
    public static int getBattlesToday(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String lastBattleDate = prefs.getString(KEY_LAST_BATTLE_DATE, "");
        String today = getTodayString();
        
        if (!today.equals(lastBattleDate)) {
            // New day, reset count
            return 0;
        }
        
        return prefs.getInt(KEY_BATTLE_COUNT, 0);
    }
    
    /**
     * Get remaining battles for today
     */
    public static int getRemainingBattles(Context context) {
        return MAX_DAILY_BATTLES - getBattlesToday(context);
    }
    
    /**
     * Record a battle
     */
    public static void recordBattle(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String lastBattleDate = prefs.getString(KEY_LAST_BATTLE_DATE, "");
        String today = getTodayString();
        
        int currentCount = 0;
        if (today.equals(lastBattleDate)) {
            currentCount = prefs.getInt(KEY_BATTLE_COUNT, 0);
        }
        
        prefs.edit()
            .putString(KEY_LAST_BATTLE_DATE, today)
            .putInt(KEY_BATTLE_COUNT, currentCount + 1)
            .apply();
            
        Log.d(TAG, "Battle recorded. Battles today: " + (currentCount + 1));
    }
    
    /**
     * Reset daily battles (for testing or admin purposes)
     */
    public static void resetDailyBattles(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
            .remove(KEY_BATTLE_COUNT)
            .remove(KEY_LAST_BATTLE_DATE)
            .apply();
    }
    
    /**
     * Get today's date as string for comparison
     */
    private static String getTodayString() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1; // Calendar.MONTH is 0-based
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return year + "-" + month + "-" + day;
    }
    
    /**
     * Get time until next battle reset (in hours)
     */
    public static int getHoursUntilReset() {
        Calendar cal = Calendar.getInstance();
        int hoursUntilMidnight = 24 - cal.get(Calendar.HOUR_OF_DAY);
        return hoursUntilMidnight;
    }
}
