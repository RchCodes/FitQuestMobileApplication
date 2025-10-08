package com.example.fitquest;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

/**
 * FitQuestNotificationManager
 * Handles all notifications for FitQuest (quests, rewards, achievements, etc.)
 * Supports user preferences and Android 8+ notification channels.
 */
public class FitQuestNotificationManager {

    // === CHANNEL CONFIG ===
    private static final String CHANNEL_ID = "fitquest_notifications";
    private static final String CHANNEL_NAME = "FitQuest Notifications";
    private static final String CHANNEL_DESCRIPTION = "Notifications for quests, rewards, and events";

    // === NOTIFICATION IDS ===
    public static final int QUEST_NOTIFICATION_ID = 1001;
    public static final int REWARD_NOTIFICATION_ID = 1002;
    public static final int LEVEL_UP_NOTIFICATION_ID = 1003;
    public static final int DAILY_REMINDER_NOTIFICATION_ID = 1004;
    public static final int ACHIEVEMENT_NOTIFICATION_ID = 1005;

    // === SHARED PREFERENCES ===
    private static final String PREFS_NAME = "fitquest_notifications";
    private static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";
    private static final String KEY_QUEST_NOTIFICATIONS = "quest_notifications";
    private static final String KEY_REWARD_NOTIFICATIONS = "reward_notifications";
    private static final String KEY_DAILY_REMINDERS = "daily_reminders";
    private static final String KEY_LEVEL_UP_NOTIFICATIONS = "level_up_notifications";
    private static final String KEY_ACHIEVEMENT_NOTIFICATIONS = "achievement_notifications";

    private static boolean channelCreated = false;

    private final Context context;
    private final android.app.NotificationManager systemNotificationManager;
    private final SharedPreferences prefs;

    public FitQuestNotificationManager(Context context) {
        this.context = context.getApplicationContext();
        this.systemNotificationManager =
                (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        createNotificationChannel();
        checkAndRequestNotificationPermission(); // 👈 add permission check on init
    }

    // === PERMISSION HANDLER (Android 13+) ===
    private void checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                // Show a gentle message for the user (since we can’t request permission directly from a Context)
                Toast.makeText(context,
                        "Please enable notifications for FitQuest in App Settings to stay updated!",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    // === CHANNEL CREATION ===
    private void createNotificationChannel() {
        if (channelCreated || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                android.app.NotificationManager.IMPORTANCE_DEFAULT
        );
        channel.setDescription(CHANNEL_DESCRIPTION);
        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setShowBadge(true);

        systemNotificationManager.createNotificationChannel(channel);
        channelCreated = true;
    }

    // === PREFERENCES ===
    public boolean areNotificationsEnabled() {
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true);
    }

    public void setNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply();
    }

    public boolean areQuestNotificationsEnabled() {
        return prefs.getBoolean(KEY_QUEST_NOTIFICATIONS, true);
    }

    public void setQuestNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_QUEST_NOTIFICATIONS, enabled).apply();
    }

    public boolean areRewardNotificationsEnabled() {
        return prefs.getBoolean(KEY_REWARD_NOTIFICATIONS, true);
    }

    public void setRewardNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_REWARD_NOTIFICATIONS, enabled).apply();
    }

    public boolean areDailyRemindersEnabled() {
        return prefs.getBoolean(KEY_DAILY_REMINDERS, true);
    }

    public void setDailyRemindersEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_DAILY_REMINDERS, enabled).apply();
    }

    public boolean areLevelUpNotificationsEnabled() {
        return prefs.getBoolean(KEY_LEVEL_UP_NOTIFICATIONS, true);
    }

    public void setLevelUpNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_LEVEL_UP_NOTIFICATIONS, enabled).apply();
    }

    public boolean areAchievementNotificationsEnabled() {
        return prefs.getBoolean(KEY_ACHIEVEMENT_NOTIFICATIONS, true);
    }

    public void setAchievementNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_ACHIEVEMENT_NOTIFICATIONS, enabled).apply();
    }

    // === NOTIFICATION HELPERS ===
    private PendingIntent buildPendingIntent(Intent intent, int requestCode) {
        return PendingIntent.getActivity(
                context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private Uri getDefaultSound() {
        return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    }

    private boolean canPostNotifications() {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    // === NOTIFICATION TYPES ===
    public void showQuestNotification(String title, String message) {
        if (!areNotificationsEnabled() || !areQuestNotificationsEnabled() || !canPostNotifications()) return;

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("open_quests", true);

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(buildPendingIntent(intent, QUEST_NOTIFICATION_ID))
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setSound(getDefaultSound())
                .build();

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
        {return;
        }
        NotificationManagerCompat.from(context).notify(QUEST_NOTIFICATION_ID, notification);
    }

    public void showRewardNotification(String title, String message) {
        if (!areNotificationsEnabled() || !areRewardNotificationsEnabled() || !canPostNotifications()) return;

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("show_rewards", true);

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_reward)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(buildPendingIntent(intent, REWARD_NOTIFICATION_ID))
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
                .setVibrate(new long[]{0, 200, 100, 200})
                .setSound(getDefaultSound())
                .build();

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        NotificationManagerCompat.from(context).notify(REWARD_NOTIFICATION_ID, notification);
    }

    public void showLevelUpNotification(String playerName, int newLevel) {
        if (!areNotificationsEnabled() || !areLevelUpNotificationsEnabled() || !canPostNotifications()) return;

        String title = "Level Up!";
        String message = playerName + " reached level " + newLevel + "!";

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("show_level_up", true);

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_level_up)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(buildPendingIntent(intent, LEVEL_UP_NOTIFICATION_ID))
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .setVibrate(new long[]{0, 300, 150, 300})
                .setSound(getDefaultSound())
                .build();

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        NotificationManagerCompat.from(context).notify(LEVEL_UP_NOTIFICATION_ID, notification);
    }

    public void showAchievementNotification(String title, String message) {
        if (!areNotificationsEnabled() || !areAchievementNotificationsEnabled() || !canPostNotifications()) return;

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("show_achievements", true);

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_achievement)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(buildPendingIntent(intent, ACHIEVEMENT_NOTIFICATION_ID))
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setSound(getDefaultSound())
                .build();

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
        {return;
        }
        NotificationManagerCompat.from(context).notify(ACHIEVEMENT_NOTIFICATION_ID, notification);
    }

    public void showDailyReminderNotification() {
        if (!areNotificationsEnabled() || !areDailyRemindersEnabled() || !canPostNotifications()) return;

        String title = "Daily Quest Reminder";
        String message = "Don’t forget to complete your daily quests for rewards!";

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("open_quests", true);

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_daily_quest)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(buildPendingIntent(intent, DAILY_REMINDER_NOTIFICATION_ID))
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setVibrate(new long[]{0, 150, 75, 150})
                .setSound(getDefaultSound())
                .build();

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        NotificationManagerCompat.from(context).notify(DAILY_REMINDER_NOTIFICATION_ID, notification);
    }

    // === CANCEL METHODS ===
    public void cancelNotification(int notificationId) {
        NotificationManagerCompat.from(context).cancel(notificationId);
    }

    public void cancelAllNotifications() {
        NotificationManagerCompat.from(context).cancelAll();
    }

    // === STATIC HELPERS ===
    public static void notifyQuestCompleted(Context context, String questName) {
        new FitQuestNotificationManager(context)
                .showRewardNotification("Quest Completed!", "You completed: " + questName);
    }

    public static void notifyLevelUp(Context context, String playerName, int newLevel) {
        new FitQuestNotificationManager(context)
                .showLevelUpNotification(playerName, newLevel);
    }

    public static void notifyDailyReminder(Context context) {
        new FitQuestNotificationManager(context)
                .showDailyReminderNotification();
    }

    public static void notifyAchievement(Context context, String achievementName, String description) {
        new FitQuestNotificationManager(context)
                .showAchievementNotification("Achievement Unlocked!", achievementName + ": " + description);
    }

    public void scheduleDailyReminder() {
        // Future implementation for WorkManager or AlarmManager scheduling
    }

    public void cancelDailyReminder() {
    }
}
