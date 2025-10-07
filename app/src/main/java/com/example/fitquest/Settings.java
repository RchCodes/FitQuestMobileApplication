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

    private void setupHelpButtons(View view) {
        Button helpBtn = view.findViewById(R.id.help_button);
        Button contactBtn = view.findViewById(R.id.contact_button);
        Button faqBtn = view.findViewById(R.id.faq_button);

        helpBtn.setOnClickListener(v ->
                Toast.makeText(context, "Help documentation coming soon!", Toast.LENGTH_SHORT).show());

        contactBtn.setOnClickListener(v -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:support@fitquest.com"));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "FitQuest Support");
            emailIntent.putExtra(Intent.EXTRA_TEXT, "Hello FitQuest Support,\n\n");

            if (emailIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(emailIntent);
            } else {
                Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show();
            }
        });

        faqBtn.setOnClickListener(v ->
                Toast.makeText(context, "FAQ page coming soon!", Toast.LENGTH_SHORT).show());
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
