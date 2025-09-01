package com.example.fitquest;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import java.util.concurrent.ExecutionException;
import java.util.List;

import static java.lang.Math.atan2;

public class ExerciseTrackingActivity extends AppCompatActivity {

    private PreviewView previewView;
    private TextView repCounter;
    private TextView feedbackText;
    private TextView instructionText;
    private TextView timerText;
    private PoseOverlayView poseOverlay;
    private LinearLayout permissionLayout;
    private Button requestPermissionButton;
    private PoseDetector poseDetector;
    private int squatReps = 0;
    private int targetReps = 10;
    private boolean isSquatting = false;
    private boolean isPlanking = false;
    private String exerciseType = "squats";
    private long plankStartTime = 0;
    private long plankTotalTime = 0;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean exerciseCompleted = false;

    private static final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA"};
    private static final int REQUEST_CODE_PERMISSIONS = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_tracking);

        previewView = findViewById(R.id.previewView);
        repCounter = findViewById(R.id.repCounter);
        feedbackText = findViewById(R.id.feedbackText);
        instructionText = findViewById(R.id.instructionText);
        timerText = findViewById(R.id.timerText);
        poseOverlay = findViewById(R.id.poseOverlay);
        permissionLayout = findViewById(R.id.permission_layout);
        requestPermissionButton = findViewById(R.id.request_permission_button);

        // Get exercise type and target from intent
        if (getIntent().hasExtra("EXERCISE_TYPE")) {
            exerciseType = getIntent().getStringExtra("EXERCISE_TYPE");
        }
        if (getIntent().hasExtra("MAX_PROGRESS")) {
            targetReps = getIntent().getIntExtra("MAX_PROGRESS", 10);
        }

        // Setup pose detector with standard options
        PoseDetectorOptions options = new PoseDetectorOptions.Builder()
                .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
                .build();
        poseDetector = PoseDetection.getClient(options);

        // Set up permission button click listener
        requestPermissionButton.setOnClickListener(v -> requestCameraPermission());

        // Check and request camera permissions
        if (allPermissionsGranted()) {
            hidePermissionUI();
            startCamera();
            setupExerciseUI();
        } else {
            showPermissionUI();
        }
    }

    private void setupExerciseUI() {
        switch (exerciseType) {
            case "squats":
                instructionText.setText("Stand with feet shoulder-width apart\nBend your knees and lower your body\nKeep your back straight");
                repCounter.setVisibility(View.VISIBLE);
                timerText.setVisibility(View.GONE);
                break;
            case "pushups":
                instructionText.setText("Start in plank position\nLower your body by bending elbows\nPush back up to starting position");
                repCounter.setVisibility(View.VISIBLE);
                timerText.setVisibility(View.GONE);
                break;
            case "plank":
                instructionText.setText("Hold your body in a straight line\nKeep your core tight\nDon't let your hips sag");
                repCounter.setVisibility(View.GONE);
                timerText.setVisibility(View.VISIBLE);
                startPlankTimer();
                break;
        }
    }

    private void startPlankTimer() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isPlanking && !exerciseCompleted) {
                    plankTotalTime += 1000; // Add 1 second
                    updateTimerDisplay();
                    handler.postDelayed(this, 1000);
                }
            }
        }, 1000);
    }

    private void updateTimerDisplay() {
        long seconds = plankTotalTime / 1000;
        runOnUiThread(() -> timerText.setText("Plank Time: " + seconds + "s"));
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != ContextCompat.checkSelfPermission(this, permission)) {
                return false;
            }
        }
        return true;
    }

    private void requestCameraPermission() {
        Toast.makeText(this, "Requesting camera permission...", Toast.LENGTH_SHORT).show();
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
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
            if (allPermissionsGranted()) {
                Toast.makeText(this, "Camera permission granted! Starting camera...", Toast.LENGTH_SHORT).show();
                hidePermissionUI();
                startCamera();
                setupExerciseUI();
            } else {
                Toast.makeText(this, "Camera permission denied. Please grant camera permission manually.", Toast.LENGTH_LONG).show();
                showPermissionInstructions();
            }
        }
    }

    private void showPermissionInstructions() {
        Toast.makeText(this, "Go to Settings > Apps > FitQuest > Permissions > Camera and enable it", Toast.LENGTH_LONG).show();
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis =
                        new ImageAnalysis.Builder()
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

        // Check if we have enough body landmarks for accurate tracking
        if (!hasEnoughBodyLandmarks(pose)) {
            runOnUiThread(() -> feedbackText.setText("Please stand back to see your full body"));
            return;
        }

        switch (exerciseType) {
            case "squats":
                detectSquat(pose);
                break;
            case "pushups":
                detectPushup(pose);
                break;
            case "plank":
                detectPlank(pose);
                break;
            default:
                detectSquat(pose); // default to squats
                break;
        }
    }

    private boolean hasEnoughBodyLandmarks(Pose pose) {
        // Check for essential landmarks
        List<PoseLandmark> landmarks = pose.getAllPoseLandmarks();
        if (landmarks.size() < 10) return false; // Need at least 10 landmarks

        // Check for key body parts
        boolean hasHead = pose.getPoseLandmark(PoseLandmark.NOSE) != null;
        boolean hasShoulders = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER) != null && 
                              pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER) != null;
        boolean hasHips = pose.getPoseLandmark(PoseLandmark.LEFT_HIP) != null && 
                         pose.getPoseLandmark(PoseLandmark.RIGHT_HIP) != null;
        boolean hasKnees = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE) != null && 
                          pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE) != null;

        return hasHead && hasShoulders && hasHips && hasKnees;
    }

    private void detectSquat(Pose pose) {
        PoseLandmark leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
        PoseLandmark leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE);
        PoseLandmark leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE);

        if (leftHip != null && leftKnee != null && leftAnkle != null) {
            // Calculate knee angle
            double kneeAngle = getAngle(leftHip, leftKnee, leftAnkle);

            // Improved squat detection using angle
            if (kneeAngle < 120 && !isSquatting) {
                // Going down - knee angle is less than 120 degrees
                isSquatting = true;
                runOnUiThread(() -> feedbackText.setText("Good! Keep going down"));
                Log.d("Squat", "Going down - Angle: " + kneeAngle);
            } else if (kneeAngle > 160 && isSquatting) {
                // Stood up - knee angle is greater than 160 degrees
                squatReps++;
                runOnUiThread(() -> {
                    repCounter.setText("Squat Reps: " + squatReps + "/" + targetReps);
                    feedbackText.setText("Great rep! Keep it up!");
                });
                isSquatting = false;
                Log.d("Squat", "Rep completed - Angle: " + kneeAngle);
                
                // Check if target reached
                if (squatReps >= targetReps) {
                    completeExercise();
                }
            } else if (kneeAngle > 120 && kneeAngle < 160) {
                runOnUiThread(() -> feedbackText.setText("Hold the squat position"));
            }
        }
    }

    private void detectPushup(Pose pose) {
        PoseLandmark leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
        PoseLandmark leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW);
        PoseLandmark leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST);

        if (leftShoulder != null && leftElbow != null && leftWrist != null) {
            // Calculate elbow angle
            double elbowAngle = getAngle(leftShoulder, leftElbow, leftWrist);

            // Pushup detection
            if (elbowAngle < 90 && !isSquatting) {
                // Going down
                isSquatting = true;
                runOnUiThread(() -> feedbackText.setText("Good! Lower your body"));
                Log.d("Pushup", "Going down - Angle: " + elbowAngle);
            } else if (elbowAngle > 160 && isSquatting) {
                // Pushed up
                squatReps++;
                runOnUiThread(() -> {
                    repCounter.setText("Pushup Reps: " + squatReps + "/" + targetReps);
                    feedbackText.setText("Excellent pushup! Keep going!");
                });
                isSquatting = false;
                Log.d("Pushup", "Rep completed - Angle: " + elbowAngle);
                
                // Check if target reached
                if (squatReps >= targetReps) {
                    completeExercise();
                }
            } else if (elbowAngle > 90 && elbowAngle < 160) {
                runOnUiThread(() -> feedbackText.setText("Hold the pushup position"));
            }
        }
    }

    private void detectPlank(Pose pose) {
        PoseLandmark leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
        PoseLandmark leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
        PoseLandmark leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE);

        if (leftShoulder != null && leftHip != null && leftAnkle != null) {
            // Calculate body alignment angle
            double bodyAngle = getAngle(leftShoulder, leftHip, leftAnkle);

            // Plank detection - body should be relatively straight
            if (bodyAngle > 160 && !isPlanking) {
                // Good plank position
                isPlanking = true;
                plankStartTime = System.currentTimeMillis();
                runOnUiThread(() -> feedbackText.setText("Perfect plank position! Hold it!"));
                Log.d("Plank", "Good position - Angle: " + bodyAngle);
            } else if (bodyAngle < 140 && isPlanking) {
                // Lost plank position
                isPlanking = false;
                runOnUiThread(() -> feedbackText.setText("Get back into plank position"));
                Log.d("Plank", "Position lost - Angle: " + bodyAngle);
            } else if (bodyAngle >= 140 && bodyAngle <= 160) {
                runOnUiThread(() -> feedbackText.setText("Keep your body straight"));
            }
            
            // Check if target time reached (convert targetReps to seconds for plank)
            if (plankTotalTime >= targetReps * 1000) {
                completeExercise();
            }
        }
    }

    private void completeExercise() {
        if (exerciseCompleted) return; // Prevent multiple completions
        
        exerciseCompleted = true;
        final String message;
        if (exerciseType.equals("plank")) {
            long seconds = plankTotalTime / 1000;
            message = "Plank completed! " + seconds + " seconds held.";
        } else {
            message = "Exercise completed! " + squatReps + " reps done.";
        }
        
        runOnUiThread(() -> feedbackText.setText("🎉 " + message));
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        
        // Return to main activity after a short delay
        handler.postDelayed(() -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }, 3000);
    }

    /**
     * Calculate the angle between three points
     * @param firstPoint First point
     * @param midPoint Middle point (vertex of the angle)
     * @param lastPoint Last point
     * @return Angle in degrees
     */
    private static double getAngle(PoseLandmark firstPoint, PoseLandmark midPoint, PoseLandmark lastPoint) {
        double result = Math.toDegrees(
                atan2(lastPoint.getPosition().y - midPoint.getPosition().y,
                        lastPoint.getPosition().x - midPoint.getPosition().x)
                        - atan2(firstPoint.getPosition().y - midPoint.getPosition().y,
                        firstPoint.getPosition().x - midPoint.getPosition().x));
        result = Math.abs(result); // Angle should never be negative
        if (result > 180) {
            result = (360.0 - result); // Always get the acute representation of the angle
        }
        return result;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (poseDetector != null) {
            poseDetector.close();
        }
        handler.removeCallbacksAndMessages(null);
    }
}
