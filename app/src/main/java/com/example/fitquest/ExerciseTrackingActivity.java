package com.example.fitquest;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions;

import java.util.concurrent.ExecutionException;
import android.content.pm.PackageManager;
import android.app.Activity;
import androidx.core.app.ActivityCompat;

import static java.lang.Math.atan2;

public class ExerciseTrackingActivity extends AppCompatActivity {

    private PreviewView previewView;
    private TextView repCounter;
    private TextView angleDisplay;
    private PoseDetector poseDetector;
    private int squatReps = 0;
    private boolean isSquatting = false;
    private String exerciseType = "squats";
    private LinearLayout permissionLayout;
    private Button requestPermissionButton;

    private static final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA"};
    private static final int REQUEST_CODE_PERMISSIONS = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_tracking);

        previewView = findViewById(R.id.previewView);
        repCounter = findViewById(R.id.repCounter);
        angleDisplay = findViewById(R.id.angleDisplay);
        permissionLayout = findViewById(R.id.permission_layout);
        requestPermissionButton = findViewById(R.id.request_permission_button);

        // Get exercise type from intent
        if (getIntent().hasExtra("EXERCISE_TYPE")) {
            exerciseType = getIntent().getStringExtra("EXERCISE_TYPE");
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
        } else {
            showPermissionUI();
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
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

    private void detectSquat(Pose pose) {
        PoseLandmark leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
        PoseLandmark leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE);
        PoseLandmark leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE);

        if (leftHip != null && leftKnee != null && leftAnkle != null) {
            // Calculate knee angle
            double kneeAngle = getAngle(leftHip, leftKnee, leftAnkle);
            
            // Update angle display
            runOnUiThread(() -> angleDisplay.setText("Knee Angle: " + String.format("%.1f°", kneeAngle)));

            // Improved squat detection using angle
            if (kneeAngle < 120 && !isSquatting) {
                // Going down - knee angle is less than 120 degrees
                isSquatting = true;
                Log.d("Squat", "Going down - Angle: " + kneeAngle);
            } else if (kneeAngle > 160 && isSquatting) {
                // Stood up - knee angle is greater than 160 degrees
                squatReps++;
                runOnUiThread(() -> repCounter.setText("Squat Reps: " + squatReps));
                isSquatting = false;
                Log.d("Squat", "Rep completed - Angle: " + kneeAngle);
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
            
            runOnUiThread(() -> angleDisplay.setText("Elbow Angle: " + String.format("%.1f°", elbowAngle)));

            // Pushup detection
            if (elbowAngle < 90 && !isSquatting) {
                // Going down
                isSquatting = true;
                Log.d("Pushup", "Going down - Angle: " + elbowAngle);
            } else if (elbowAngle > 160 && isSquatting) {
                // Pushed up
                squatReps++;
                runOnUiThread(() -> repCounter.setText("Pushup Reps: " + squatReps));
                isSquatting = false;
                Log.d("Pushup", "Rep completed - Angle: " + elbowAngle);
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
            
            runOnUiThread(() -> angleDisplay.setText("Body Angle: " + String.format("%.1f°", bodyAngle)));

            // Plank detection - body should be relatively straight
            if (bodyAngle > 160 && !isSquatting) {
                // Good plank position
                isSquatting = true;
                Log.d("Plank", "Good position - Angle: " + bodyAngle);
            } else if (bodyAngle < 140 && isSquatting) {
                // Lost plank position
                squatReps++;
                runOnUiThread(() -> repCounter.setText("Plank Holds: " + squatReps));
                isSquatting = false;
                Log.d("Plank", "Position lost - Angle: " + bodyAngle);
            }
        }
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
}
