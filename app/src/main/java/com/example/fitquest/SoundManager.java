package com.example.fitquest;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.view.View;

public class SoundManager {
    
    private static SoundPool soundPool;
    private static int volume = 50; // 0-100
    private static boolean soundEnabled = true;
    private static boolean isInitialized = false;
    
    private static final String PREFS_NAME = "fitquest_sound";
    private static final String KEY_VOLUME = "sound_volume";
    private static final String KEY_ENABLED = "sound_enabled";
    
    // Sound effect IDs
    private static int buttonClickSoundId;
    private static int questCompleteSoundId;
    private static int levelUpSoundId;
    private static int combatHitSoundId;
    
    public static void initialize(Context context) {
        if (isInitialized) return;
        
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        volume = prefs.getInt(KEY_VOLUME, 50);
        soundEnabled = prefs.getBoolean(KEY_ENABLED, true);
        
        // Initialize SoundPool
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build();
            
        soundPool = new SoundPool.Builder()
            .setMaxStreams(10)
            .setAudioAttributes(audioAttributes)
            .build();
            
        // Load sound effects (you'll need to add these to res/raw/)
        try {
            buttonClickSoundId = soundPool.load(context, R.raw.button_click, 1);
            questCompleteSoundId = soundPool.load(context, R.raw.quest_complete, 1);
            levelUpSoundId = soundPool.load(context, R.raw.level_up, 1);
            combatHitSoundId = soundPool.load(context, R.raw.combat_hit, 1);
        } catch (Exception e) {
            // Handle missing sound files gracefully
            e.printStackTrace();
        }
        
        isInitialized = true;
    }
    
    public static void playButtonClick() {
        if (!soundEnabled || soundPool == null) return;
        soundPool.play(buttonClickSoundId, volume / 100f, volume / 100f, 1, 0, 1f);
    }
    
    /**
     * Add click sound to any View (Button, ImageView, etc.)
     */
    public static void addClickSound(View view) {
        if (view == null) return;
        
        view.setOnClickListener(v -> {
            playButtonClick();
            // Call the original click listener if it exists
            if (v.getTag() instanceof View.OnClickListener) {
                ((View.OnClickListener) v.getTag()).onClick(v);
            }
        });
    }
    
    /**
     * Set click listener with sound for any View
     */
    public static void setOnClickListenerWithSound(View view, View.OnClickListener listener) {
        if (view == null) return;
        
        view.setTag(listener); // Store original listener
        view.setOnClickListener(v -> {
            playButtonClick();
            if (listener != null) {
                listener.onClick(v);
            }
        });
    }
    
    public static void playQuestComplete() {
        if (!soundEnabled || soundPool == null) return;
        soundPool.play(questCompleteSoundId, volume / 100f, volume / 100f, 1, 0, 1f);
    }
    
    public static void playLevelUp() {
        if (!soundEnabled || soundPool == null) return;
        soundPool.play(levelUpSoundId, volume / 100f, volume / 100f, 1, 0, 1f);
    }
    
    public static void playCombatHit() {
        if (!soundEnabled || soundPool == null) return;
        soundPool.play(combatHitSoundId, volume / 100f, volume / 100f, 1, 0, 1f);
    }
    
    public static void setVolume(int vol, Context context) {
        volume = Math.max(0, Math.min(100, vol)); // Clamp between 0-100
        
        // Save to preferences
        if (context != null) {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit().putInt(KEY_VOLUME, volume).apply();
        }
    }
    
    public static void setSoundEnabled(boolean enabled, Context context) {
        soundEnabled = enabled;
        
        // Save to preferences
        if (context != null) {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit().putBoolean(KEY_ENABLED, soundEnabled).apply();
        }
    }
    
    public static int getVolume() {
        return volume;
    }
    
    public static boolean isSoundEnabled() {
        return soundEnabled;
    }
    
    public static void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
            isInitialized = false;
        }
    }
}