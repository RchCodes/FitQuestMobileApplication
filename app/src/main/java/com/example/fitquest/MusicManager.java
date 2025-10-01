package com.example.fitquest;

import android.content.Context;
import android.media.MediaPlayer;

public class MusicManager {

    private static MediaPlayer mediaPlayer;
    private static int volume = 50; // 0-100

    public static void start(Context context) {
        if (mediaPlayer != null) stop();
        mediaPlayer = MediaPlayer.create(context, R.raw.bgm);
        mediaPlayer.setLooping(true);
        mediaPlayer.setVolume(volume / 100f, volume / 100f);
        mediaPlayer.start();
    }

    public static void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public static void setVolume(int vol) {
        volume = vol;
        if (mediaPlayer != null) mediaPlayer.setVolume(volume / 100f, volume / 100f);
    }

    public static int getVolume() { return volume; }
    public static boolean isPlaying() { return mediaPlayer != null && mediaPlayer.isPlaying(); }
}
