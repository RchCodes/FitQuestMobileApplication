package com.example.fitquest;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FitnessChallengeActivity extends AppCompatActivity implements ExerciseDetector.ExerciseListener {

    private static final String TAG = "FitnessChallenge";
    private View permissionLayout;
    private PreviewView previewView;
    private PoseOverlayView poseOverlay;
    private CircularProgressIndicator circularProgress;
    private TextView progressText, feedbackText, instructionText, timerText, tvChallengeTitle, tvChallengeObjective;
    private Button btnFinishChallenge, requestPermissionButton;

    private PoseDetector poseDetector;
    private ExerciseDetector exerciseDetector;
    private AudioManager audioManager;

    private ProcessCameraProvider cameraProvider;
    private ExecutorService analysisExecutor;
    private boolean strictMode = true;

    private String name;
    private String challengeId;
    private String exerciseType;
    private String objective;

    private int target;
    private int timeLimit;

    private boolean challengeCompleted = false;

    private int badFormFrameCount = 0;
    private static final int BAD_FORM_THRESHOLD_FRAMES = 20; // e.g. ~0.7s at 30 FPS

    private boolean outOfPosition = false;
    private long outOfPositionTime = 0L;

    private CountDownTimer challengeTimer;

    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fitness_challenge);

        // Views
        previewView = findViewById(R.id.previewView);
        poseOverlay = findViewById(R.id.poseOverlay);
        circularProgress = findViewById(R.id.circularProgress);
        progressText = findViewById(R.id.progressText);
        feedbackText = findViewById(R.id.feedbackText);
        instructionText = findViewById(R.id.instructionText);
        timerText = findViewById(R.id.timerText);

        tvChallengeTitle = findViewById(R.id.tvChallengeTitle);
        tvChallengeObjective = findViewById(R.id.tvChallengeObjective);
        btnFinishChallenge = findViewById(R.id.btnFinishChallenge);
        permissionLayout = findViewById(R.id.permission_layout);
        requestPermissionButton = findViewById(R.id.request_permission_button);

        audioManager = new AudioManager(this);

        // Extract challenge info safely
        Intent intent = getIntent();

        challengeId = intent.getStringExtra("challengeId");
        exerciseType = intent.getStringExtra("exercise");
        objective = intent.getStringExtra("objective");
        name = intent.getStringExtra("name");

        tvChallengeTitle.setText(name);
        tvChallengeObjective.setText(objective);

        try {
            // Try getting them as ints
            timeLimit = intent.getIntExtra("timeLimit", 0);
            target = intent.getIntExtra("target", 0);

            // If they were passed as strings, parse them
            if (timeLimit == 0) {
                String timeStr = intent.getStringExtra("timeLimit");
                if (timeStr != null) timeLimit = Integer.parseInt(timeStr);
                Log.d("FitQuest", "Time limit set to: " + timeLimit + " seconds");
            }
            if (target == 0) {
                String targetStr = intent.getStringExtra("target");
                if (targetStr != null) target = Integer.parseInt(targetStr);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            timeLimit = 0;
            target = 0;
        }

        // Initialize detector
        exerciseDetector = new ExerciseDetector(this, "challenge", 9999);
        exerciseDetector.setListener(this);

        instructionText.setText(objective);
        circularProgress.setMax(target);

        btnFinishChallenge.setOnClickListener(v -> finish());

        setupPermissions();
        initializePoseDetector();
        startChallengeTimer();
    }


    private int parseTimeLimit(String time) {
        try {
            return Integer.parseInt(time.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 120; // default 2 mins
        }
    }

    private void initializePoseDetector() {
        PoseDetectorOptions options = new PoseDetectorOptions.Builder()
                .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
                .build();
        poseDetector = PoseDetection.getClient(options);
    }

    private void setupPermissions() {
        if (allPermissionsGranted()) {
            permissionLayout.setVisibility(View.GONE);
            startCamera();
        } else {
            permissionLayout.setVisibility(View.VISIBLE);
            requestPermissionButton.setOnClickListener(v ->
                    ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS));
        }
    }


    private boolean allPermissionsGranted() {
        for (String p : REQUIRED_PERMISSIONS)
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED)
                return false;
        return true;
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases();
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Camera provider error", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraUseCases() {
        if (cameraProvider == null) return;

        CameraSelector selector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build();

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        analysisExecutor = Executors.newSingleThreadExecutor();
        ImageAnalysis analysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        analysis.setAnalyzer(analysisExecutor, this::analyzeImage);

        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle(this, selector, preview, analysis);
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void analyzeImage(ImageProxy imageProxy) {
        try {
            if (poseDetector == null || imageProxy.getImage() == null) {
                imageProxy.close();
                return;
            }

            int rotation = imageProxy.getImageInfo().getRotationDegrees();
            InputImage inputImage = InputImage.fromMediaImage(imageProxy.getImage(), rotation);

            poseDetector.process(inputImage)
                    .addOnSuccessListener(pose -> {
                        poseOverlay.post(() -> {
                            poseOverlay.setFrameInfo(
                                    imageProxy.getWidth(), imageProxy.getHeight(),
                                    rotation, true,
                                    previewView.getWidth(), previewView.getHeight());
                            poseOverlay.setPose(pose);
                        });

                        exerciseDetector.processPose(pose, exerciseType);

                        if (strictMode && !exerciseDetector.isInPosition(pose, exerciseType)) {
                            badFormFrameCount++;

                            if (badFormFrameCount == 1) {
                                feedbackText.setText("❌ Keep proper form!");
                                audioManager.speakOnce("Maintain form!");
                            }

                            if (badFormFrameCount > BAD_FORM_THRESHOLD_FRAMES) {
                                failChallenge("Lost proper form!");
                            }

                            return; // skip rep counting
                        } else {
                            badFormFrameCount = 0;
                        }



                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Pose detection failed", e))
                    .addOnCompleteListener(t -> imageProxy.close());
        } catch (Exception e) {
            imageProxy.close();
        }
    }

    private void startChallengeTimer() {
        challengeTimer = new CountDownTimer(timeLimit * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int elapsed = (int) ((timeLimit * 1000L - millisUntilFinished) / 1000L);
                int remaining = timeLimit - elapsed;
                timerText.setText("⏱ " + remaining + "s left");

                if (remaining <= 10)
                    audioManager.speak(String.valueOf(remaining));
            }

            @Override
            public void onFinish() {
                if (!challengeCompleted) {
                    // Timer finished but target not reached - fail the challenge
                    failChallenge("Time's up! You didn't reach the target.");
                }
            }
        };
        challengeTimer.start();
    }

    private void completeChallenge() {
        if (challengeCompleted) return; // Prevent double call
        challengeCompleted = true;

        if (challengeTimer != null) challengeTimer.cancel();

        feedbackText.setText("🎉 Challenge Completed!");
        audioManager.speak("Great job! Challenge completed!");

        markChallengeComplete(challengeId);

        new Handler(getMainLooper()).postDelayed(this::finish, 5000);
    }


    private void updateProgressBar(int repCount) {
        String currentStr = progressText.getText() != null ? progressText.getText().toString() : "0";

        int current = 0;
        if (!currentStr.isEmpty()) {
            try {
                current = Integer.parseInt(currentStr);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                current = 0;
            }
        }

        current = repCount; // or whatever logic you use

        progressText.setText(String.valueOf(current)+ "/" + target);
        circularProgress.setProgress(current);
    }



    private void failChallenge(String reason) {
        if (challengeCompleted) return;
        challengeCompleted = true;
        
        try {
            if (challengeTimer != null) {
                challengeTimer.cancel();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error canceling timer", e);
        }

        try {
            if (feedbackText != null) {
                feedbackText.setText("❌ Failed: " + reason);
            }
            if (audioManager != null) {
                audioManager.speak("Challenge failed. " + reason);
            }
            Toast.makeText(this, reason, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing failure UI", e);
        }

        try {
            new Handler(getMainLooper()).postDelayed(() -> {
                try {
                    finish();
                } catch (Exception e) {
                    Log.e(TAG, "Error finishing activity", e);
                }
            }, 4000);
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling finish", e);
        }
    }

    private void markChallengeComplete(String challengeId) {
        try {
            // Mark challenge as completed in local storage
            ChallengeManager.markCompleted(this, challengeId);

            // Update linked goal if any
            ChallengeModel challenge = ChallengeManager.getById(this, challengeId);
            if (challenge != null && challenge.getLinkedGoalId() != null) {
                Goals.updateGoalProgress(this, challenge.getLinkedGoalId());
            }

            // Mark as completed in Firebase (for progress tracking)
            String uid = AvatarManager.getCurrentUserId();
            if (uid == null || challengeId == null) {
                Log.w(TAG, "Cannot mark complete: uid or challengeId null");
                return;
            }

            DatabaseReference ref = FirebaseDatabase.getInstance()
                    .getReference("challenge_progress")
                    .child(uid)
                    .child(challengeId);
            ref.setValue(true);


            // Auto-claim reward if not already claimed
            AvatarModel avatar = AvatarManager.loadAvatarOffline(this);
            if (challenge != null && avatar != null && !challenge.isClaimed()) {
                ChallengeManager.claimReward(this, avatar, challengeId);
                AvatarManager.saveAvatarOnline(avatar);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to mark challenge complete", e);
        }
    }

    @Override
    public void onRepCountChanged(int currentReps, int target) {
        runOnUiThread(() -> {
            feedbackText.setText("Keep going! " + currentReps + " reps");
            updateProgressBar(currentReps);
        });
        audioManager.playBeep();

        if (currentReps >= target && !challengeCompleted)
            completeChallenge();
    }


    @Override
    public void onPlankTimeUpdated(long seconds, long requiredSeconds) {
    }

    @Override
    public void onFeedbackUpdated(String feedback) {
        runOnUiThread(() -> feedbackText.setText(feedback));
    }

    @Override
    public void onExerciseCompleted(String summaryMessage) {
        completeChallenge();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (challengeTimer != null) {
            challengeTimer.cancel();
            challengeTimer = null;
        }

        if (analysisExecutor != null) {
            analysisExecutor.shutdownNow();
            analysisExecutor = null;
        }

        if (poseDetector != null) {
            try { poseDetector.close(); } catch (Exception ignored) {}
            poseDetector = null;
        }

        if (exerciseDetector != null) {
            exerciseDetector.destroy();
            exerciseDetector = null;
        }

        if (audioManager != null) {
            audioManager.destroy();
            audioManager = null;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            boolean grantedAll = true;
            for (int r : grantResults)
                if (r != PackageManager.PERMISSION_GRANTED) grantedAll = false;

            if (grantedAll) startCamera();
            else {
                Toast.makeText(this, "Permissions required", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
