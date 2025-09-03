package com.example.fitquest;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import android.os.Bundle;

public class VoiceController {
    private final Context context;
    private final Handler handler;
    private SpeechRecognizer speechRecognizer;
    private boolean isTrackingActive = false;
    private final TrackingStateCallback callback;

    public interface TrackingStateCallback {
        void onTrackingStateChanged(boolean isActive);
    }

    public VoiceController(Context context, TrackingStateCallback callback) {
        this.context = context;
        this.callback = callback;
        this.handler = new Handler(Looper.getMainLooper());
        initSpeechRecognizer();
    }

    private void initSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {}

                @Override
                public void onBeginningOfSpeech() {}

                @Override
                public void onRmsChanged(float rmsdB) {}

                @Override
                public void onBufferReceived(byte[] buffer) {}

                @Override
                public void onEndOfSpeech() {}

                @Override
                public void onError(int error) {
                    Log.e("VoiceController", "Voice recognition error: " + error);
                }

                @Override
                public void onResults(Bundle results) {
                    ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null && !matches.isEmpty()) {
                        String command = matches.get(0).toLowerCase();
                        processVoiceCommand(command);
                    }
                }

                @Override
                public void onPartialResults(Bundle partialResults) {}

                @Override
                public void onEvent(int eventType, Bundle params) {}
            });
        }
    }

    public void toggleTracking() {
        if (isTrackingActive) {
            stopTracking();
        } else {
            startTracking();
        }
    }

    public void startTracking() {
        isTrackingActive = true;
        callback.onTrackingStateChanged(true);
        Toast.makeText(context, "Exercise tracking started!", Toast.LENGTH_SHORT).show();
    }

    public void stopTracking() {
        isTrackingActive = false;
        callback.onTrackingStateChanged(false);
        Toast.makeText(context, "Exercise tracking stopped!", Toast.LENGTH_SHORT).show();
    }

    private void processVoiceCommand(String command) {
        Log.d("VoiceCommand", "Received: " + command);
        
        if (command.contains("start") || command.contains("begin") || command.contains("go")) {
            startTracking();
        } else if (command.contains("stop") || command.contains("pause") || command.contains("end")) {
            stopTracking();
        } else if (command.contains("reset")) {
            // Reset functionality can be added here
            Toast.makeText(context, "Reset command received", Toast.LENGTH_SHORT).show();
        }
        
        // Continue listening for more commands
        handler.postDelayed(this::startListening, 1000);
    }

    public void startListening() {
        if (speechRecognizer != null) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.getPackageName());
            speechRecognizer.startListening(intent);
        }
    }

    public void destroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        handler.removeCallbacksAndMessages(null);
    }
}
