package com.example.fitquest;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class Settings {

    private final Dialog dialog;
    private final Context context;
    private final FitQuestNotificationManager notificationManager;

    public Settings(Context context) {
        this.context = context;
        this.notificationManager = new FitQuestNotificationManager(context);

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.settings, null);

        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view);
        dialog.setCancelable(true);

        setupMusicControls(view);
        setupSoundControls(view);
        setupNotificationControls(view);
        setupTestingControls(view);
        setupHelpButtons(view);
        setupPendingQuests(view);
    }

    private void setupMusicControls(View view) {
        SeekBar musicSeekBar = view.findViewById(R.id.music_seekbar);
        Switch musicSwitch = view.findViewById(R.id.music_switch);

        musicSeekBar.setProgress(MusicManager.getVolume());
        musicSwitch.setChecked(MusicManager.isMusicEnabled());

        musicSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                MusicManager.setVolume(seekBar.getProgress(), context);
                Toast.makeText(context, "Music volume: " + seekBar.getProgress(), Toast.LENGTH_SHORT).show();
            }
        });

        musicSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            MusicManager.setMusicEnabled(isChecked, context);
            Toast.makeText(context, isChecked ? "Music Enabled" : "Music Disabled", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupSoundControls(View view) {
        SeekBar sfxSeekBar = view.findViewById(R.id.sfx_seekbar);
        Switch sfxSwitch = view.findViewById(R.id.sfx_switch);

        sfxSeekBar.setProgress(SoundManager.getVolume());
        sfxSwitch.setChecked(SoundManager.isSoundEnabled());

        sfxSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SoundManager.setVolume(seekBar.getProgress(), context);
                Toast.makeText(context, "SFX volume: " + seekBar.getProgress(), Toast.LENGTH_SHORT).show();
            }
        });

        sfxSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SoundManager.setSoundEnabled(isChecked, context);
            Toast.makeText(context, isChecked ? "Sound Effects Enabled" : "Sound Effects Disabled", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupNotificationControls(View view) {
        Switch notificationSwitch = view.findViewById(R.id.notification_switch);

        // Check current state
        notificationSwitch.setChecked(notificationManager.areNotificationsEnabled());

        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Request permission on Android 13+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                            != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(context, "Please allow notification permission in app settings", Toast.LENGTH_LONG).show();
                        notificationSwitch.setChecked(false);
                        return;
                    }
                }

                notificationManager.setNotificationsEnabled(true);
                notificationManager.setQuestNotificationsEnabled(true);
                notificationManager.setRewardNotificationsEnabled(true);
                notificationManager.setDailyRemindersEnabled(true);
                notificationManager.scheduleDailyReminder();

                Toast.makeText(context, "All Notifications Enabled", Toast.LENGTH_SHORT).show();

            } else {
                notificationManager.setNotificationsEnabled(false);
                notificationManager.setQuestNotificationsEnabled(false);
                notificationManager.setRewardNotificationsEnabled(false);
                notificationManager.setDailyRemindersEnabled(false);
                notificationManager.cancelDailyReminder();

                Toast.makeText(context, "All Notifications Disabled", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupTestingControls(View view) {
        Button addExpButton = view.findViewById(R.id.add_exp_button);
        Button addCoinsButton = view.findViewById(R.id.add_coins_button);
        Button addLevelButton = view.findViewById(R.id.add_level_button);

        SoundManager.setOnClickListenerWithSound(addExpButton, v -> {
            AvatarModel avatar = AvatarManager.loadAvatarOffline(context);
            if (avatar != null) {
                boolean leveledUp = avatar.addXp(100);
                AvatarManager.saveAvatarOffline(context, avatar);
                AvatarManager.saveAvatarOnline(avatar);
                
                String message = "Added 100 EXP!";
                if (leveledUp) {
                    message += " Level up! You're now level " + avatar.getLevel() + "!";
                }
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "No avatar found!", Toast.LENGTH_SHORT).show();
            }
        });

        SoundManager.setOnClickListenerWithSound(addCoinsButton, v -> {
            AvatarModel avatar = AvatarManager.loadAvatarOffline(context);
            if (avatar != null) {
                avatar.addCoins(1000);
                AvatarManager.saveAvatarOffline(context, avatar);
                AvatarManager.saveAvatarOnline(avatar);
                Toast.makeText(context, "Added 1000 coins! Total: " + avatar.getCoins(), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "No avatar found!", Toast.LENGTH_SHORT).show();
            }
        });

        SoundManager.setOnClickListenerWithSound(addLevelButton, v -> {
            AvatarModel avatar = AvatarManager.loadAvatarOffline(context);
            if (avatar != null) {
                int currentLevel = avatar.getLevel();
                int newLevel = Math.min(currentLevel + 1, 100); // Cap at level 100
                
                // Calculate XP needed for the new level
                int xpForNewLevel = LevelProgression.getMaxXpForLevel(newLevel);
                avatar.setXp(xpForNewLevel);
                avatar.setLevel(newLevel);
                
                AvatarManager.saveAvatarOffline(context, avatar);
                AvatarManager.saveAvatarOnline(avatar);
                Toast.makeText(context, "Level up! You're now level " + newLevel + "!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "No avatar found!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupHelpButtons(View view) {
        // Fully disabled help, contact, and FAQ buttons - commented out
        // Button helpBtn = view.findViewById(R.id.help_button);
        // Button contactBtn = view.findViewById(R.id.contact_button);
        // Button faqBtn = view.findViewById(R.id.faq_button);

        // Help, contact, and FAQ buttons are completely disabled and not initialized
        // They are set to GONE in the layout or commented out here
    }

    private void setupPendingQuests(View view) {
        TextView pendingQuestsText = view.findViewById(R.id.pending_quests_text);

        int pending = QuestManager.getIncompleteQuestCount(context);
        if (pending > 0) {
            pendingQuestsText.setText("You have " + pending + " quests pending!");
            pendingQuestsText.setVisibility(View.VISIBLE);

            if (notificationManager.areNotificationsEnabled() && notificationManager.areQuestNotificationsEnabled()) {
                if (hasNotificationPermission()) {
                    notificationManager.showQuestNotification(
                            "Pending Quests",
                            "You have " + pending + " quests waiting to be completed!"
                    );
                }
            }
        } else {
            pendingQuestsText.setVisibility(View.GONE);
        }
    }

    private boolean hasNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true; // Below Android 13
    }

    public void show() {
        dialog.show();
    }
}
