package com.example.fitquest;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.List;

import static java.lang.Math.atan2;

public class ExerciseTrackingActivity extends AppCompatActivity {

    // UI Components
    private PreviewView previewView;
    private TextView repCounter;
    private TextView feedbackText;
    private TextView instructionText;
    private TextView timerText;
    private PoseOverlayView poseOverlay;
    private LinearLayout permissionLayout;
    private Button requestPermissionButton;
    private Button settingsButton;
    private Button voiceButton;

    // Core Components
    private PoseDetector poseDetector;
    private ExerciseDetector exerciseDetector;
    private VoiceController voiceController;
    private AudioManager audioManager;

    // Exercise State
    private String exerciseType = "squats";
    private String difficultyLevel = "beginner";
    private int targetReps = 10;
    private boolean exerciseCompleted = false;
    private boolean isTrackingActive = false;

    // Constants
    private static final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.RECORD_AUDIO"};
    private static final int REQUEST_CODE_PERMISSIONS = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_tracking);

        initializeViews();
        initializeComponents();
        setupPermissions();
        setupExerciseFromIntent();
    }

    private void initializeViews() {
        previewView = findViewById(R.id.previewView);
        repCounter = findViewById(R.id.repCounter);
        feedbackText = findViewById(R.id.feedbackText);
        instructionText = findViewById(R.id.instructionText);
        timerText = findViewById(R.id.timerText);
        poseOverlay = findViewById(R.id.poseOverlay);
        permissionLayout = findViewById(R.id.permission_layout);
        requestPermissionButton = findViewById(R.id.request_permission_button);

        // Add overlay buttons
        addOverlayButtons();
    }

    private void addOverlayButtons() {
        // Settings button
        settingsButton = new Button(this);
        settingsButton.setText("Settings");
        settingsButton.setOnClickListener(v -> startActivity(new Intent(this, UserSettingsActivity.class)));
        addContentView(settingsButton, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Voice button
        voiceButton = new Button(this);
        voiceButton.setText("🎤 Start Tracking");
        voiceButton.setOnClickListener(v -> voiceController.toggleTracking());
        LinearLayout.LayoutParams voiceParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        voiceParams.topMargin = 150;
        addContentView(voiceButton, voiceParams);
    }

    private void initializeComponents() {
        // Initialize pose detector
        PoseDetectorOptions options = new PoseDetectorOptions.Builder()
                .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
                .build();
        poseDetector = PoseDetection.getClient(options);

        // Initialize exercise detector
        exerciseDetector = new ExerciseDetector(this, difficultyLevel, targetReps);

        // Initialize voice controller
        voiceController = new VoiceController(this, this::onTrackingStateChanged);

        // Initialize audio manager
        audioManager = new AudioManager(this);
    }

    private void setupPermissions() {
        requestPermissionButton.setOnClickListener(v -> 
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        );

        if (allPermissionsGranted()) {
            hidePermissionUI();
            startCamera();
            setupExerciseUI();
        } else {
            showPermissionUI();
        }
    }

    private void setupExerciseFromIntent() {
        difficultyLevel = User.getDifficultyLevel(this);

        if (getIntent().hasExtra("EXERCISE_TYPE")) {
            exerciseType = getIntent().getStringExtra("EXERCISE_TYPE");
        }
        if (getIntent().hasExtra("MAX_PROGRESS")) {
            targetReps = getIntent().getIntExtra("MAX_PROGRESS", 10);
        }
        if (getIntent().hasExtra("DIFFICULTY_LEVEL")) {
            difficultyLevel = getIntent().getStringExtra("DIFFICULTY_LEVEL");
        }

        exerciseDetector.updateExercise(exerciseType, difficultyLevel, targetReps);
    }

    @Override
    protected void onResume() {
        super.onResume();
        difficultyLevel = User.getDifficultyLevel(this);
        exerciseDetector.updateExercise(exerciseType, difficultyLevel, targetReps);
        setupExerciseUI();

        if (allPermissionsGranted()) {
            hidePermissionUI();
        }
    }

    private void onTrackingStateChanged(boolean isActive) {
        isTrackingActive = isActive;
        runOnUiThread(() -> {
            voiceButton.setText(isActive ? "🎤 Stop Tracking" : "🎤 Start Tracking");
            feedbackText.setText(isActive ? "Tracking started! Get into position." : "Tracking stopped. Say 'Start' to resume.");
        });
    }

    private void setupExerciseUI() {
        String difficultyText = "Difficulty: " + difficultyLevel.substring(0, 1).toUpperCase() + difficultyLevel.substring(1);

        switch (exerciseType) {
            case "squats":
                instructionText.setText("Stand with feet shoulder-width apart\nBend your knees and lower your body\nKeep your back straight\n" + difficultyText);
                repCounter.setVisibility(View.VISIBLE);
                timerText.setVisibility(View.GONE);
                repCounter.setText("Squat Reps: " + exerciseDetector.getCurrentReps() + "/" + targetReps);
                audioManager.speak("Starting squats. " + difficultyText);
                break;
            case "pushups":
                instructionText.setText("Start in plank position\nLower your body by bending elbows\nPush back up to starting position\n" + difficultyText);
                repCounter.setVisibility(View.VISIBLE);
                timerText.setVisibility(View.GONE);
                repCounter.setText("Pushup Reps: " + exerciseDetector.getCurrentReps() + "/" + targetReps);
                audioManager.speak("Starting push ups. " + difficultyText);
                break;
            case "plank":
                instructionText.setText("Hold your body in a straight line\nKeep your core tight\nDon't let your hips sag\n" + difficultyText);
                repCounter.setVisibility(View.GONE);
                timerText.setVisibility(View.VISIBLE);
                exerciseDetector.startPlankTimer();
                audioManager.speak("Starting plank. Hold the position.");
                break;
            case "crunches":
                instructionText.setText("Lie on your back\nLift shoulders off the ground\nControl down slowly\n" + difficultyText);
                repCounter.setVisibility(View.VISIBLE);
                timerText.setVisibility(View.GONE);
                repCounter.setText("Crunch Reps: " + exerciseDetector.getCurrentReps() + "/" + targetReps);
                audioManager.speak("Starting crunches. " + difficultyText);
                break;
            case "lunges":
                instructionText.setText("Step forward\nLower until both knees are bent\nPush back to start\n" + difficultyText);
                repCounter.setVisibility(View.VISIBLE);
                timerText.setVisibility(View.GONE);
                repCounter.setText("Lunge Reps: " + exerciseDetector.getCurrentReps() + "/" + targetReps);
                audioManager.speak("Starting lunges. " + difficultyText);
                break;
        }
    }

    // Permission handling
    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void showPermissionUI() {
        permissionLayout.setVisibility(View.VISIBLE);
    }

    private void hidePermissionUI() {
        permissionLayout.setVisibility(View.GONE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            boolean grantedAll = true;
            for (int res : grantResults) {
                if (res != PackageManager.PERMISSION_GRANTED) {
                    grantedAll = false;
                    break;
                }
            }
            if (grantedAll) {
                hidePermissionUI();
                startCamera();
                voiceController.startListening();
            } else {
                showPermissionUI();
                Toast.makeText(this, "Permissions required to proceed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Camera setup
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), this::analyzeImage);

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                        .build();

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void analyzeImage(@NonNull ImageProxy imageProxy) {
        @SuppressWarnings("UnsafeOptInUsageError")
        InputImage inputImage = InputImage.fromMediaImage(
                imageProxy.getImage(),
                imageProxy.getImageInfo().getRotationDegrees()
        );

        // Provide frame info to overlay for correct mapping
        if (poseOverlay != null && imageProxy.getImage() != null) {
            int imgW = imageProxy.getImage().getWidth();
            int imgH = imageProxy.getImage().getHeight();
            int rot = imageProxy.getImageInfo().getRotationDegrees();
            boolean front = true; // we use front camera
            poseOverlay.setFrameInfo(imgW, imgH, rot, front, previewView.getWidth(), previewView.getHeight());
        }

        poseDetector.process(inputImage)
                .addOnSuccessListener(this::detectExercise)
                .addOnFailureListener(e -> Log.e("PoseDetection", "Failed: " + e.getMessage()))
                .addOnCompleteListener(task -> imageProxy.close());
    }

    private void detectExercise(Pose pose) {
        // Update pose overlay for joint visualization
        if (poseOverlay != null) {
            poseOverlay.setPose(pose);
        }

        // Check if tracking is active
        if (!isTrackingActive) {
            runOnUiThread(() -> feedbackText.setText("Say 'Start' to begin tracking"));
            return;
        }

        // Check if we have enough body landmarks for accurate tracking
        if (!exerciseDetector.hasEnoughBodyLandmarks(pose)) {
            runOnUiThread(() -> feedbackText.setText("Please stand back to see your full body"));
            return;
        }

        // Process exercise detection
        exerciseDetector.processPose(pose, exerciseType);
        
        // Update UI based on exercise state
        updateExerciseUI();
        
        // Check if exercise is completed
        if (exerciseDetector.isExerciseCompleted()) {
            completeExercise();
        }
    }

    private void updateExerciseUI() {
        if (exerciseType.equals("plank")) {
            runOnUiThread(() -> timerText.setText("Plank Time: " + exerciseDetector.getPlankTimeSeconds() + "s"));
        } else {
            runOnUiThread(() -> repCounter.setText(
                exerciseType.substring(0, 1).toUpperCase() + exerciseType.substring(1) + 
                " Reps: " + exerciseDetector.getCurrentReps() + "/" + targetReps
            ));
        }
        
        // Update feedback text
        runOnUiThread(() -> feedbackText.setText(exerciseDetector.getFeedbackText()));
    }

    private void completeExercise() {
        if (exerciseCompleted) return;

        exerciseCompleted = true;
        final String message;
        if (exerciseType.equals("plank")) {
            long seconds = exerciseDetector.getPlankTimeSeconds();
            message = "Plank completed! " + seconds + " seconds held.";
        } else {
            message = "Exercise completed! " + exerciseDetector.getCurrentReps() + " reps done.";
        }

        runOnUiThread(() -> feedbackText.setText("🎉 " + message));
        audioManager.speak(message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        // Return to main activity after a short delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }, 2000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (poseDetector != null) {
            poseDetector.close();
        }
        if (voiceController != null) {
            voiceController.destroy();
        }
        if (audioManager != null) {
            audioManager.destroy();
        }
        if (exerciseDetector != null) {
            exerciseDetector.destroy();
        }
    }
}
