package com.example.fitquest;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

public class SoundManager {

    private static SoundPool soundPool;
    private static int volume = 50; // 0-100
    private static int clickSoundId;

    public static void init(Context context) {
        if (soundPool == null) {
            AudioAttributes attrs = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            soundPool = new SoundPool.Builder()
                    .setMaxStreams(5)
                    .setAudioAttributes(attrs)
                    .build();

            clickSoundId = soundPool.load(context, R.raw.click, 1);
        }
    }

    public static void playClick() {
        if (soundPool != null) soundPool.play(clickSoundId, volume / 100f, volume / 100f, 1, 0, 1f);
    }

    public static void setVolume(int vol) { volume = vol; }
    public static int getVolume() { return volume; }
    public static void release() { if (soundPool != null) { soundPool.release(); soundPool = null; } }
}
