package com.example.fitquest;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ProgressBar;
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

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExerciseTrackingActivity extends AppCompatActivity implements ExerciseDetector.ExerciseListener {

    private static final String TAG = "ExerciseTrackingAct";

    private PreviewView previewView;
    private PoseOverlayView poseOverlay;

    private ProgressBar circularProgress;
    private TextView progressText;

    private TextView feedbackText, instructionText;
    private View permissionLayout;
    private Button requestPermissionButton;

    private PoseDetector poseDetector;
    private ExerciseDetector exerciseDetector;
    private AudioManager audioManager;

    private String exerciseType = "squats";
    private String difficultyLevel = "beginner";
    private int targetReps = 10;
    private String questId = null;

    private static final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO
    };
    private static final int REQUEST_CODE_PERMISSIONS = 10;

    private ProcessCameraProvider cameraProvider;
    private CameraSelector cameraSelector;
    private ExecutorService analysisExecutor;

    // Keep last reported plank seconds so we only report deltas
    private long lastReportedPlankSeconds = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_tracking);

        previewView = findViewById(R.id.previewView);
        poseOverlay = findViewById(R.id.poseOverlay);
        feedbackText = findViewById(R.id.feedbackText);
        instructionText = findViewById(R.id.instructionText);
        permissionLayout = findViewById(R.id.permission_layout);
        requestPermissionButton = findViewById(R.id.request_permission_button);

        circularProgress = findViewById(R.id.circularProgress);
        progressText = findViewById(R.id.progressText);


        audioManager = new AudioManager(this);

        // Read intent extras
        if (getIntent().hasExtra("EXERCISE_TYPE")) exerciseType = getIntent().getStringExtra("EXERCISE_TYPE");
        if (getIntent().hasExtra("MAX_PROGRESS")) targetReps = getIntent().getIntExtra("MAX_PROGRESS", 10);
        if (getIntent().hasExtra("DIFFICULTY_LEVEL")) difficultyLevel = getIntent().getStringExtra("DIFFICULTY_LEVEL");
        if (getIntent().hasExtra("QUEST_ID")) questId = getIntent().getStringExtra("QUEST_ID");

        exerciseDetector = new ExerciseDetector(this, difficultyLevel, targetReps);
        exerciseDetector.setListener(this);

        initializePoseDetector();
        setupPermissions();
        setupUiForExercise();
    }

    private void setupUiForExercise() {

        if (exerciseType.equals("plank")) {
            circularProgress.setMax((int) targetReps); // seconds target
            progressText.setText("0s");
        } else {
            circularProgress.setMax(targetReps);
            progressText.setText("0/" + targetReps);
        }
        instructionText.setText("Perform " + targetReps + " " + capitalize(exerciseType));
        feedbackText.setText("Get ready...");
        audioManager.speak("Starting " + exerciseType);
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
        for (String p : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) return false;
        }
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

        cameraSelector = new CameraSelector.Builder()
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
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, analysis);
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

            // send detection
            poseDetector.process(inputImage)
                    .addOnSuccessListener(pose -> {
                        // update overlay and detector
                        poseOverlay.post(() -> {
                            poseOverlay.setFrameInfo(imageProxy.getWidth(), imageProxy.getHeight(),
                                    rotation, true, previewView.getWidth(), previewView.getHeight());
                        });
                        poseOverlay.post(() -> poseOverlay.setPose(pose));
                        exerciseDetector.processPose(pose, exerciseType);
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Pose detection failed", e))
                    .addOnCompleteListener(t -> imageProxy.close());

        } catch (Exception e) {
            Log.e(TAG, "analyzeImage error", e);
            imageProxy.close();
        }
    }

    // ExerciseListener callbacks
    @Override
    public void onRepCountChanged(int currentReps, int target) {
        runOnUiThread(() -> {
            circularProgress.setMax(target);

            // animate from old progress to new progress
            int oldProgress = circularProgress.getProgress();
            animateProgress(circularProgress, oldProgress, currentReps);

            progressText.setText(currentReps + "/" + target);
        });

        QuestManager.reportExerciseResult(this, exerciseType, 1);
    }


    @Override
    public void onPlankTimeUpdated(long seconds, long requiredSeconds) {
        runOnUiThread(() -> {
            circularProgress.setMax((int) requiredSeconds);

            int remaining = (int) (requiredSeconds - seconds);

            int oldProgress = circularProgress.getProgress();
            animateProgress(circularProgress, oldProgress, remaining);

            progressText.setText(remaining + "s");
        });

    // report only delta seconds to avoid overshoot
        long delta = seconds - lastReportedPlankSeconds;
        if (delta > 0) {
            QuestManager.reportExerciseResult(this, exerciseType, (int) delta);
            lastReportedPlankSeconds = seconds;
        }
    }

    private void animateProgress(ProgressBar progressBar, int from, int to) {
        ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "progress", from, to);
        animation.setDuration(300); // 0.3s smooth transition
        animation.setInterpolator(new DecelerateInterpolator());
        animation.start();
    }


    @Override
    public void onFeedbackUpdated(String feedback) {
        runOnUiThread(() -> feedbackText.setText(feedback));
    }

    @Override
    public void onExerciseCompleted(String summaryMessage) {
        runOnUiThread(() -> {
            feedbackText.setText("🎉 " + summaryMessage);
            audioManager.speak(summaryMessage, () -> {
                // Callback after audio finishes
                finish();
            });
            Toast.makeText(this, summaryMessage, Toast.LENGTH_LONG).show();
        });

        // Finalize and persist progress, without auto-claim
        QuestManager.reportExerciseResult(this, exerciseType, 0);

        // Auto-apply rewards only for the specific questId if provided and quest became completed
//        if (questId != null && !questId.isEmpty()) {
//            for (QuestModel q : QuestManager.getAll(this)) {
//                if (questId.equals(q.getId()) && q.isCompleted() && !q.isClaimed()) {
//                    boolean leveled = QuestManager.claimQuest(this, q);
//                    QuestRewardManager.showRewardPopup(this, q.getReward());
//                    if (leveled) {
//                        AvatarModel avatar = AvatarManager.loadAvatarOffline(this);
//                        if (avatar != null) QuestRewardManager.showLevelUpPopup(this, avatar.getLevel(), avatar.getRank());
//                    }
//                    break;
//                }
//            }
//        }

        new Handler(Looper.getMainLooper()).postDelayed(this::finish, 5000);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0,1).toUpperCase() + s.substring(1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            boolean grantedAll = true;
            for (int r : grantResults) if (r != PackageManager.PERMISSION_GRANTED) { grantedAll = false; break; }
            if (grantedAll) {
                permissionLayout.setVisibility(View.GONE);
                startCamera();
            } else {
                Toast.makeText(this, "Permissions required", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (poseDetector != null) {
            poseDetector.close();
            poseDetector = null;
        }
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
            cameraProvider = null;
        }
        if (analysisExecutor != null) {
            analysisExecutor.shutdown();
            analysisExecutor = null;
        }
        if (exerciseDetector != null) exerciseDetector.destroy();
        if (audioManager != null) audioManager.destroy();
    }
}
