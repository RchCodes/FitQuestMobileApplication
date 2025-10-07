package com.example.fitquest;

import android.app.Dialog;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class PauseDialog {

    final Dialog dialog;

    public PauseDialog(Context context, Runnable onQuit) {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_pause);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCanceledOnTouchOutside(true);

        SeekBar seekMusic = dialog.findViewById(R.id.seekMusic);
        SeekBar seekSfx = dialog.findViewById(R.id.seekSfx);
        ImageView btnQuit = dialog.findViewById(R.id.btnQuit);

        // Initialize current volumes
        seekMusic.setProgress(MusicManager.getVolume());
        seekSfx.setProgress(SoundManager.getVolume());

        // Listeners
        seekMusic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                MusicManager.setVolume(progress, context);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        seekSfx.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                SoundManager.setVolume(progress, context);
                SoundManager.playButtonClick(); // optional feedback
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        btnQuit.setOnClickListener(v -> {
            SoundManager.playButtonClick();
            if (onQuit != null) onQuit.run();
            dialog.dismiss();
        });

        // Tap outside to close
        View root = dialog.findViewById(android.R.id.content);
        root.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) dialog.dismiss();
            return true;
        });
    }

    public void show() { dialog.show(); }
    public void dismiss() { dialog.dismiss(); }
}
