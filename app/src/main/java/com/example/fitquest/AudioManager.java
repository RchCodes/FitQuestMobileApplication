package com.example.fitquest;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.media.ToneGenerator;

import java.util.Locale;

public class AudioManager {
    private final Context context;
    private TextToSpeech tts;
    private ToneGenerator toneGen; // reusable ToneGenerator

    public AudioManager(Context context) {
        this.context = context;
        initTTS();
        initToneGen();
    }

    private void initTTS() {
        tts = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.getDefault());
            }
        });
    }

    private void initToneGen() {
        // Fully qualify android.media.AudioManager to avoid conflict with this class
        toneGen = new ToneGenerator(android.media.AudioManager.STREAM_MUSIC, 100);
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

    // Play a short beep for each rep
    public void playBeep() {
        if (toneGen != null) {
            toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 150);
        }
    }

    public void destroy() {
        if (tts != null) tts.shutdown();
        if (toneGen != null) {
            toneGen.release();
            toneGen = null;
        }
    }
}
