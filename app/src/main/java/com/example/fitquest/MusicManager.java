package com.example.fitquest;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;

public class MusicManager {

    private static MediaPlayer mediaPlayer;
    private static MediaPlayer battlePlayer;
    private static int volume = 50; // 0-100
    private static boolean musicEnabled = true;
    private static boolean isInitialized = false;
    
    private static final String PREFS_NAME = "fitquest_music";
    private static final String KEY_VOLUME = "music_volume";
    private static final String KEY_ENABLED = "music_enabled";

    public static void initialize(Context context) {
        if (isInitialized) return;
        
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        volume = prefs.getInt(KEY_VOLUME, 50);
        musicEnabled = prefs.getBoolean(KEY_ENABLED, true);
        isInitialized = true;
    }

    public static void start(Context context) {
        initialize(context);
        
        if (!musicEnabled) return;
        
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) return; // Already playing
            stop(); // Stop and recreate if needed
        }
        
        try {
            mediaPlayer = MediaPlayer.create(context, R.raw.bgm);
            if (mediaPlayer != null) {
                mediaPlayer.setLooping(true);
                mediaPlayer.setVolume(volume / 100f, volume / 100f);
                mediaPlayer.start();
            }
        } catch (Exception e) {
            // Handle any media creation errors
            e.printStackTrace();
        }
    }

    public static void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }
    
    public static void resume() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    public static void stop() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
                mediaPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mediaPlayer = null;
        }
    }

    public static void setVolume(int vol, Context context) {
        volume = Math.max(0, Math.min(100, vol)); // Clamp between 0-100
        
        // Save to preferences
        if (context != null) {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit().putInt(KEY_VOLUME, volume).apply();
        }
        
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(volume / 100f, volume / 100f);
        }
    }
    
    public static void setMusicEnabled(boolean enabled, Context context) {
        musicEnabled = enabled;
        
        // Save to preferences
        if (context != null) {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit().putBoolean(KEY_ENABLED, musicEnabled).apply();
        }
        
        if (enabled) {
            // Start music if enabled
            if (context != null) {
                start(context);
            }
        } else {
            // Stop music if disabled
            stop();
        }
    }

    public static int getVolume() { 
        return volume; 
    }
    
    public static boolean isMusicEnabled() {
        return musicEnabled;
    }
    
    public static boolean isPlaying() { 
        return mediaPlayer != null && mediaPlayer.isPlaying(); 
    }
    
    public static void onActivityResume(Context context) {
        if (musicEnabled && (mediaPlayer == null || !mediaPlayer.isPlaying())) {
            start(context);
        }
    }
    
    public static void onActivityPause() {
        // Pause music when app goes to background
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
        if (battlePlayer != null && battlePlayer.isPlaying()) {
            battlePlayer.pause();
        }
    }
    
    public static void onActivityDestroy() {
        // Only stop if explicitly called or app is closing
        // This allows music to persist across activities
    }
    
    // Battle BGM methods
    public static void startBattleBGM(Context context) {
        initialize(context);
        
        if (!musicEnabled) return;
        
        // Stop regular BGM
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
        
        if (battlePlayer != null) {
            if (battlePlayer.isPlaying()) return; // Already playing
            stopBattleBGM(); // Stop and recreate if needed
        }
        
        try {
            battlePlayer = MediaPlayer.create(context, R.raw.battle_bgm);
            if (battlePlayer != null) {
                battlePlayer.setLooping(true);
                battlePlayer.setVolume(volume / 100f, volume / 100f);
                battlePlayer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void stopBattleBGM() {
        if (battlePlayer != null) {
            try {
                battlePlayer.stop();
                battlePlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            battlePlayer = null;
        }
    }
    
    public static void resumeBattleBGM() {
        if (battlePlayer != null && !battlePlayer.isPlaying()) {
            battlePlayer.start();
        }
    }
    
    public static void pauseBattleBGM() {
        if (battlePlayer != null && battlePlayer.isPlaying()) {
            battlePlayer.pause();
        }
    }
    
    public static boolean isBattleBGMPlaying() {
        return battlePlayer != null && battlePlayer.isPlaying();
    }
}
