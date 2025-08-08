package com.example.fitquest;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

public class Settings {

    private final Dialog dialog;

    public Settings(Context context) {
        // Inflate layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.settings, null);

        // Set up dialog
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view);
        dialog.setCancelable(true);

        // Show SeekBar interaction
        SeekBar musicSeekBar = view.findViewById(R.id.music_seekbar);
        SeekBar sfxSeekBar = view.findViewById(R.id.sfx_seekbar);

        musicSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(context, "Music volume: " + seekBar.getProgress(), Toast.LENGTH_SHORT).show();
            }
        });

        sfxSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(context, "SFX volume: " + seekBar.getProgress(), Toast.LENGTH_SHORT).show();
            }
        });

        // Notification switch
        Switch notificationSwitch = view.findViewById(R.id.notification_switch);
        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Toast.makeText(context, isChecked ? "Notifications Enabled" : "Notifications Disabled", Toast.LENGTH_SHORT).show();
        });

        // Help buttons
        Button helpBtn = view.findViewById(R.id.help_button);
        Button contactBtn = view.findViewById(R.id.contact_button);
        Button faqBtn = view.findViewById(R.id.faq_button);

        helpBtn.setOnClickListener(v -> Toast.makeText(context, "Help clicked", Toast.LENGTH_SHORT).show());
        contactBtn.setOnClickListener(v -> Toast.makeText(context, "Contact Us clicked", Toast.LENGTH_SHORT).show());
        faqBtn.setOnClickListener(v -> Toast.makeText(context, "FAQ clicked", Toast.LENGTH_SHORT).show());
    }

    public void show() {
        dialog.show();
    }
}
