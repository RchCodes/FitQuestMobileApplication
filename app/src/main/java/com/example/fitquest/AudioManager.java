package com.example.fitquest;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;


import java.util.Locale;

public class AudioManager {
    private final Context context;
    private TextToSpeech tts;

    public AudioManager(Context context) {
        this.context = context;
        initTTS();
    }

    private void initTTS() {
        tts = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.getDefault());
            }
        });
    }

    public void speak(String text) {
        if (tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "fitquest_tts");
        }
    }

    public void speak(String text, Runnable onDone) {
        if (tts != null) {
            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {}

                @Override
                public void onDone(String utteranceId) {
                    if (onDone != null) {
                        new android.os.Handler(android.os.Looper.getMainLooper()).post(onDone);
                    }
                }

                @Override
                public void onError(String utteranceId) {}
            });
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "fitquest_tts");
        }
    }


    public void playRepSound() {
        try {
            android.media.ToneGenerator tg = new android.media.ToneGenerator(
                    android.media.AudioManager.STREAM_NOTIFICATION, 100);
            tg.startTone(android.media.ToneGenerator.TONE_PROP_BEEP, 150);
        } catch (Exception ignored) {}
    }

    public void destroy() {
        if (tts != null) tts.shutdown();
    }
}
